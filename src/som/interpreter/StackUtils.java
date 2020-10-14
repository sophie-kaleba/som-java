/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.interpreter;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBlock;
import som.vmobjects.SMethod;


/**
 * Arguments.
 * 0) Interpreter
 * 2) Invokable
 * 3) Arguments
 *
 * Slots:
 * 0) Execution stack
 * 1) Stack pointer
 * 2) onStack marker
 */
public class StackUtils {
  private enum FrameArguments {
    INTERPRETER,
    INVOKABLE,
    ARGUMENTS
  }

  public static void initializeStackSlots(final VirtualFrame frame,
      final FrameSlot executionStackSlot,
      final SMethod method) {

    int stackLength = (int) method.getMaximumLengthOfStack();
    SAbstractObject[] stack = new SAbstractObject[stackLength];
    for (int i = 0; i < stackLength; i++) {
      stack[i] = Universe.current().nilObject;
    }

    // TODO - getting the arguments from the frame to put them on stack...
    // could likely do both when putting them in the stack in the first place
    // or just don't put them in the frame's arguments, is is useful anyway?
    final SAbstractObject[] arguments = getCurrentArguments(frame);
    System.arraycopy(arguments, 0, stack, 0, arguments.length);

    frame.setObject(executionStackSlot, stack);
    resetStackPointer(frame, method);
  }

  public static FrameOnStackMarker initializeStackMarkerSlot(VirtualFrame frame,
      FrameSlot frameOnStackMarkerSlot) {
    FrameOnStackMarker marker = new FrameOnStackMarker();
    frame.setObject(frameOnStackMarkerSlot, marker);

    return marker;
  }

  /***
   * CURRENT STATE OF THE FRAME.
   */

  public static SMethod getCurrentMethod(final VirtualFrame frame) {
    return (SMethod) frame.getArguments()[FrameArguments.INVOKABLE.ordinal()];
  }

  public static SAbstractObject[] getCurrentArguments(VirtualFrame frame) {
    return (SAbstractObject[]) frame.getArguments()[FrameArguments.ARGUMENTS.ordinal()];
  }

  public static SAbstractObject[] getCurrentStack(VirtualFrame frame)
      throws FrameSlotTypeException {
    return (SAbstractObject[]) frame.getObject(getCurrentMethod(frame).getStackSlot());
  }

  public static int getCurrentStackPointer(VirtualFrame frame) throws FrameSlotTypeException {
    return frame.getInt(getCurrentMethod(frame).getStackPointerSlot());
  }

  public static FrameOnStackMarker getCurrentOnStackMarker(VirtualFrame frame)
      throws FrameSlotTypeException {
    return (FrameOnStackMarker) frame.getObject(
        getCurrentMethod(frame).getFrameOnStackMarkerSlot());
  }

  public static SAbstractObject[] getArguments(VirtualFrame frame, SMethod method)
      throws FrameSlotTypeException {
    int numArgs = method.getNumberOfArguments();
    SAbstractObject[] arguments = new SAbstractObject[numArgs];
    for (int i = 0; i < numArgs; ++i) {
      arguments[i] = StackUtils.getRelativeStackElement(frame, numArgs - 1 - i);
    }
    return arguments;
  }

  /**
   * EXECUTION STACK MODIFIERS.
   */

  private static void setStackPointer(VirtualFrame frame, FrameSlot stackPointerSlot,
      int value) {
    frame.setInt(stackPointerSlot, value);
  }

  public static void resetStackPointer(VirtualFrame frame,
      SMethod method) {
    // arguments are stored in front of local variables
    int localOffset = method.getNumberOfArguments();

    // Set the stack pointer to its initial value thereby clearing the stack
    setStackPointer(frame, method.getStackPointerSlot(), localOffset
        + (int) method.getNumberOfLocals().getEmbeddedInteger() - 1);
  }

  /**
   * will return top of stack if index is 0.
   */
  public static SAbstractObject getRelativeStackElement(VirtualFrame frame, int index)
      throws FrameSlotTypeException {
    int sp = getCurrentStackPointer(frame);
    return getCurrentStack(frame)[sp - index];
  }

  private static SAbstractObject getStackElement(VirtualFrame frame,
      int index)
      throws FrameSlotTypeException {
    return getCurrentStack(frame)[index];
  }

  // TODO - modify the value on the spot rather than setting the whole stack again
  private static void setStackElement(VirtualFrame frame,
      final FrameSlot executionStackSlot, int index, SAbstractObject value)
      throws FrameSlotTypeException {

    SAbstractObject[] stack = getCurrentStack(frame);
    stack[index] = value;
    frame.setObject(executionStackSlot, stack);
  }

  public static SAbstractObject pop(VirtualFrame frame) throws FrameSlotTypeException {
    // Pop an object from the expression stack and return it
    int currentStackPointer = getCurrentStackPointer(frame);

    SAbstractObject result = getStackElement(frame, currentStackPointer);
    setStackPointer(frame, getCurrentMethod(frame).getStackPointerSlot(),
        currentStackPointer - 1);
    return result;
  }

  public static void push(VirtualFrame frame,
      SAbstractObject value) throws FrameSlotTypeException {
    // Push an object onto the expression stack
    int currentStackPointer = getCurrentStackPointer(frame) + 1;
    FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    FrameSlot currentStackPointerSlot = getCurrentMethod(frame).getStackPointerSlot();

    setStackElement(frame, currentStackSlot, currentStackPointer, value);
    setStackPointer(frame, currentStackPointerSlot, currentStackPointer);
  }

  public static void popArgumentsAndPushResult(VirtualFrame frame,
      final SAbstractObject result, SMethod method) throws FrameSlotTypeException {
    int numberOfArguments = method.getNumberOfArguments();

    for (int i = 0; i < numberOfArguments; i++) {
      pop(frame);
    }

    push(frame, result);
  }

  public static SAbstractObject getArgument(final VirtualFrame frame,
      final int index,
      final int contextLevel)
      throws FrameSlotTypeException {
    VirtualFrame context = getContext(frame, contextLevel);
    return getStackElement(context, index);
  }

  public static void setArgument(final VirtualFrame frame,
      final int index, final int contextLevel,
      final SAbstractObject value) throws FrameSlotTypeException {
    VirtualFrame context = getContext(frame, contextLevel);

    // TODO - add a test for this commented case
    // FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    FrameSlot currentStackSlot = getCurrentMethod(context).getStackSlot();

    setStackElement(context, currentStackSlot, index, value);
  }

  @ExplodeLoop
  public static VirtualFrame getContext(VirtualFrame frame, int contextLevel) {

    while (contextLevel > 0) {
      SBlock receiver = (SBlock) getCurrentArguments(frame)[0];
      frame = receiver.getMaterializedContext();
      contextLevel--;
    }

    return frame;
  }

  public static SAbstractObject getLocal(final VirtualFrame frame,
      final int index, final int contextLevel)
      throws FrameSlotTypeException {

    VirtualFrame context = getContext(frame, contextLevel);
    SMethod contextMethod = getCurrentMethod(context);
    int localOffset = contextMethod.getNumberOfArguments();

    return getStackElement(context, localOffset + index);
  }

  private static void setLocal(VirtualFrame frame, FrameSlot executionStackSlot,
      final int index, int localOffset, SAbstractObject value) throws FrameSlotTypeException {
    setStackElement(frame, executionStackSlot, localOffset + index, value);
  }

  public static void setLocal(final VirtualFrame frame,
      final int index,
      final int contextLevel, SAbstractObject value)
      throws FrameSlotTypeException {
    FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    VirtualFrame context = getContext(frame, contextLevel);
    int localOffset = getCurrentMethod(context).getNumberOfArguments();

    setLocal(context, currentStackSlot, index, localOffset, value);
  }

}

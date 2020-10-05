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

import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBlock;
import som.vmobjects.SMethod;


/**
 * Arguments:
 * 0) Interpreter
 * 2) Invokable
 * 3) Arguments
 * 4) Receiver
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
    ARGUMENTS,
    RECEIVER
  }

  private enum FrameSlots {
    STACK,
    STACK_POINTER,
    ON_STACK_MARKER
  }

  // TODO - clean
  public static void initializeStackSlots(final VirtualFrame frame,
      final FrameSlot executionStackSlot,
      final FrameSlot frameOnStackMarkerSlot,
      final SMethod method) {
    int stackLength = (int) method.getMaximumLengthOfStack();
    SAbstractObject[] stack = new SAbstractObject[stackLength];

    for (int i = 0; i < stackLength; i++) {
      stack[i] = Universe.current().nilObject;
    }

    final SAbstractObject[] arguments =
        (SAbstractObject[]) frame.getArguments()[FrameArguments.ARGUMENTS.ordinal()];

    int numArgs = arguments.length;
    for (int i = 0; i < numArgs; ++i) {
      stack[i] = arguments[i];
    }

    frame.setObject(executionStackSlot, stack);
    resetStackPointer(frame, method);
    FrameOnStackMarker marker = new FrameOnStackMarker();
    frame.setObject(frameOnStackMarkerSlot, marker);
  }

  /***
   * CURRENT STATE OF THE FRAME
   */

  public static SMethod getCurrentMethod(final VirtualFrame frame) {
    return (SMethod) frame.getArguments()[FrameArguments.INVOKABLE.ordinal()];
  }

  public static SAbstractObject getCurrentReceiver(VirtualFrame frame) {
    return (SAbstractObject) frame.getArguments()[FrameArguments.RECEIVER.ordinal()];
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
    return (FrameOnStackMarker) frame.getObject(getCurrentMethod(frame).getOnStackSlot());
  }

  public static SAbstractObject[] copyArgumentFrom(VirtualFrame frame, SMethod method)
      throws FrameSlotTypeException {
    int numArgs = method.getNumberOfArguments();
    SAbstractObject[] arguments = new SAbstractObject[numArgs];
    for (int i = 0; i < numArgs; ++i) {
      arguments[i] = StackUtils.getRelativeStackElement(frame, numArgs - 1 - i);
    }
    return arguments;
  }

  /**
   * EXECUTION STACK MODIFIERS
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
   * will return top of stack if index is 0
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

  // TODO - delete
  // public static SAbstractObject getArgumentFrom(VirtualFrame truffleFrame, int index,
  // int contextLevel) {
  // VirtualFrame context = getContext(truffleFrame, contextLevel);
  //
  // // Get the argument with the given index
  // return getCurrentArguments(context)[index];
  // }

  public static SAbstractObject getArgumentFromStack(VirtualFrame frame, int index,
      int contextLevel) throws FrameSlotTypeException {
    VirtualFrame context = getContext(frame, contextLevel);
    return getStackElement(context, index);
  }

  public static VirtualFrame getContext(VirtualFrame frame, int contextLevel) {

    while (contextLevel > 0) {
      SBlock receiver = (SBlock) getCurrentArguments(frame)[0];
      frame = receiver.getMaterializedContext();
      contextLevel--;
    }

    return frame;
  }

  protected static VirtualFrame determineContext(VirtualFrame frame) {
    SAbstractObject self = getCurrentArguments(frame)[0];
    int i = self.getContextLevel();

    while (i > 0) {
      frame = self.getMaterializedContext();
      self = getCurrentArguments(frame)[0];
      i--;
    }

    return frame;
  }

  public static SAbstractObject getLocal(final VirtualFrame frame,
      final int index, final int contextLevel)
      throws FrameSlotTypeException {
    // Get the local with the given index in the given context

    VirtualFrame context = getContext(frame, contextLevel);
    SMethod contextMethod =
        (SMethod) context.getArguments()[FrameArguments.INVOKABLE.ordinal()];
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
    // Get the local with the given index in the given context
    FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    VirtualFrame context = getContext(frame, contextLevel);
    int localOffset = getCurrentMethod(context).getNumberOfArguments();

    setLocal(context, currentStackSlot, index, localOffset, value);
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

    // TODO - check if I had to get the stack slot of the context (believe so)
    // FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    FrameSlot currentStackSlot = getCurrentMethod(context).getStackSlot();

    setStackElement(context, currentStackSlot, index, value);
  }

  public static void copyArgumentsFrom(final VirtualFrame frame,
      final FrameSlot executionStackSlot,
      final SMethod method) throws FrameSlotTypeException {
    // copy arguments from frame:
    // - arguments are at the top of the stack of frame.
    // - copy them into the argument area of the current frame
    int numArgs = method.getNumberOfArguments();
    for (int i = 0; i < numArgs; ++i) {
      SAbstractObject value =
          getStackElement(frame, numArgs - 1 - i);
      setStackElement(frame, executionStackSlot, i, value);
    }
  }

  public static SAbstractObject[] fetchArguments(final VirtualFrame truffleFrame,
      int numArgs) throws FrameSlotTypeException {
    SAbstractObject localArgs[] = new SAbstractObject[numArgs];
    for (int i = 0; i < numArgs; ++i) {
      SAbstractObject value =
          getRelativeStackElement(truffleFrame, numArgs - 1 - i);
      localArgs[i] = value;
    }
    return localArgs;
  }

  public static boolean isOnStack(VirtualFrame truffleFrame, FrameSlot onStackSlot)
      throws FrameSlotTypeException {
    return truffleFrame.getBoolean(onStackSlot);
  }

  public static boolean areStackEqual(final VirtualFrame truffleFrame, final Frame frame)
      throws FrameSlotTypeException {
    int stackSize = getCurrentStack(truffleFrame).length;
    boolean result = true;

    assert stackSize == frame.getStack().length;

    for (int i = 0; i < stackSize; i++) {
      result = result && getStackElement(truffleFrame, i).equals(frame.getStack()[i]);
    }

    return result;
  }

}

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

import java.util.Arrays;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import som.GraalSOMLanguage;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBlock;
import som.vmobjects.SMethod;


/**
 * Frame arguments indexes.
 * 0) Invokable
 * 1) Arguments
 *
 * Frame slots indexes:
 * 0) Execution stack
 * 1) Stack pointer
 * 2) onStack marker
 */
public class StackUtils {
  private enum FrameArguments {
    INVOKABLE,
    ARGUMENTS
  }

  public static final FrameDescriptor FRAME_DESCRIPTOR =
      GraalSOMLanguage.getCurrentContext().newFrameDescriptor();

  private static final FrameSlot STACK_POINTER_SLOT   =
      FRAME_DESCRIPTOR.findFrameSlot("stackPointer");
  public static final FrameSlot  STACK_SLOT           =
      FRAME_DESCRIPTOR.findFrameSlot("stack");
  private static final FrameSlot ON_STACK_MARKER_SLOT =
      FRAME_DESCRIPTOR.findFrameSlot("onStack");

  public static void initializeStackSlots(final VirtualFrame frame,
      final SMethod method) {

    int stackLength = (int) method.getMaximumLengthOfStack();
    SAbstractObject[] stack = new SAbstractObject[stackLength];
    Arrays.fill(stack, Universe.current().nilObject);

    // TODO - getting the arguments from the frame to put them on stack...
    // could likely do both when putting them in the stack in the first place
    // or just don't put them in the frame's arguments, is is useful anyway?
    final SAbstractObject[] arguments = getCurrentArguments(frame);
    System.arraycopy(arguments, 0, stack, 0, arguments.length);

    frame.setObject(STACK_SLOT, stack);
    resetStackPointer(frame, method);
  }

  public static FrameOnStackMarker initializeStackMarkerSlot(VirtualFrame frame) {
    FrameOnStackMarker marker = new FrameOnStackMarker();
    frame.setObject(ON_STACK_MARKER_SLOT, marker);

    return marker;
  }

  /***
   * CURRENT STATE OF THE FRAME.
   */
  public static int getStackPointer(VirtualFrame frame) {
    return FrameUtil.getIntSafe(frame, STACK_POINTER_SLOT);
  }

  public static SAbstractObject[] getStack(VirtualFrame frame) {
    return (SAbstractObject[]) FrameUtil.getObjectSafe(frame, STACK_SLOT);
  }

  public static FrameOnStackMarker getOnStackMarker(VirtualFrame frame) {
    return (FrameOnStackMarker) FrameUtil.getObjectSafe(frame, ON_STACK_MARKER_SLOT);
  }

  public static SMethod getMethod(final VirtualFrame frame) {
    return (SMethod) frame.getArguments()[FrameArguments.INVOKABLE.ordinal()];
  }

  public static SAbstractObject[] getCurrentArguments(VirtualFrame frame) {
    return (SAbstractObject[]) frame.getArguments()[FrameArguments.ARGUMENTS.ordinal()];
  }

  public static SAbstractObject[] getArguments(VirtualFrame frame, SMethod method) {
    int numArgs = method.getNumberOfArguments();
    SAbstractObject[] arguments = new SAbstractObject[numArgs];
    for (int i = 0; i < numArgs; ++i) {
      arguments[i] = getRelativeStackElement(frame, numArgs - 1 - i);
    }
    return arguments;
  }

  /**
   * EXECUTION STACK MODIFIERS.
   */

  private static void setStackPointer(VirtualFrame frame,
      int value) {
    frame.setInt(STACK_POINTER_SLOT, value);
  }

  public static void resetStackPointer(VirtualFrame frame,
      SMethod method) {
    // arguments are stored in front of local variables
    int localOffset = method.getNumberOfArguments();

    // Set the stack pointer to its initial value thereby clearing the stack
    setStackPointer(frame,
        localOffset + (int) method.getNumberOfLocals().getEmbeddedInteger() - 1);
  }

  /**
   * will return top of stack if index is 0.
   */
  public static SAbstractObject getRelativeStackElement(VirtualFrame frame, int index) {
    return getStack(frame)[getStackPointer(frame) - index];
  }

  private static SAbstractObject getStackElement(VirtualFrame frame,
      int index) {
    return getStack(frame)[index];
  }

  // TODO - modify the value on the spot rather than setting the whole stack again
  private static void setStackElement(VirtualFrame frame,
      int index, SAbstractObject value) {

    SAbstractObject[] stack = getStack(frame);
    stack[index] = value;
    frame.setObject(STACK_SLOT, stack);
  }

  public static SAbstractObject pop(VirtualFrame frame) {
    int currentStackPointer = getStackPointer(frame);

    SAbstractObject result = getStackElement(frame, currentStackPointer);
    setStackPointer(frame, currentStackPointer - 1);
    return result;
  }

  public static void push(VirtualFrame frame,
      SAbstractObject value) {

    int currentStackPointer = getStackPointer(frame) + 1;

    setStackElement(frame, currentStackPointer, value);
    setStackPointer(frame, currentStackPointer);
  }

  public static void popArgumentsAndPushResult(VirtualFrame frame,
      final SAbstractObject result, SMethod method) {
    int numberOfArguments = method.getNumberOfArguments();

    for (int i = 0; i < numberOfArguments; i++) {
      pop(frame);
    }

    push(frame, result);
  }

  public static SAbstractObject getArgument(final VirtualFrame frame,
      final int index,
      final int contextLevel) {
    VirtualFrame context = getContext(frame, contextLevel);
    return getStackElement(context, index);
  }

  public static void setArgument(final VirtualFrame frame,
      final int index, final int contextLevel,
      final SAbstractObject value) {
    CompilerAsserts.partialEvaluationConstant(contextLevel);

    VirtualFrame context = getContext(frame, contextLevel);

    // TODO - add a test for this commented case
    // FrameSlot currentStackSlot = getCurrentMethod(frame).getStackSlot();
    setStackElement(context, index, value);
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
      final int index, final int contextLevel) {
    VirtualFrame context = getContext(frame, contextLevel);
    SMethod contextMethod = getMethod(context);
    int localOffset = contextMethod.getNumberOfArguments();

    return getStackElement(context, localOffset + index);
  }

  public static void setLocal(final VirtualFrame frame,
      final int index,
      final int contextLevel, SAbstractObject value) {
    VirtualFrame context = getContext(frame, contextLevel);
    int localOffset = getMethod(context).getNumberOfArguments();

    setStackElement(context, localOffset + index, value);
  }

}

/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.vmobjects;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;

import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;


public class SBlock extends SAbstractObject {

  private final SMethod method;
  private final SClass  blockClass;

  private final MaterializedFrame materializedContext;

  public SBlock(final SMethod method, final SClass blockClass,
      final MaterializedFrame materializedContext) {
    this.method = method;
    this.blockClass = blockClass;
    this.materializedContext = materializedContext;
  }

  public int getContextLevel() {
    return method.getContextLevel();
  }

  public SMethod getMethod() {
    return method;
  }

  public MaterializedFrame getMaterializedContext() {
    return materializedContext;
  }

  @Override
  public final SClass getSOMClass(final Universe universe) {
    return blockClass;
  }

  public static SPrimitive getEvaluationPrimitive(int numberOfArguments,
      final Universe universe) {
    return new Evaluation(numberOfArguments, universe);
  }

  public static class Evaluation extends SPrimitive {

    private final int numberOfArguments;

    public Evaluation(int numberOfArguments, final Universe universe) {
      super(computeSignatureString(numberOfArguments), universe);
      this.numberOfArguments = numberOfArguments;
    }

    @Override
    public void invoke(final VirtualFrame frame,
        final Interpreter interpreter) {

      // Get the block (the receiver) from the stack
      SBlock self =
          (SBlock) StackUtils.getRelativeStackElement(frame, numberOfArguments - 1);

      // Push a new frame and set its context to be the one specified in
      // the block
      IndirectCallNode indirectCallNode = interpreter.getIndirectCallNode();
      // TODO - see whether I can get the method considered as PE constant
      SAbstractObject[] arguments = StackUtils.getArgumentsFromStack(frame, self.method);

      SAbstractObject result =
          (SAbstractObject) indirectCallNode.call(self.getMethod().getCallTarget(),
              (Object[]) arguments);

      StackUtils.popArgumentsAndPushResult(frame, result, self.method);
    }

    private static java.lang.String computeSignatureString(int numberOfArguments) {
      // Compute the signature string
      java.lang.String signatureString = "value";
      if (numberOfArguments > 1) {
        signatureString += ":";
      }

      // Add extra value: selector elements if necessary
      for (int i = 2; i < numberOfArguments; i++) {
        signatureString += "with:";
      }

      // Return the signature string
      return signatureString;
    }
  }
}

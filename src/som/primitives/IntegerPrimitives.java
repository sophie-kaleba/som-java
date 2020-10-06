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

package som.primitives;

import java.math.BigInteger;

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.*;


public class IntegerPrimitives extends Primitives {

  public IntegerPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("asString", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber self = (SNumber) StackUtils.pop(truffleFrame);

        SString value = self.primAsString(universe);

        StackUtils.push(truffleFrame, value);

      }
    });

    installInstancePrimitive(new SPrimitive("sqrt", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger self = (SInteger) StackUtils.pop(truffleFrame);

        SNumber result = self.primSqrt(universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("atRandom", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger self = (SInteger) StackUtils.pop(truffleFrame);

        SInteger randomInt = universe.newInteger(
            (long) (self.getEmbeddedInteger() * Math.random()));

        StackUtils.push(truffleFrame, randomInt);

      }
    });

    installInstancePrimitive(new SPrimitive("+", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primAdd(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("-", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primSubtract(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("*", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primMultiply(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("//", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primDoubleDivide(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("/", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primIntegerDivide(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("%", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primModulo(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("rem:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SInteger left = (SInteger) StackUtils.pop(truffleFrame);

        SInteger result = left.primRemainder(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("&", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber result = left.primBitAnd(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject right = StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SObject result = left.primEqual(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("<", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);
        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SObject result = left.primLessThan(right, universe);

        StackUtils.push(truffleFrame, result);

      }
    });

    installInstancePrimitive(new SPrimitive("<<", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);

        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber value = left.primLeftShift(right, universe);

        StackUtils.push(truffleFrame, value);

      }
    });

    installInstancePrimitive(new SPrimitive("bitXor:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) StackUtils.pop(truffleFrame);

        SNumber left = (SNumber) StackUtils.pop(truffleFrame);

        SNumber xored = left.primBitXor(right, universe);

        StackUtils.push(truffleFrame, xored);

      }
    });

    installInstancePrimitive(new SPrimitive("as32BitSignedValue", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger rcvr = (SInteger) StackUtils.pop(truffleFrame);

        SInteger asSigned32 = universe.newInteger((int) rcvr.getEmbeddedInteger());

        StackUtils.push(truffleFrame, asSigned32);

      }
    });

    installInstancePrimitive(new SPrimitive("as32BitUnsignedValue", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger rcvr = (SInteger) StackUtils.pop(truffleFrame);

        SInteger as32Int =
            universe.newInteger(Integer.toUnsignedLong((int) rcvr.getEmbeddedInteger()));

        StackUtils.push(truffleFrame, as32Int);

      }
    });

    installInstancePrimitive(new SPrimitive(">>>", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger right = (SInteger) StackUtils.pop(truffleFrame);

        SInteger rcvr = (SInteger) StackUtils.pop(truffleFrame);

        SInteger shiftedInteger =
            universe.newInteger(rcvr.getEmbeddedInteger() >>> right.getEmbeddedInteger());

        StackUtils.push(truffleFrame, shiftedInteger);

      }
    });

    installClassPrimitive(new SPrimitive("fromString:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString param = (SString) StackUtils.pop(truffleFrame);

        StackUtils.pop(truffleFrame);

        try {
          long result = Long.parseLong(param.getEmbeddedString());

          SInteger integer = universe.newInteger(result);

          StackUtils.push(truffleFrame, integer);

        } catch (NumberFormatException e) {
          BigInteger result = new BigInteger(param.getEmbeddedString());

          SBigInteger bigInteger = new SBigInteger(result);
          StackUtils.push(truffleFrame, bigInteger);
        }

      }
    });
  }
}

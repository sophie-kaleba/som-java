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

import som.interpreter.Frame;
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
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber self = (SNumber) frame.pop();
        SNumber selfT = (SNumber) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SString stringed = self.primAsString(universe);

        frame.push(stringed);
        StackUtils.push(truffleFrame, stringed);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("sqrt", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger self = (SInteger) frame.pop();
        SInteger selfT = (SInteger) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SNumber result = self.primSqrt(universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("atRandom", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger self = (SInteger) frame.pop();
        SInteger selfT = (SInteger) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SInteger randomInt = universe.newInteger(
            (long) (self.getEmbeddedInteger() * Math.random()));

        frame.push(randomInt);
        StackUtils.push(truffleFrame, randomInt);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("+", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primAdd(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("-", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primSubtract(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("*", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primMultiply(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("//", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primDoubleDivide(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("/", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primIntegerDivide(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("%", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primModulo(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("rem:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SInteger left = (SInteger) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SInteger leftT = (SInteger) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SInteger result = left.primRemainder(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("&", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SNumber result = left.primBitAnd(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject right = frame.pop();
        SNumber left = (SNumber) frame.pop();

        SAbstractObject rightT = StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert left == leftT;
        assert right == rightT;

        SObject result = left.primEqual(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("<", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber left = (SNumber) frame.pop();

        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;
        assert left == leftT;

        SObject result = left.primLessThan(right, universe);

        frame.push(result);
        StackUtils.push(truffleFrame, result);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("<<", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;

        SNumber left = (SNumber) frame.pop();
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert left == leftT;

        SNumber shifted = left.primLeftShift(right, universe);

        frame.push(shifted);

        StackUtils.push(truffleFrame, shifted);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("bitXor:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SNumber right = (SNumber) frame.pop();
        SNumber rightT = (SNumber) StackUtils.pop(truffleFrame);

        assert right == rightT;

        SNumber left = (SNumber) frame.pop();
        SNumber leftT = (SNumber) StackUtils.pop(truffleFrame);

        assert left == leftT;

        SNumber xored = left.primBitXor(right, universe);

        frame.push(xored);
        StackUtils.push(truffleFrame, xored);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("as32BitSignedValue", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger rcvr = (SInteger) frame.pop();
        SInteger rcvrT = (SInteger) StackUtils.pop(truffleFrame);

        assert rcvr == rcvrT;

        SInteger asSigned32 = universe.newInteger((int) rcvr.getEmbeddedInteger());

        frame.push(asSigned32);
        StackUtils.push(truffleFrame, asSigned32);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";

      }
    });

    installInstancePrimitive(new SPrimitive("as32BitUnsignedValue", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger rcvr = (SInteger) frame.pop();
        SInteger rcvrT = (SInteger) StackUtils.pop(truffleFrame);

        assert rcvr == rcvrT;

        SInteger as32Int =
            universe.newInteger(Integer.toUnsignedLong((int) rcvr.getEmbeddedInteger()));

        frame.push(as32Int);
        StackUtils.push(truffleFrame, as32Int);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive(">>>", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger right = (SInteger) frame.pop();
        SInteger rightT = (SInteger) StackUtils.pop(truffleFrame);

        assert right == rightT;

        SInteger rcvr = (SInteger) frame.pop();
        SInteger rcvrT = (SInteger) StackUtils.pop(truffleFrame);

        assert rcvr == rcvrT;

        SInteger shiftedInteger =
            universe.newInteger(rcvr.getEmbeddedInteger() >>> right.getEmbeddedInteger());

        frame.push(shiftedInteger);
        StackUtils.push(truffleFrame, shiftedInteger);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installClassPrimitive(new SPrimitive("fromString:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString param = (SString) frame.pop();
        SString paramT = (SString) StackUtils.pop(truffleFrame);

        frame.pop();
        StackUtils.pop(truffleFrame);

        try {
          long result = Long.parseLong(param.getEmbeddedString());
          long resultT = Long.parseLong(paramT.getEmbeddedString());

          assert result == resultT : "objects differ";

          SInteger integer = universe.newInteger(result);
          frame.push(integer);
          StackUtils.push(truffleFrame, integer);

        } catch (NumberFormatException e) {
          BigInteger result = new BigInteger(param.getEmbeddedString());
          BigInteger resultT = new BigInteger(paramT.getEmbeddedString());

          assert result == resultT : "objects differ";

          SBigInteger bigInteger = new SBigInteger(result);
          frame.push(bigInteger);
          StackUtils.push(truffleFrame, bigInteger);
        }
        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });
  }
}

/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
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

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.*;


public class StringPrimitives extends Primitives {

  public StringPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("concatenate:", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString argument = (SString) frame.pop();
        SString self = (SString) frame.pop();

        SString argumentT = (SString) StackUtils.pop(truffleFrame);
        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert argument == argumentT;
        assert self == selfT;

        SString value = universe.newString(self.getEmbeddedString()
            + argument.getEmbeddedString());
        frame.push(value);
        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("asSymbol", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SSymbol value = universe.symbolFor(self.getEmbeddedString());

        frame.push(value);
        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("length", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SInteger value = universe.newInteger(self.getEmbeddedString().length());
        frame.push(value);
        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject op1 = frame.pop();
        SString op2 = (SString) frame.pop(); // self

        SAbstractObject op1T = StackUtils.pop(truffleFrame);
        SString op2T = (SString) StackUtils.pop(truffleFrame);

        assert op1 == op1T;
        assert op2 == op2T;

        if (op1.getSOMClass(universe) == universe.stringClass) {
          SString s = (SString) op1;
          if (s.getEmbeddedString().equals(op2.getEmbeddedString())) {
            frame.push(universe.trueObject);
            StackUtils.push(truffleFrame, universe.trueObject);
            return;
          }
        }

        frame.push(universe.falseObject);
        StackUtils.push(truffleFrame, universe.falseObject);
      }
    });

    installInstancePrimitive(new SPrimitive("primSubstringFrom:to:", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SInteger end = (SInteger) frame.pop();
        SInteger start = (SInteger) frame.pop();

        SString self = (SString) frame.pop();

        SInteger endT = (SInteger) StackUtils.pop(truffleFrame);
        SInteger startT = (SInteger) StackUtils.pop(truffleFrame);

        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert end == endT;
        assert start == startT;
        assert self == selfT;

        try {
          SString value = universe.newString(self.getEmbeddedString().substring(
              (int) start.getEmbeddedInteger() - 1,
              (int) end.getEmbeddedInteger()));

          frame.push(value);
          StackUtils.push(truffleFrame, value);
        } catch (IndexOutOfBoundsException e) {
          SString error = universe.newString(new java.lang.String(
              "Error - index out of bounds"));

          frame.push(error);
          StackUtils.push(truffleFrame, error);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("hashcode", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SInteger value = universe.newInteger(self.getEmbeddedString().hashCode());
        frame.push(value);
        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("isWhiteSpace", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isWhitespace(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
          StackUtils.push(truffleFrame, universe.trueObject);
        } else {
          frame.push(universe.falseObject);
          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isLetters", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isLetter(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
          StackUtils.push(truffleFrame, universe.trueObject);
        } else {
          frame.push(universe.falseObject);
          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isDigits", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        SString selfT = (SString) StackUtils.pop(truffleFrame);

        assert self == selfT;

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isDigit(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
          StackUtils.push(truffleFrame, universe.trueObject);
        } else {
          frame.push(universe.falseObject);
          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });
  }
}

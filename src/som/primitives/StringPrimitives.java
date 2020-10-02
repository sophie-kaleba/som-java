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
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString argument = (SString) StackUtils.pop(truffleFrame);
        SString self = (SString) StackUtils.pop(truffleFrame);

        SString value = universe.newString(self.getEmbeddedString()
            + argument.getEmbeddedString());

        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("asSymbol", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString self = (SString) StackUtils.pop(truffleFrame);

        SSymbol value = universe.symbolFor(self.getEmbeddedString());

        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("length", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString self = (SString) StackUtils.pop(truffleFrame);

        SInteger value = universe.newInteger(self.getEmbeddedString().length());

        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject op1 = StackUtils.pop(truffleFrame);
        SString op2 = (SString) StackUtils.pop(truffleFrame); // self

        if (op1.getSOMClass(universe) == universe.stringClass) {
          SString s = (SString) op1;
          if (s.getEmbeddedString().equals(op2.getEmbeddedString())) {

            StackUtils.push(truffleFrame, universe.trueObject);
            return;
          }
        }

        StackUtils.push(truffleFrame, universe.falseObject);
      }
    });

    installInstancePrimitive(new SPrimitive("primSubstringFrom:to:", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger end = (SInteger) StackUtils.pop(truffleFrame);
        SInteger start = (SInteger) StackUtils.pop(truffleFrame);

        SString self = (SString) StackUtils.pop(truffleFrame);

        try {
          SString value = universe.newString(self.getEmbeddedString().substring(
              (int) start.getEmbeddedInteger() - 1,
              (int) end.getEmbeddedInteger()));

          StackUtils.push(truffleFrame, value);
        } catch (IndexOutOfBoundsException e) {
          SString error = universe.newString(new java.lang.String(
              "Error - index out of bounds"));

          StackUtils.push(truffleFrame, error);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("hashcode", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString self = (SString) StackUtils.pop(truffleFrame);

        SInteger value = universe.newInteger(self.getEmbeddedString().hashCode());

        StackUtils.push(truffleFrame, value);
      }
    });

    installInstancePrimitive(new SPrimitive("isWhiteSpace", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString self = (SString) StackUtils.pop(truffleFrame);
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isWhitespace(embedded.charAt(i))) {

            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {

          StackUtils.push(truffleFrame, universe.trueObject);
        } else {

          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isLetters", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString self = (SString) StackUtils.pop(truffleFrame);
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isLetter(embedded.charAt(i))) {

            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {

          StackUtils.push(truffleFrame, universe.trueObject);
        } else {

          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isDigits", universe) {

      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SString self = (SString) StackUtils.pop(truffleFrame);
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isDigit(embedded.charAt(i))) {

            StackUtils.push(truffleFrame, universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {

          StackUtils.push(truffleFrame, universe.trueObject);
        } else {

          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });
  }
}

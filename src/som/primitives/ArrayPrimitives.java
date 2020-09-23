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

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SInteger;
import som.vmobjects.SPrimitive;


public class ArrayPrimitives extends Primitives {

  public ArrayPrimitives(final Universe universe) {
    super(universe);
  }

  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("at:", universe) {

      public void invoke(final Frame frame, VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger index = (SInteger) frame.pop();
        SInteger indexT = (SInteger) StackUtils.pop(truffleFrame);

        assert index == indexT : "indexes differ";

        SArray self = (SArray) frame.pop();
        SArray selfT = (SArray) StackUtils.pop(truffleFrame);

        assert self == selfT : "Arrays differ";

        SAbstractObject fields = self.getIndexableField(index.getEmbeddedInteger() - 1);
        SAbstractObject fieldsT = selfT.getIndexableField(indexT.getEmbeddedInteger() - 1);

        assert fields == fieldsT : "Fields differ";

        StackUtils.push(truffleFrame, fields);
        frame.push(fieldsT);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different, after";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ, after";

      }
    });

    installInstancePrimitive(new SPrimitive("at:put:", universe) {

      public void invoke(final Frame frame, VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject value = frame.pop();
        SInteger index = (SInteger) frame.pop();
        SArray self = (SArray) frame.getStackElement(0);
        self.setIndexableField(index.getEmbeddedInteger() - 1, value);

        SAbstractObject valueT = StackUtils.pop(truffleFrame);
        SInteger indexT = (SInteger) StackUtils.pop(truffleFrame);
        SArray selfT = (SArray) StackUtils.getRelativeStackElement(truffleFrame, 0);
        selfT.setIndexableField(indexT.getEmbeddedInteger() - 1, valueT);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installInstancePrimitive(new SPrimitive("length", universe) {

      public void invoke(final Frame frame, VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SArray self = (SArray) frame.pop();
        SArray selfT = (SArray) StackUtils.pop(truffleFrame);

        assert self == selfT : "values differ";
        SInteger fields = universe.newInteger(self.getNumberOfIndexableFields());

        frame.push(fields);
        StackUtils.push(truffleFrame, fields);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });

    installClassPrimitive(new SPrimitive("new:", universe) {

      public void invoke(final Frame frame, VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SInteger length = (SInteger) frame.pop();
        SInteger lengthT = (SInteger) StackUtils.pop(truffleFrame);
        assert length.equals(lengthT) : "length are different";

        frame.pop(); // not required
        StackUtils.pop(truffleFrame);

        SArray array = universe.newArray(length.getEmbeddedInteger());

        frame.push(array);
        StackUtils.push(truffleFrame, array);

        assert StackUtils.areStackEqual(truffleFrame,
            frame) : "Stack are different";
        assert StackUtils.getCurrentStackPointer(
            truffleFrame) == frame.getStackPointer() : "Stack pointers differ";
      }
    });
  }
}

/**
 * Copyright (c) 2016 Michael Haupt, github@haupz.de
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


public class ObjectPrimitives extends Primitives {

  public ObjectPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {

    installInstancePrimitive(new SPrimitive("==", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject op1 = StackUtils.pop(truffleFrame);
        SAbstractObject op2 = StackUtils.pop(truffleFrame);

        if (op1 == op2) {
          StackUtils.push(truffleFrame, universe.trueObject);

        } else {
          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("hashcode", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject self = StackUtils.pop(truffleFrame);

        SInteger hashCode = universe.newInteger(self.hashCode());

        StackUtils.push(truffleFrame, hashCode);
      }
    });

    installInstancePrimitive(new SPrimitive("objectSize", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject self = StackUtils.pop(truffleFrame);

        // each object holds its class as an implicit member that contributes to its size
        int size = 1;
        if (self instanceof SArray) {
          size += ((SArray) self).getNumberOfIndexableFields();
        }
        if (self instanceof SObject) {
          size += ((SObject) self).getNumberOfFields();
        }

        SInteger value = universe.newInteger(size);

        StackUtils.push(truffleFrame, value);

      }
    });

    installInstancePrimitive(new SPrimitive("perform:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject arg = StackUtils.pop(truffleFrame);
        SAbstractObject self = StackUtils.getRelativeStackElement(truffleFrame, 0);
        SSymbol selector = (SSymbol) arg;

        SInvokable invokable = self.getSOMClass(universe).lookupInvokable(selector);
        invokable.indirectInvoke(truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("perform:inSuperclass:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject arg2 = StackUtils.pop(truffleFrame);
        SAbstractObject arg = StackUtils.pop(truffleFrame);

        SSymbol selector = (SSymbol) arg;
        SClass clazz = (SClass) arg2;

        SInvokable invokable = clazz.lookupInvokable(selector);

        invokable.indirectInvoke(truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("perform:withArguments:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject arg2 = StackUtils.pop(truffleFrame);
        SAbstractObject arg = StackUtils.pop(truffleFrame);
        SAbstractObject self = StackUtils.getRelativeStackElement(truffleFrame, 0);

        SSymbol selector = (SSymbol) arg;
        SArray args = (SArray) arg2;

        for (int i = 0; i < args.getNumberOfIndexableFields(); i++) {
          StackUtils.push(truffleFrame, args.getIndexableField(i));
        }

        SInvokable invokable = self.getSOMClass(universe).lookupInvokable(selector);
        invokable.indirectInvoke(truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("instVarAt:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject arg = StackUtils.pop(truffleFrame);
        SObject self = (SObject) StackUtils.pop(truffleFrame);
        SInteger idx = (SInteger) arg;

        StackUtils.push(truffleFrame, self.getField(idx.getEmbeddedInteger() - 1));
      }
    });

    installInstancePrimitive(new SPrimitive("instVarAt:put:", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject val = StackUtils.pop(truffleFrame);
        SAbstractObject arg = StackUtils.pop(truffleFrame);
        SObject self = (SObject) StackUtils.getRelativeStackElement(truffleFrame, 0);

        SInteger idx = (SInteger) arg;

        self.setField(idx.getEmbeddedInteger() - 1, val);
      }
    });

    installInstancePrimitive(new SPrimitive("class", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject self = StackUtils.pop(truffleFrame);

        StackUtils.push(truffleFrame, self.getSOMClass(universe));

      }
    });

    installInstancePrimitive(new SPrimitive("halt", universe) {
      @Override
      public void invoke(final VirtualFrame truffleFrame,
          final Interpreter interpreter) {
        Universe.errorPrintln("BREAKPOINT");
      }
    });
  }
}

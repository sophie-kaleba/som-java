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

import som.interpreter.Frame;
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
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject op1 = frame.pop();
        SAbstractObject op2 = frame.pop();

        SAbstractObject op1t = StackUtils.pop(truffleFrame);
        SAbstractObject op2t = StackUtils.pop(truffleFrame);

        assert op1 == op1t;
        assert op2 == op2t;

        if (op1 == op2) {
          frame.push(universe.trueObject);
          StackUtils.push(truffleFrame, universe.trueObject);

        } else {
          frame.push(universe.falseObject);
          StackUtils.push(truffleFrame, universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("hashcode", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject self = frame.pop();
        SAbstractObject selfT = StackUtils.pop(truffleFrame);

        assert self == selfT : "objects differ";

        SInteger hashCode = universe.newInteger(self.hashCode());

        frame.push(hashCode);
        StackUtils.push(truffleFrame, hashCode);
      }
    });

    installInstancePrimitive(new SPrimitive("objectSize", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject self = frame.pop();
        SAbstractObject selfT = StackUtils.pop(truffleFrame);

        assert self == selfT;

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
        frame.push(value);
      }
    });

    installInstancePrimitive(new SPrimitive("perform:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject arg = frame.pop();
        SAbstractObject self = frame.getStackElement(0);
        SSymbol selector = (SSymbol) arg;

        SAbstractObject argT = StackUtils.pop(truffleFrame);
        SAbstractObject selfT = StackUtils.getRelativeStackElement(truffleFrame, 0);
        SAbstractObject selectorT = (SSymbol) argT;

        assert arg == argT;
        assert self == selfT;
        assert selector == selectorT;

        SInvokable invokable = self.getSOMClass(universe).lookupInvokable(selector);
        // TODO fix - just pass along the truffleFrame?
        invokable.indirectInvoke(frame, truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("perform:inSuperclass:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject arg2 = frame.pop();
        SAbstractObject arg = frame.pop();
        // Object self = frame.getStackElement(0);

        SAbstractObject arg2T = StackUtils.pop(truffleFrame);
        SAbstractObject argT = StackUtils.pop(truffleFrame);

        assert arg2 == arg2T;
        assert arg == argT;

        SSymbol selector = (SSymbol) arg;
        SClass clazz = (SClass) arg2;

        SSymbol selectorT = (SSymbol) argT;
        SClass clazzT = (SClass) arg2T;

        assert selector == selectorT;
        assert clazz == clazzT;

        SInvokable invokable = clazz.lookupInvokable(selector);
        // TODO fix - just pass along the truffleFrame?
        invokable.indirectInvoke(frame, truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("perform:withArguments:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject arg2 = frame.pop();
        SAbstractObject arg = frame.pop();
        SAbstractObject self = frame.getStackElement(0);

        SAbstractObject arg2T = StackUtils.pop(truffleFrame);
        SAbstractObject argT = StackUtils.pop(truffleFrame);
        SAbstractObject selfT = StackUtils.getRelativeStackElement(truffleFrame, 0);

        assert arg == argT;
        assert arg2 == arg2T;
        assert self == selfT;

        SSymbol selector = (SSymbol) arg;
        SArray args = (SArray) arg2;

        SSymbol selectorT = (SSymbol) argT;
        SAbstractObject argsT = (SArray) arg2T;
        assert selector == selectorT;
        assert args == argsT;

        for (int i = 0; i < args.getNumberOfIndexableFields(); i++) {
          frame.push(args.getIndexableField(i));
          StackUtils.push(truffleFrame, args.getIndexableField(i));
        }

        SInvokable invokable = self.getSOMClass(universe).lookupInvokable(selector);
        // TODO fix - just pass along the truffleFrame?
        invokable.indirectInvoke(frame, truffleFrame, interpreter);
      }
    });

    installInstancePrimitive(new SPrimitive("instVarAt:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject arg = frame.pop();
        SObject self = (SObject) frame.pop();
        SInteger idx = (SInteger) arg;

        SAbstractObject argT = StackUtils.pop(truffleFrame);
        SObject selfT = (SObject) StackUtils.pop(truffleFrame);
        SInteger idxT = (SInteger) argT;

        assert arg == argT;
        assert self == selfT;
        assert idx == idxT;

        frame.push(self.getField(idx.getEmbeddedInteger() - 1));
        StackUtils.push(truffleFrame, selfT.getField(idxT.getEmbeddedInteger() - 1));
      }
    });

    installInstancePrimitive(new SPrimitive("instVarAt:put:", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject val = frame.pop();
        SAbstractObject arg = frame.pop();
        SObject self = (SObject) frame.getStackElement(0);

        SAbstractObject valT = StackUtils.pop(truffleFrame);
        SAbstractObject argT = StackUtils.pop(truffleFrame);
        SObject selfT = (SObject) StackUtils.getRelativeStackElement(truffleFrame, 0);

        assert val == valT;
        assert arg == argT;
        assert self == selfT;

        SInteger idx = (SInteger) arg;
        SInteger idxT = (SInteger) argT;
        assert idx == idxT;

        self.setField(idx.getEmbeddedInteger() - 1, val);
        selfT.setField(idx.getEmbeddedInteger() - 1, val);
      }
    });

    installInstancePrimitive(new SPrimitive("class", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SAbstractObject self = frame.pop();
        SAbstractObject selfT = StackUtils.pop(truffleFrame);

        assert self == selfT;

        StackUtils.push(truffleFrame, self.getSOMClass(universe));
        frame.push(self.getSOMClass(universe));
      }
    });

    installInstancePrimitive(new SPrimitive("halt", universe) {
      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) {
        Universe.errorPrintln("BREAKPOINT");
      }
    });
  }
}

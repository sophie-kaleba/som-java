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

import som.compiler.ProgramDefinitionError;
import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SInteger;
import som.vmobjects.SPrimitive;
import som.vmobjects.SString;
import som.vmobjects.SSymbol;


public class SystemPrimitives extends Primitives {

  public SystemPrimitives(final Universe universe) {
    super(universe);
  }

  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("load:", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SSymbol argument = (SSymbol) StackUtils.pop(frame);
        StackUtils.pop(frame); // TODO - not required? why?

        SClass result = null;

        try {
          result = universe.loadClass(argument);
        } catch (ProgramDefinitionError e) {
          universe.errorExit(e.toString());
        }
        StackUtils.push(frame, result != null ? result : universe.nilObject);
      }
    });

    installInstancePrimitive(new SPrimitive("exit:", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SInteger error = (SInteger) StackUtils.pop(frame);

        universe.exit(error.getEmbeddedInteger());
      }
    });

    installInstancePrimitive(new SPrimitive("global:", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SSymbol argument = (SSymbol) StackUtils.pop(frame);
        StackUtils.pop(frame); // TODO - not required? why?

        SAbstractObject result = universe.getGlobal(argument);
        StackUtils.push(frame, result != null ? result : universe.nilObject);
      }
    });

    installInstancePrimitive(new SPrimitive("global:put:", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        SAbstractObject value = StackUtils.pop(frame);
        SSymbol argument = (SSymbol) StackUtils.pop(frame);

        universe.setGlobal(argument, value);
      }
    });

    installInstancePrimitive(new SPrimitive("printString:", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SString argument = (SString) StackUtils.pop(frame);

        Universe.print(argument.getEmbeddedString());
      }
    });

    installInstancePrimitive(new SPrimitive("printNewline", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) {
        Universe.println("");
      }
    });

    startMicroTime = System.nanoTime() / 1000L;
    startTime = startMicroTime / 1000L;
    installInstancePrimitive(new SPrimitive("time", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        StackUtils.pop(frame); // ignore TODO- why?

        int time = (int) (System.currentTimeMillis() - startTime);
        SInteger value = universe.newInteger(time);

        StackUtils.push(frame, value);

      }
    });

    installInstancePrimitive(new SPrimitive("ticks", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        StackUtils.pop(frame);

        int time = (int) (System.nanoTime() / 1000L - startMicroTime);
        SInteger value = universe.newInteger(time);

        StackUtils.push(frame, value);

      }
    });

    installInstancePrimitive(new SPrimitive("fullGC", universe) {

      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {

        StackUtils.pop(frame);
        System.gc();
        StackUtils.push(frame, universe.trueObject);

      }
    });

  }

  private long startTime;
  private long startMicroTime;
}

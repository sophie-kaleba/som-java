package som.primitives;

import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.SPrimitive;


public class PrimitivePrimitives extends Primitives {
  public PrimitivePrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("holder", universe) {

      @Override
      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) {
        SPrimitive self = (SPrimitive) StackUtils.pop(frame);
        StackUtils.push(frame, self.getHolder());
      }
    });

    installInstancePrimitive(new SPrimitive("signature", universe) {

      @Override
      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) {
        SPrimitive self = (SPrimitive) StackUtils.pop(frame);
        StackUtils.push(frame, self.getSignature());
      }
    });
  }
}

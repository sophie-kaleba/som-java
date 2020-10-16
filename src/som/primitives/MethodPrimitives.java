package som.primitives;

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.SClass;
import som.vmobjects.SMethod;
import som.vmobjects.SPrimitive;
import som.vmobjects.SSymbol;


public class MethodPrimitives extends Primitives {
  public MethodPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("holder", universe) {

      @Override
      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SMethod self = (SMethod) StackUtils.pop(frame);

        SClass sclass = self.getHolder();

        StackUtils.push(frame, sclass);
      }
    });

    installInstancePrimitive(new SPrimitive("signature", universe) {

      @Override
      public void invoke(final VirtualFrame frame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SMethod self = (SMethod) StackUtils.pop(frame);

        SSymbol signature = self.getSignature();

        StackUtils.push(frame, signature);
      }
    });
  }
}

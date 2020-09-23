package som.primitives;

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Frame;
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
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SMethod self = (SMethod) frame.pop();
        SMethod selfT = (SMethod) StackUtils.pop(truffleFrame);

        assert self == selfT;

        SClass sclass = self.getHolder();

        frame.push(sclass);
        StackUtils.push(truffleFrame, sclass);
      }
    });

    installInstancePrimitive(new SPrimitive("signature", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SMethod self = (SMethod) frame.pop();
        SMethod selfT = (SMethod) StackUtils.pop(truffleFrame);

        assert self == selfT : "objects differ";

        SSymbol signature = self.getSignature();

        frame.push(signature);
        StackUtils.push(truffleFrame, signature);
      }
    });
  }
}

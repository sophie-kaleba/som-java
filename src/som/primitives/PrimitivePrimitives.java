package som.primitives;

import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.interpreter.Frame;
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
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SPrimitive self = (SPrimitive) frame.pop();
        SPrimitive selfT = (SPrimitive) StackUtils.pop(truffleFrame);

        assert self == selfT;

        StackUtils.push(truffleFrame, self.getHolder());
        frame.push(self.getHolder());
      }
    });

    installInstancePrimitive(new SPrimitive("signature", universe) {

      @Override
      public void invoke(final Frame frame, final VirtualFrame truffleFrame,
          final Interpreter interpreter) throws FrameSlotTypeException {
        SPrimitive self = (SPrimitive) frame.pop();
        SPrimitive selfT = (SPrimitive) StackUtils.pop(truffleFrame);

        assert self == selfT;

        StackUtils.push(truffleFrame, self.getSignature());
        frame.push(self.getSignature());
      }
    });
  }
}

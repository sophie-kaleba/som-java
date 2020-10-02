package som.interpreter;

import com.oracle.truffle.api.nodes.ControlFlowException;

import som.vmobjects.SAbstractObject;


public class ReturnException extends ControlFlowException {

  private static final long        serialVersionUID = 6317360573982456256L;
  private final SAbstractObject    result;
  private final FrameOnStackMarker outerContextMarker;

  public ReturnException(SAbstractObject result, FrameOnStackMarker target) {
    this.result = result;
    this.outerContextMarker = target;
  }

  public SAbstractObject getResult() {
    return this.result;
  }

  public FrameOnStackMarker getTarget() {
    return this.outerContextMarker;
  }

  public boolean hasReachedTarget(FrameOnStackMarker currentMarker) {
    return currentMarker == this.outerContextMarker;
  }
}

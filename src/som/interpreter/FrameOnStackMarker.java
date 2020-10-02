package som.interpreter;

/**
 * See equivalent in TruffleSOM
 * 
 * The FrameOnStackMarker is a marker to represent the identity of frames, and
 * to keep track on the status of whether the frame is still on the stack.
 * Currently it is used to implement non-local returns by marking
 * stack frames with it, and checking for the marker during unwinding.
 *
 * @author Stefan Marr
 */
public final class FrameOnStackMarker {
  private boolean isOnStack;

  public FrameOnStackMarker() {
    isOnStack = true;
  }

  public void frameNoLongerOnStack() {
    isOnStack = false;
  }

  public boolean isOnStack() {
    return isOnStack;
  }
}

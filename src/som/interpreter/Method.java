package som.interpreter;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;

import som.compiler.ProgramDefinitionError;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SMethod;


public final class Method extends Invokable {

  @CompilationFinal(dimensions = 1) final byte[] bytecodes;
  private final SMethod                          method;
  public final FrameSlot                         executionStackSlot;
  public final FrameSlot                         stackPointerSlot;
  public final FrameSlot                         frameOnStackMarkerSlot;

  public Method(final TruffleLanguage<?> language, final int numberOfBytecodes,
      final SMethod method, final FrameDescriptor frameDescriptor,
      final FrameSlot executionStackSlot, final FrameSlot stackPointerSlot,
      final FrameSlot onStackSlot) {
    super(language, frameDescriptor);
    this.bytecodes = new byte[numberOfBytecodes];
    this.method = method;
    this.executionStackSlot = executionStackSlot;
    this.stackPointerSlot = stackPointerSlot;
    this.frameOnStackMarkerSlot = onStackSlot;
  }

  @Override
  public Object execute(final VirtualFrame frame)
      throws ReturnException {
    Interpreter interpreter = (Interpreter) frame.getArguments()[0];

    StackUtils.initializeStackSlots(frame, this.executionStackSlot, this.method);
    FrameOnStackMarker marker =
        StackUtils.initializeStackMarkerSlot(frame, this.frameOnStackMarkerSlot);

    while (true) {
      try {
        SAbstractObject result = interpreter.start(frame, this.method);
        return result;
      } catch (ReturnException e) {
        if (e.hasReachedTarget(marker)) {
          SAbstractObject result = e.getResult();
          return result;
        }
        marker.frameNoLongerOnStack();
        throw e;
      } catch (ProgramDefinitionError | FrameSlotTypeException exception) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        exception.printStackTrace();
        System.exit(1);
      } finally {
        marker.frameNoLongerOnStack();
      }
    }
  }

  public int getNumberOfBytecodes() {
    // Get the number of bytecodes in this method
    return bytecodes.length;
  }

  public byte getBytecode(final int index) {
    // Get the bytecode at the given index
    return bytecodes[index];
  }

  public void setBytecode(final int index, final byte value) {
    // Set the bytecode at the given index to the given value
    bytecodes[index] = value;
  }

  public byte[] getBytecodes() {
    return this.bytecodes;
  }

  @Override
  public String toString() {
    return method.toString();
  }
}

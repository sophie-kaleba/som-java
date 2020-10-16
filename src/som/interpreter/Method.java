package som.interpreter;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.profiles.ValueProfile;

import som.GraalSOMLanguage;
import som.compiler.ProgramDefinitionError;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SMethod;


public final class Method extends Invokable {

  @CompilationFinal(dimensions = 1) final byte[] bytecodes;
  private final SMethod                          method;

  private final @Children DirectCallNode[] inlineCacheDirectCallNodes;

  private final @CompilationFinal(dimensions = 1) SClass[]       inlineCacheClass;
  private final @CompilationFinal(dimensions = 1) SInvokable[]   inlineCacheInvokable;
  private final @CompilationFinal(dimensions = 1) ValueProfile[] receiverProfiles;

  public Method(final TruffleLanguage<?> language, final int numberOfBytecodes,
      final SMethod method) {
    super(language, StackUtils.FRAME_DESCRIPTOR);
    this.bytecodes = new byte[numberOfBytecodes];
    this.method = method;
    inlineCacheClass = new SClass[numberOfBytecodes];
    inlineCacheInvokable = new SInvokable[numberOfBytecodes];
    inlineCacheDirectCallNodes = new DirectCallNode[numberOfBytecodes];
    receiverProfiles = new ValueProfile[numberOfBytecodes];
  }

  @Override
  public Object execute(final VirtualFrame frame)
      throws ReturnException {

    StackUtils.initializeStackSlots(frame, this.method);
    FrameOnStackMarker marker =
        StackUtils.initializeStackMarkerSlot(frame);

    while (true) {
      try {
        SAbstractObject result =
            GraalSOMLanguage.getCurrentContext().getInterpreter().start(frame, this.method);
        return result;
      } catch (ReturnException e) {
        if (e.hasReachedTarget(marker)) {
          SAbstractObject result = e.getResult();
          return result;
        }
        throw e;
      } catch (ProgramDefinitionError exception) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        exception.printStackTrace();
        System.exit(1);
      } finally {
        marker.frameNoLongerOnStack();
      }
    }
  }

  public SClass getInlineCacheClass(final int bytecodeIndex) {
    return inlineCacheClass[bytecodeIndex];
  }

  public SInvokable getInlineCacheInvokable(final int bytecodeIndex) {
    return inlineCacheInvokable[bytecodeIndex];
  }

  public DirectCallNode getInlineCacheDirectCallNode(final int bytecodeIndex) {
    CompilerAsserts.partialEvaluationConstant(bytecodeIndex);
    return inlineCacheDirectCallNodes[bytecodeIndex];
  }

  public void setReceiverProfile(final int bytecodeIndex,
      final ValueProfile valueProfile) {
    receiverProfiles[bytecodeIndex] = valueProfile;
  }

  public ValueProfile getReceiverProfile(final int bytecodeIndex) {
    return receiverProfiles[bytecodeIndex];
  }

  public void setInlineCache(final int bytecodeIndex, final SClass receiverClass,
      final SInvokable invokable) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    inlineCacheClass[bytecodeIndex] = receiverClass;
    inlineCacheInvokable[bytecodeIndex] = invokable;
    if (invokable != null) {
      if (invokable.isPrimitive()) {
        inlineCacheDirectCallNodes[bytecodeIndex] = null;
      } else {
        inlineCacheDirectCallNodes[bytecodeIndex] =
            DirectCallNode.create(((SMethod) invokable).getCallTarget());
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

package som.interpreter;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.RootNode;


public abstract class Invokable extends RootNode {

  protected Invokable(TruffleLanguage<?> language) {
    super(language);
  }

  protected Invokable(TruffleLanguage<?> language, FrameDescriptor frameDescriptor) {
    super(language, frameDescriptor);
  }

}

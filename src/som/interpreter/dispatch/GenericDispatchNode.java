package som.interpreter.dispatch;

import java.util.ArrayList;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;

import som.interpreter.Interpreter;
import som.interpreter.StackUtils;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SMethod;
import som.vmobjects.SSymbol;


/**
 * will be responsible for the sends
 * will hold a cache for methods
 */
public class GenericDispatchNode {

  private final int MAX_CACHE_SIZE = 6;

  // TODO - for now, unlimited cache size
  // turn them into arrays?
  // TODO - should be turned into array or else to get optimized
  private final @CompilationFinal ArrayList<SClass>         inlineCacheClasses;
  private final @CompilationFinal ArrayList<SInvokable>     inlineCacheInvokables;
  private final @CompilationFinal ArrayList<DirectCallNode> inlineCacheDirectCallNodes;

  private final Universe    universe;
  private final Interpreter interpreter;

  public GenericDispatchNode(final Universe universe) {
    inlineCacheClasses = new ArrayList<>();
    inlineCacheInvokables = new ArrayList<>();
    inlineCacheDirectCallNodes = new ArrayList<>();
    this.universe = universe;
    this.interpreter = universe.getInterpreter();
  }

  public void send(final SSymbol selector, final SClass receiverClass,
      final VirtualFrame frame,
      final SMethod method) {
    // First try the inline cache
    SInvokable invokableWithoutCacheHit = null;

    for (int i = 0; i < inlineCacheClasses.size(); i++) {
      SClass cachedClass = inlineCacheClasses.get(i);
      if (cachedClass == receiverClass) {
        SInvokable invokable = inlineCacheInvokables.get(i);
        if (invokable != null) {
          DirectCallNode invokableDirectCallNode =
              inlineCacheDirectCallNodes.get(i);
          CompilerAsserts.partialEvaluationConstant(invokableDirectCallNode);
          invokable.directInvoke(frame, interpreter, invokableDirectCallNode);
          return;
        }
      }
    }
    CompilerDirectives.transferToInterpreterAndInvalidate();
    // Lookup the invokable with the given signature
    invokableWithoutCacheHit = receiverClass.lookupInvokable(selector);
    setInlineCache(receiverClass, invokableWithoutCacheHit);

    invokeWithoutCacheHit(selector, frame, invokableWithoutCacheHit);
  }

  private void invokeWithoutCacheHit(SSymbol selector, VirtualFrame frame,
      SInvokable invokableWithoutCacheHit) {
    if (invokableWithoutCacheHit != null) {
      invokableWithoutCacheHit.indirectInvoke(frame, interpreter);
    } else {
      int numberOfArguments = selector.getNumberOfSignatureArguments();

      SAbstractObject receiver =
          StackUtils.getRelativeStackElement(frame, numberOfArguments - 1);

      receiver.sendDoesNotUnderstand(selector, universe, interpreter, frame);
    }
  }

  private void setInlineCache(final SClass receiverClass,
      final SInvokable invokable) {
    CompilerDirectives.transferToInterpreterAndInvalidate();
    inlineCacheClasses.add(receiverClass);
    inlineCacheInvokables.add(invokable);
    if (invokable != null) {
      if (invokable.isPrimitive()) {
        inlineCacheDirectCallNodes.add(null);
      } else {
        inlineCacheDirectCallNodes.add(
            DirectCallNode.create(((SMethod) invokable).getCallTarget()));
      }
    }

  }
}

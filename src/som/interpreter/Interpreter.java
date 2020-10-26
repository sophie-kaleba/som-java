/**
 * Copyright (c) 2017 Michael Haupt, github@haupz.de
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.interpreter;

import static som.interpreter.Bytecodes.DUP;
import static som.interpreter.Bytecodes.HALT;
import static som.interpreter.Bytecodes.POP;
import static som.interpreter.Bytecodes.POP_ARGUMENT;
import static som.interpreter.Bytecodes.POP_FIELD;
import static som.interpreter.Bytecodes.POP_LOCAL;
import static som.interpreter.Bytecodes.PUSH_ARGUMENT;
import static som.interpreter.Bytecodes.PUSH_BLOCK;
import static som.interpreter.Bytecodes.PUSH_CONSTANT;
import static som.interpreter.Bytecodes.PUSH_FIELD;
import static som.interpreter.Bytecodes.PUSH_GLOBAL;
import static som.interpreter.Bytecodes.PUSH_LOCAL;
import static som.interpreter.Bytecodes.RETURN_LOCAL;
import static som.interpreter.Bytecodes.RETURN_NON_LOCAL;
import static som.interpreter.Bytecodes.SEND;
import static som.interpreter.Bytecodes.SUPER_SEND;
import static som.interpreter.Bytecodes.getBytecodeLength;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.profiles.ValueProfile;

import som.compiler.ProgramDefinitionError;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SMethod;
import som.vmobjects.SObject;
import som.vmobjects.SSymbol;


public class Interpreter {

  private final Universe         universe;
  private final IndirectCallNode indirectCallNode =
      Truffle.getRuntime().createIndirectCallNode();

  public Interpreter(final Universe universe) {
    this.universe = universe;
  }

  private void doDup(final VirtualFrame frame) {
    SAbstractObject value = StackUtils.getRelativeStackElement(frame, 0);

    StackUtils.push(frame, value);
  }

  private void doPushLocal(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {

    SAbstractObject value = StackUtils.getLocal(frame,
        method.getBytecode(bytecodeIndex + 1),
        method.getBytecode(bytecodeIndex + 2));

    StackUtils.push(frame, value);
  }

  private void doPushArgument(final int bytecodeIndex, final VirtualFrame frame,
      final SMethod method) {
    SAbstractObject value =
        StackUtils.getArgument(frame, method.getBytecode(bytecodeIndex + 1),
            method.getBytecode(bytecodeIndex + 2));

    StackUtils.push(frame, value);
  }

  private void doPushField(final int bytecodeIndex,
      final VirtualFrame frame, final SMethod method) {
    int fieldIndex = method.getBytecode(bytecodeIndex + 1);
    SAbstractObject value = ((SObject) getSelf(frame, method)).getField(fieldIndex);

    StackUtils.push(frame, value);
  }

  private void doPushBlock(final int bytecodeIndex,
      final VirtualFrame frame, final SMethod method)
      throws ProgramDefinitionError {

    SMethod blockMethod = (SMethod) method.getConstant(bytecodeIndex);
    SBlock block = universe.newBlock(blockMethod,
        blockMethod.getNumberOfArguments(), frame.materialize());

    StackUtils.push(frame, block);
  }

  private void doPushConstant(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {

    StackUtils.push(frame,
        method.getConstant(bytecodeIndex));
  }

  private void doPushGlobal(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {

    SSymbol globalName = (SSymbol) method.getConstant(bytecodeIndex);
    SAbstractObject global = universe.getGlobal(globalName);

    if (global != null) {
      StackUtils.push(frame, global);

    } else {
      // Send 'unknownGlobal:' to self
      // TODO - reconsider
      CompilerDirectives.transferToInterpreter();
      getSelf(frame, method).sendUnknownGlobal(globalName, universe, this, frame);
    }
  }

  private void doPop(final VirtualFrame frame) {
    StackUtils.pop(frame);
  }

  private void doPopLocal(final int bytecodeIndex, final VirtualFrame frame,
      final SMethod method) {
    SAbstractObject value = StackUtils.pop(frame);

    StackUtils.setLocal(frame,
        method.getBytecode(bytecodeIndex + 1), method.getBytecode(bytecodeIndex + 2),
        value);
  }

  private void doPopArgument(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {

    SAbstractObject value = StackUtils.pop(frame);

    StackUtils.setArgument(frame,
        method.getBytecode(bytecodeIndex + 1), method.getBytecode(bytecodeIndex + 2),
        value);
  }

  private void doPopField(final int bytecodeIndex,
      final VirtualFrame frame, final SMethod method) {
    int fieldIndex = method.getBytecode(bytecodeIndex + 1);

    ((SObject) getSelf(frame, method)).setField(fieldIndex, StackUtils.pop(frame));
  }

  private void doSuperSend(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {
    SSymbol signature = (SSymbol) method.getConstant(bytecodeIndex);

    SClass holderSuper = (SClass) method.getHolder().getSuperClass();
    SInvokable invokable = holderSuper.lookupInvokable(signature);

    if (invokable != null) {
      invokable.indirectInvoke(frame, this);
    } else {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      int numberOfArguments = signature.getNumberOfSignatureArguments();

      SAbstractObject receiver =
          StackUtils.getRelativeStackElement(frame, numberOfArguments - 1);

      receiver.sendDoesNotUnderstand(signature, universe, this, frame);
    }
  }

  private SAbstractObject doReturnLocal(final VirtualFrame frame) {
    return StackUtils.pop(frame);
  }

  private SAbstractObject doReturnNonLocal(final VirtualFrame frame, final SMethod method)
      throws ReturnException {

    SAbstractObject value = StackUtils.pop(frame);

    VirtualFrame context = StackUtils.getContext(frame, method.getContextLevel());

    FrameOnStackMarker marker = StackUtils.getOnStackMarker(context);

    // TODO - when the body was commented, the tests for NLR were still passing, this need
    // further investigation
    if (!marker.isOnStack()) {
      CompilerDirectives.transferToInterpreter();
      // Try to recover by sending 'escapedBlock:' to the sending object
      // this can get a bit nasty when using nested blocks. In this case
      // the "sender" will be the surrounding block and not the object
      // that actually sent the 'value' message.
      SBlock blockT = (SBlock) StackUtils.getCurrentArguments(frame)[0];
      SAbstractObject receiver = (SAbstractObject) StackUtils.getCurrentArguments(context)[0];

      receiver.sendEscapedBlock(blockT, universe, this, frame);
      return StackUtils.pop(frame);

      // TODO - get the sender from truffle frame
      // SAbstractObject sender =
      // frame.getPreviousFrame().getOuterContext(universe.nilObject).getArgument(0, 0);
      //
      // // ... and execute the escapedBlock message instead
      // sender.sendEscapedBlock(blockT, universe, this, frame);
      // return StackUtils.pop(frame);
    }

    // throw the exception to pass around the context and pop the right frames
    throw new ReturnException(value, marker);
  }

  private void doSend(final int bytecodeIndex,
      final VirtualFrame frame,
      final SMethod method) {
    SSymbol signature = (SSymbol) method.getConstant(bytecodeIndex);

    int numberOfArguments = signature.getNumberOfSignatureArguments();
    SAbstractObject receiver = StackUtils.getRelativeStackElement(frame,
        numberOfArguments - 1);

    ValueProfile receiverClassValueProfile =
        method.getMethod().getReceiverProfile(bytecodeIndex);
    if (receiverClassValueProfile == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      receiverClassValueProfile = ValueProfile.createClassProfile();
      method.getMethod().setReceiverProfile(bytecodeIndex, receiverClassValueProfile);
    }

    send(signature,
        receiverClassValueProfile.profile(receiver).getSOMClass(universe),
        bytecodeIndex, frame, method);
  }

  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
  public SAbstractObject start(final VirtualFrame frame,
      final SMethod method)
      throws ReturnException, ProgramDefinitionError {

    int bytecodeIndex = 0;

    while (true) {

      int currentBytecodeIndex = bytecodeIndex;
      byte bytecode = method.getBytecode(currentBytecodeIndex);

      // Compute the next bytecode index
      int bytecodeLength = getBytecodeLength(bytecode);
      bytecodeIndex = currentBytecodeIndex + bytecodeLength;

      CompilerAsserts.partialEvaluationConstant(bytecodeIndex);
      CompilerAsserts.partialEvaluationConstant(currentBytecodeIndex);
      CompilerAsserts.partialEvaluationConstant(method);
      CompilerAsserts.partialEvaluationConstant(bytecode);

      // Handle the current bytecode
      switch (bytecode) {
        case HALT: {
          return StackUtils.getRelativeStackElement(frame, 0);
        }

        case DUP: {
          doDup(frame);
          break;
        }

        case PUSH_LOCAL: {
          doPushLocal(currentBytecodeIndex, frame, method);
          break;
        }

        case PUSH_ARGUMENT: {
          doPushArgument(currentBytecodeIndex, frame, method);
          break;
        }

        case PUSH_FIELD: {
          doPushField(currentBytecodeIndex, frame, method);
          break;
        }

        case PUSH_BLOCK: {
          doPushBlock(currentBytecodeIndex, frame, method);
          break;
        }

        case PUSH_CONSTANT: {
          doPushConstant(currentBytecodeIndex, frame, method);
          break;
        }

        case PUSH_GLOBAL: {
          doPushGlobal(currentBytecodeIndex, frame, method);
          break;
        }

        case POP: {
          doPop(frame);
          break;
        }

        case POP_LOCAL: {
          doPopLocal(currentBytecodeIndex, frame, method);
          break;
        }

        case POP_ARGUMENT: {
          doPopArgument(currentBytecodeIndex, frame, method);
          break;
        }

        case POP_FIELD: {
          doPopField(currentBytecodeIndex, frame, method);
          break;
        }

        case SEND: {
          try {
            doSend(currentBytecodeIndex, frame, method);
          } catch (RestartLoopException e) {
            bytecodeIndex = 0;
            StackUtils.resetStackPointer(frame, method);
          }
          break;
        }

        case SUPER_SEND: {
          try {
            doSuperSend(currentBytecodeIndex, frame, method);
          } catch (RestartLoopException e) {
            bytecodeIndex = 0;
            StackUtils.resetStackPointer(frame, method);
          }
          break;
        }

        case RETURN_LOCAL: {
          return doReturnLocal(frame);
        }

        case RETURN_NON_LOCAL: {
          return doReturnNonLocal(frame, method);
        }

        default:
          Universe.errorPrintln("Nasty bug in interpreter");
          break;
      }
    }
  }

  public SAbstractObject getSelf(final VirtualFrame frame, final SMethod method) {
    // Get the self object from the interpreter
    VirtualFrame outerContext = StackUtils.getContext(frame, method.getContextLevel());
    return StackUtils.getArgument(outerContext, 0, 0);
  }

  private void send(final SSymbol selector, final SClass receiverClass,
      final int bytecodeIndex, final VirtualFrame frame,
      final SMethod method) {
    // First try the inline cache
    SInvokable invokableWithoutCacheHit = null;

    SClass cachedClass = method.getMethod().getInlineCacheClass(bytecodeIndex);
    if (cachedClass == receiverClass) {
      SInvokable invokable = method.getMethod().getInlineCacheInvokable(bytecodeIndex);
      if (invokable != null) {
        DirectCallNode invokableDirectCallNode =
            method.getMethod().getInlineCacheDirectCallNode(bytecodeIndex);
        CompilerAsserts.partialEvaluationConstant(invokableDirectCallNode);
        invokable.directInvoke(frame, this, invokableDirectCallNode);
        return;
      }
    } else {
      if (cachedClass == null) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        // Lookup the invokable with the given signature
        invokableWithoutCacheHit = receiverClass.lookupInvokable(selector);
        method.getMethod().setInlineCache(bytecodeIndex, receiverClass,
            invokableWithoutCacheHit);
      } else {
        // the bytecode index after the send is used by the selector constant, and can be used
        // safely as another cache item
        cachedClass = method.getMethod().getInlineCacheClass(bytecodeIndex + 1);
        if (cachedClass == receiverClass) {
          SInvokable invokable = method.getMethod().getInlineCacheInvokable(bytecodeIndex + 1);
          if (invokable != null) {
            DirectCallNode invokableDirectCallNode =
                method.getMethod().getInlineCacheDirectCallNode(bytecodeIndex + 1);
            CompilerAsserts.partialEvaluationConstant(invokableDirectCallNode);
            invokable.directInvoke(frame, this, invokableDirectCallNode);
            return;
          }
        } else {
          CompilerDirectives.transferToInterpreterAndInvalidate();
          invokableWithoutCacheHit = receiverClass.lookupInvokable(selector);
          if (cachedClass == null) {
            method.getMethod().setInlineCache(bytecodeIndex + 1, receiverClass,
                invokableWithoutCacheHit);
          }
        }
      }
    }
    CompilerDirectives.transferToInterpreterAndInvalidate();
    invokeWithoutCacheHit(selector, frame, invokableWithoutCacheHit);
  }

  private void invokeWithoutCacheHit(SSymbol selector, VirtualFrame frame,
      SInvokable invokableWithoutCacheHit) {
    if (invokableWithoutCacheHit != null) {
      invokableWithoutCacheHit.indirectInvoke(frame, this);
    } else {
      int numberOfArguments = selector.getNumberOfSignatureArguments();

      SAbstractObject receiver =
          StackUtils.getRelativeStackElement(frame, numberOfArguments - 1);

      receiver.sendDoesNotUnderstand(selector, universe, this, frame);
    }
  }

  public IndirectCallNode getIndirectCallNode() {
    return indirectCallNode;
  }
}

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

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;

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

  private final Universe universe;

  public Interpreter(final Universe universe) {
    this.universe = universe;
  }

  private void doDup(Frame frame) {
    // Handle the DUP bytecode
    frame.push(frame.getStackElement(0));
  }

  private void doPushLocal(final int bytecodeIndex, Frame frame) {
    // Handle the PUSH LOCAL bytecode
    frame.push(
        frame.getLocal(getMethod(frame).getBytecode(bytecodeIndex + 1),
            getMethod(frame).getBytecode(bytecodeIndex + 2)));
  }

  private void doPushArgument(final int bytecodeIndex, Frame frame) {
    // Handle the PUSH ARGUMENT bytecode
    frame.push(
        frame.getArgument(getMethod(frame).getBytecode(bytecodeIndex + 1),
            getMethod(frame).getBytecode(bytecodeIndex + 2)));
  }

  private void doPushField(final int bytecodeIndex, Frame frame) {
    // Handle the PUSH FIELD bytecode
    int fieldIndex = getMethod(frame).getBytecode(bytecodeIndex + 1);

    // Push the field with the computed index onto the stack
    frame.push(((SObject) getSelf(frame)).getField(fieldIndex));
  }

  private void doPushBlock(final int bytecodeIndex, Frame frame) throws ProgramDefinitionError {
    // Handle the PUSH BLOCK bytecode
    SMethod blockMethod = (SMethod) getMethod(frame).getConstant(bytecodeIndex);

    // Push a new block with the current getFrame() as context onto the
    // stack
    frame.push(
        universe.newBlock(blockMethod, frame,
            blockMethod.getNumberOfArguments()));
  }

  private void doPushConstant(final int bytecodeIndex, Frame frame) {
    // Handle the PUSH CONSTANT bytecode
    frame.push(getMethod(frame).getConstant(bytecodeIndex));
  }

  private void doPushGlobal(final int bytecodeIndex, Frame frame) {
    // Handle the PUSH GLOBAL bytecode
    SSymbol globalName = (SSymbol) getMethod(frame).getConstant(bytecodeIndex);

    // Get the global from the universe
    SAbstractObject global = universe.getGlobal(globalName);

    if (global != null) {
      // Push the global onto the stack
      frame.push(global);
    } else {
      // Send 'unknownGlobal:' to self
      getSelf(frame).sendUnknownGlobal(globalName, universe, this, frame);
    }
  }

  private void doPop(Frame frame) {
    // Handle the POP bytecode
    frame.pop();
  }

  private void doPopLocal(final int bytecodeIndex, Frame frame) {
    // Handle the POP LOCAL bytecode
    frame.setLocal(getMethod(frame).getBytecode(bytecodeIndex + 1),
        getMethod(frame).getBytecode(bytecodeIndex + 2), frame.pop());
  }

  private void doPopArgument(final int bytecodeIndex, Frame frame) {
    // Handle the POP ARGUMENT bytecode
    frame.setArgument(getMethod(frame).getBytecode(bytecodeIndex + 1),
        getMethod(frame).getBytecode(bytecodeIndex + 2), frame.pop());
  }

  private void doPopField(final int bytecodeIndex, Frame frame) {
    // Handle the POP FIELD bytecode
    int fieldIndex = getMethod(frame).getBytecode(bytecodeIndex + 1);

    // Set the field with the computed index to the value popped from the stack
    ((SObject) getSelf(frame)).setField(fieldIndex, frame.pop());
  }

  private void doSuperSend(final int bytecodeIndex, Frame frame) {
    // Handle the SUPER SEND bytecode
    SSymbol signature = (SSymbol) getMethod(frame).getConstant(bytecodeIndex);

    // Send the message
    // Lookup the invokable with the given signature
    SClass holderSuper = (SClass) getMethod(frame).getHolder().getSuperClass();
    SInvokable invokable = holderSuper.lookupInvokable(signature);

    if (invokable != null) {
      // Invoke the invokable in the current frame
      invokable.invoke(frame, this);
    } else {
      // Compute the number of arguments
      int numberOfArguments = signature.getNumberOfSignatureArguments();

      // Compute the receiver
      SAbstractObject receiver = frame.getStackElement(numberOfArguments - 1);

      receiver.sendDoesNotUnderstand(signature, universe, this, frame);
    }
  }

  private SAbstractObject doReturnLocal(Frame frame) {
    // Handle the RETURN LOCAL bytecode
    return frame.pop();
  }

  private SAbstractObject doReturnNonLocal(Frame frame) throws ReturnException {
    // Handle the RETURN NON LOCAL bytecode
    SAbstractObject result = frame.pop();

    // Compute the context for the non-local return
    Frame context = frame.getOuterContext(universe.nilObject);

    // Make sure the block context is still on the stack
    if (!context.hasPreviousFrame(universe.nilObject)) {
      // Try to recover by sending 'escapedBlock:' to the sending object
      // this can get a bit nasty when using nested blocks. In this case
      // the "sender" will be the surrounding block and not the object
      // that actually sent the 'value' message.
      SBlock block = (SBlock) frame.getArgument(0, 0);
      SAbstractObject sender =
          frame.getPreviousFrame().getOuterContext(universe.nilObject).getArgument(0, 0);

      // ... and execute the escapedBlock message instead
      sender.sendEscapedBlock(block, universe, this, frame);
      return frame.pop();
    }

    // throw the exception to pass around the context and pop the right frames
    throw new ReturnException(result, context);
  }

  private void doSend(final int bytecodeIndex, Frame frame) {
    // Handle the SEND bytecode
    SSymbol signature = (SSymbol) getMethod(frame).getConstant(bytecodeIndex);

    // Get the number of arguments from the signature
    int numberOfArguments = signature.getNumberOfSignatureArguments();

    // Get the receiver from the stack
    SAbstractObject receiver = frame.getStackElement(numberOfArguments - 1);

    // Send the message
    send(signature, receiver.getSOMClass(universe), bytecodeIndex, frame);
  }

  @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
  public SAbstractObject start(Frame frame) throws ReturnException, ProgramDefinitionError {

    // Iterate through the bytecodes
    while (true) {

      SMethod currentMethod = getMethod(frame);

      // Get the current bytecode index
      int bytecodeIndex = frame.getBytecodeIndex();

      // Get the current bytecode
      byte bytecode = currentMethod.getBytecode(bytecodeIndex);

      // Get the length of the current bytecode
      int bytecodeLength = getBytecodeLength(bytecode);

      // Compute the next bytecode index
      int nextBytecodeIndex = bytecodeIndex + bytecodeLength;

      // Update the bytecode index of the frame
      frame.setBytecodeIndex(nextBytecodeIndex);

      // Handle the current bytecode
      switch (bytecode) {

        case HALT: {
          // Handle the HALT bytecode
          return frame.getStackElement(0);
        }

        case DUP: {
          doDup(frame);
          break;
        }

        case PUSH_LOCAL: {
          doPushLocal(bytecodeIndex, frame);
          break;
        }

        case PUSH_ARGUMENT: {
          doPushArgument(bytecodeIndex, frame);
          break;
        }

        case PUSH_FIELD: {
          doPushField(bytecodeIndex, frame);
          break;
        }

        case PUSH_BLOCK: {
          doPushBlock(bytecodeIndex, frame);
          break;
        }

        case PUSH_CONSTANT: {
          doPushConstant(bytecodeIndex, frame);
          break;
        }

        case PUSH_GLOBAL: {
          doPushGlobal(bytecodeIndex, frame);
          break;
        }

        case POP: {
          doPop(frame);
          break;
        }

        case POP_LOCAL: {
          doPopLocal(bytecodeIndex, frame);
          break;
        }

        case POP_ARGUMENT: {
          doPopArgument(bytecodeIndex, frame);
          break;
        }

        case POP_FIELD: {
          doPopField(bytecodeIndex, frame);
          break;
        }

        case SEND: {
          doSend(bytecodeIndex, frame);
          break;
        }

        case SUPER_SEND: {
          doSuperSend(bytecodeIndex, frame);
          break;
        }

        case RETURN_LOCAL: {
          return doReturnLocal(frame);
        }

        case RETURN_NON_LOCAL: {
          return doReturnNonLocal(frame);
        }

        default:
          Universe.errorPrintln("Nasty bug in interpreter");
          break;
      }
    }
  }

  public SMethod getMethod(Frame frame) {
    // Get the method from the interpreter
    return frame.getMethod();
  }

  public SAbstractObject getSelf(Frame frame) {
    // Get the self object from the interpreter
    return frame.getOuterContext(universe.nilObject).getArgument(0, 0);
  }

  private void send(final SSymbol selector, final SClass receiverClass,
      final int bytecodeIndex, Frame frame) throws ReturnException {
    // First try the inline cache
    SInvokable invokable;

    SMethod m = getMethod(frame);
    SClass cachedClass = m.getInlineCacheClass(bytecodeIndex);
    if (cachedClass == receiverClass) {
      invokable = m.getInlineCacheInvokable(bytecodeIndex);
    } else {
      if (cachedClass == null) {
        // Lookup the invokable with the given signature
        invokable = receiverClass.lookupInvokable(selector);
        m.setInlineCache(bytecodeIndex, receiverClass, invokable);
      } else {
        // the bytecode index after the send is used by the selector constant, and can be used
        // safely as another cache item
        cachedClass = m.getInlineCacheClass(bytecodeIndex + 1);
        if (cachedClass == receiverClass) {
          invokable = m.getInlineCacheInvokable(bytecodeIndex + 1);
        } else {
          invokable = receiverClass.lookupInvokable(selector);
          if (cachedClass == null) {
            m.setInlineCache(bytecodeIndex + 1, receiverClass, invokable);
          }
        }
      }
    }

    if (invokable != null) {
      // Invoke the invokable in the current frame
      invokable.invoke(frame, this);
    } else {
      int numberOfArguments = selector.getNumberOfSignatureArguments();

      // Compute the receiver
      SAbstractObject receiver = frame.getStackElement(numberOfArguments - 1);

      receiver.sendDoesNotUnderstand(selector, universe, this, frame);
    }
  }

  public Frame newFrame(Frame prevFrame, SMethod method, Frame context) {
    return this.universe.newFrame(prevFrame, method, context);
  }

  private final IndirectCallNode indirectCallNode = Truffle.getRuntime().createIndirectCallNode();

  public IndirectCallNode getIndirectCallNode() {
    return indirectCallNode;
  }
}

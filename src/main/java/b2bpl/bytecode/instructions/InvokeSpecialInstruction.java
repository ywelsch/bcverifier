package b2bpl.bytecode.instructions;

import b2bpl.bytecode.IInstructionVisitor;
import b2bpl.bytecode.IOpCodes;
import b2bpl.bytecode.JReferenceType;
import b2bpl.bytecode.JType;


public class InvokeSpecialInstruction extends InvokeInstruction {

  private static final String[] RUNTIME_EXCEPTIONS = new String[] {
    "java.lang.NullPointerException"
  };

  public InvokeSpecialInstruction(
      JReferenceType methodOwner,
      String methodName,
      JType returnType,
      JType[] parameterTypes) {
    super(
        IOpCodes.INVOKESPECIAL,
        methodOwner,
        methodName,
        returnType,
        parameterTypes);
  }

  public String[] getRuntimeExceptions() {
    return RUNTIME_EXCEPTIONS;
  }

  public void accept(IInstructionVisitor visitor) {
    visitor.visitInvokeSpecialInstruction(this);
  }
}

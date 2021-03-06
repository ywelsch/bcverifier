package b2bpl.bytecode.instructions;

import b2bpl.bytecode.IInstructionVisitor;
import b2bpl.bytecode.IOpCodes;
import b2bpl.bytecode.JReferenceType;
import b2bpl.bytecode.JType;


public class GetFieldInstruction extends FieldInstruction {

  private static final String[] RUNTIME_EXCEPTIONS = new String[] {
    "java.lang.NullPointerException"
  };

  public GetFieldInstruction(
      JReferenceType fieldOwner,
      String fieldName,
      JType fieldType) {
    super(IOpCodes.GETFIELD, fieldOwner, fieldName, fieldType);
  }

  public String[] getRuntimeExceptions() {
    return RUNTIME_EXCEPTIONS;
  }

  public void accept(IInstructionVisitor visitor) {
    visitor.visitGetFieldInstruction(this);
  }
}

package b2bpl.bytecode.instructions;

import b2bpl.bytecode.IInstructionVisitor;
import b2bpl.bytecode.IOpCodes;
import b2bpl.bytecode.InstructionHandle;


public class IfNonNullInstruction extends AbstractIfInstruction {

  public IfNonNullInstruction(InstructionHandle target) {
    super(IOpCodes.IFNONNULL, target);
  }

  public void accept(IInstructionVisitor visitor) {
    visitor.visitIfNonNullInstruction(this);
  }
}

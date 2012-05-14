package b2bpl.bytecode.instructions;


public abstract class LocalVariableInstruction extends Instruction {

  protected final int index;
  protected String variableName = null;

  public LocalVariableInstruction(int opcode, int index) {
    super(opcode);
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
  
  public void setVariableName(String name){
      this.variableName = name;
  }
  
  public String getVariableName(){
      return variableName;
  }
}

package b2bpl.bytecode;

public class LocalVariableInfo {
	private final int registerIndex;
	private final String variableName;
	
	public LocalVariableInfo(int registerIndex, String variableName) {
		this.registerIndex = registerIndex;
		this.variableName = variableName;
	}
	public int getRegisterIndex() {
		return registerIndex;
	}
	public String getVariableName() {
		return variableName;
	}
	
	
}

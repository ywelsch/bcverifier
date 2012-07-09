package de.unikl.bcverifier.isl.checking.types;

public class ExprTypeInt extends ExprType {

	private static ExprType instance = new ExprTypeInt();

	public static ExprType instance() {
		return instance;
	}
	
	private ExprTypeInt() {}

	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypeInt;
	}
	
	@Override
	public String toString() {
		return "int";
	}
	
}

package de.unikl.bcverifier.isl.checking.types;

public class ExprTypeBool extends ExprType {

	private static ExprType instance = new ExprTypeBool();

	public static ExprType instance() {
		return instance;
	}
	
	private ExprTypeBool() {}

	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypeBool;
	}
	
	@Override
	public String toString() {
		return "boolean";
	}
	
}

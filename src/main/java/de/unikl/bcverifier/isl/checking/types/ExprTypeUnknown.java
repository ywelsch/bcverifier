package de.unikl.bcverifier.isl.checking.types;

public class ExprTypeUnknown extends ExprType {

	private static ExprType instance = new ExprTypeUnknown();

	public static ExprType instance() {
		return instance;
	}
	
	private ExprTypeUnknown() {}

	public boolean isSubtypeOf(ExprType t) {
		return false;
	}
	
	@Override
	public String toString() {
		return "unknown type";
	}
	
}

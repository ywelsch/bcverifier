package de.unikl.bcverifier.isl.checking.types;

public class BijectionType extends ExprType {

	private static ExprType instance = new BijectionType();

	public static ExprType instance() {
		return instance;
	}
	
	private BijectionType() {}

	public boolean isSubtypeOf(ExprType t) {
		return t instanceof BijectionType || t instanceof ExprTypeAny;
	}
	
	@Override
	public String toString() {
		return "bijection";
	}
	
}
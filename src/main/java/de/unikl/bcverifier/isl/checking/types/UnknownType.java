package de.unikl.bcverifier.isl.checking.types;

public class UnknownType extends ExprType {

	private static ExprType instance = new UnknownType();

	public static ExprType instance() {
		return instance;
	}
	
	private UnknownType() {}

	public boolean isSubtypeOf(ExprType t) {
		return false;
	}
	
	@Override
	public String toString() {
		return "unknown type";
	}
	
}

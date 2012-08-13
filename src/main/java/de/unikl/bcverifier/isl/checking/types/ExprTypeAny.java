package de.unikl.bcverifier.isl.checking.types;

/**
 * supertype of all types 
 */
public class ExprTypeAny extends ExprType {

	private static ExprType instance = new ExprTypeAny();

	public static ExprType instance() {
		return instance;
	}
	
	private ExprTypeAny() {}

	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypeAny;
	}
	
	@Override
	public String toString() {
		return "*any*";
	}
	
}

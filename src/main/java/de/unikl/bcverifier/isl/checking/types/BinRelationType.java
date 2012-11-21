package de.unikl.bcverifier.isl.checking.types;

public class BinRelationType extends ExprType {

	private static ExprType instance = new BinRelationType();

	public static ExprType instance() {
		return instance;
	}
	
	private BinRelationType() {}

	public boolean isSubtypeOf(ExprType t) {
		return t instanceof BinRelationType || t instanceof ExprTypeAny;
	}
	
	@Override
	public String toString() {
		return "binrelation";
	}
	
}
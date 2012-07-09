package de.unikl.bcverifier.isl.checking.types;

public abstract class ExprType {

	public abstract boolean isSubtypeOf(ExprType t);
	
	public final boolean typeEquals(ExprType other) {
		return this == other || (other.isSubtypeOf(this) && this.isSubtypeOf(other));
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof ExprType) {
			return typeEquals((ExprType) obj);
		}
		return false;
	}

}

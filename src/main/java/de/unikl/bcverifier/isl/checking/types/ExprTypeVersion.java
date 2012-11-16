package de.unikl.bcverifier.isl.checking.types;

import de.unikl.bcverifier.isl.ast.Version;

public class ExprTypeVersion extends ExprType {

	private Version version;

	private static ExprTypeVersion v_old = new ExprTypeVersion(Version.OLD);
	private static ExprTypeVersion v_new = new ExprTypeVersion(Version.NEW);
	private static ExprTypeVersion v_instance = new ExprTypeVersion(null);
	
	public ExprTypeVersion(Version version) {
		this.version = version;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypeVersion;
	}

	public Version getVersion() {
		return version;
	}

	public static ExprTypeVersion get(Version v) {
		switch (v) {
		case BOTH:
			return v_instance;
		case NEW:
			return v_new;
		case OLD:
			return v_old;
		default:
			throw new Error();
		}
	}
	
	public static ExprTypeVersion instance() {
		return v_instance;
	}
	
}

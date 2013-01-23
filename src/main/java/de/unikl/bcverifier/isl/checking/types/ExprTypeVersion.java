package de.unikl.bcverifier.isl.checking.types;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeVersion extends ExprType {

	private Version version;

	private static ExprTypeVersion v_instance = new ExprTypeVersion(null);
	
	private ExprTypeVersion(Version version) {
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
		return new ExprTypeVersion(v);
	}
	
	public static ExprTypeVersion instance() {
		return v_instance;
	}

}

package de.unikl.bcverifier.isl.checking.types;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeVersion extends ExprType {

	private Version version;
	private TwoLibraryModel env;

	private static ExprTypeVersion v_instance = new ExprTypeVersion(null, null);
	
	private ExprTypeVersion(TwoLibraryModel env, Version version) {
		this.env = env;
		this.version = version;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypeVersion;
	}

	public Version getVersion() {
		return version;
	}

	public static ExprTypeVersion get(TwoLibraryModel env, Version v) {
		return new ExprTypeVersion(env, v);
	}
	
	public static ExprTypeVersion instance() {
		return v_instance;
	}

}

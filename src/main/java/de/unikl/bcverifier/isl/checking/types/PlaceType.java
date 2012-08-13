package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.Version;

public class PlaceType extends ExprType {

	private static final PlaceType instance = new PlaceType(Version.BOTH, null);
	private final Version version;
	private final Statement statement;

	public Version getVersion() {
		return version;
	}

	public Statement getStatement() {
		return statement;
	}

	public PlaceType(Version version, Statement s) {
		this.version = version;
		this.statement = s;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t instanceof PlaceType) {
			PlaceType o = (PlaceType) t;
			return (o.version == Version.BOTH || o.version == version) 
					&& (o.statement == null || o.statement == statement); 
		}
		return false;
	}

	public static PlaceType instance() {
		return instance;
	}

}

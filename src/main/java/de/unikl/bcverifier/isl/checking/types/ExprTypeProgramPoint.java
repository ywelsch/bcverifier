package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.Version;

public class ExprTypeProgramPoint extends ExprType {


	private Version version;
	private Statement statement;
	private int line;

	public ExprTypeProgramPoint(Version version, int line, Statement s) {
		this.version = version;
		this.line = line;
		this.statement = s;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeProgramPoint) {
			ExprTypeProgramPoint other = (ExprTypeProgramPoint) t;
			return true;
		}
		return false;
	}

	public Version getVersion() {
		return version;
	}

	public int getLine() {
		return line;
	}

	public Statement getStatement() {
		return statement;
	}

	
	
}

package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.Version;

public class ExprTypeAtLineProgramPoint extends ExprTypeProgramPoint {


	private Statement statement;
	
	public ExprTypeAtLineProgramPoint(Version version, int line, Statement s) {
		super(version, line);
		this.statement = s;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAtLineProgramPoint) {
			ExprTypeAtLineProgramPoint other = (ExprTypeAtLineProgramPoint) t;
			// TODO
			return true;
		}
		return false;
	}

	public Statement getStatement() {
		return statement;
	}

	
	
}

package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeLocalPlace extends ExprTypePlace {

	private final Version version;
	private final Statement statement;

	

	public Statement getStatement() {
		return statement;
	}


	public ExprTypeLocalPlace(ExprTypeProgramPoint programPoint) {
		this.version = programPoint.getVersion();
		this.statement = programPoint.getStatement();
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t.getClass().equals(ExprTypePlace.class)) {
			return true;
		}
		if (t instanceof ExprTypeLocalPlace) {
			ExprTypeLocalPlace o = (ExprTypeLocalPlace) t;
			return (o.version == Version.BOTH || o.version == version) 
					&& (o.statement == statement); 
		}
		return false;
	}

	@Override
	public int getLineNr() {
		return TwoLibraryModel.getLineNr(getStatement());
	}

	@Override
	public Version getVersion() {
		return version;
	}
	
	@Override
	public ASTNode getASTNode() {
		return statement;
	}
	

}

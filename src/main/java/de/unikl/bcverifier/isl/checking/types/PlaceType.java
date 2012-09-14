package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

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

	public PlaceType(ExprTypeProgramPoint programPoint) {
		this.version = programPoint.getVersion();
		this.statement = programPoint.getStatement();
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

	public int getLineNr() {
		return TwoLibraryModel.getLineNr(getStatement());
	}

	public ITypeBinding getEnclosingClassType() {
		ASTNode node = statement;
		while (node != null) {
			if (node instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) node;
				return typeDeclaration.resolveBinding();
			}
			node = node.getParent();
		}
		return null;
	}

}

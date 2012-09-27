package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.unikl.bcverifier.isl.ast.Version;

public class ExprTypePlace extends ExprType {

	private static final ExprTypePlace instance = new ExprTypePlace();
	
	
	@Override
	public boolean isSubtypeOf(ExprType t) {
		return t instanceof ExprTypePlace;
	}

	public static ExprTypePlace instance() {
		return instance;
	}

	public ASTNode getASTNode() {
		throw new Error("not implemented");
	}

	public Version getVersion() {
		throw new Error("not implemented");
	}

	public ITypeBinding getEnclosingClassType() {
		ASTNode node = getASTNode();
		while (node != null) {
			if (node instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) node;
				return typeDeclaration.resolveBinding();
			}
			node = node.getParent();
		}
		return null;
	}

	public int getLineNr() {
		throw new Error("not implemented");
	}

}

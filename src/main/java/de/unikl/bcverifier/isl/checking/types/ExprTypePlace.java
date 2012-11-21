package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypePlace extends ExprType {

	private static final ExprTypePlace instance = new ExprTypePlace(false, null);
	
	private final ExprTypeProgramPoint pptype;
	private final boolean isLocal;
	
	public ExprTypePlace(boolean isLocal, ExprTypeProgramPoint pptype) {
		this.isLocal = isLocal;
		this.pptype = pptype;
	}

	public static ExprTypePlace instance() {
		return instance;
	}

	public ASTNode getASTNode() {
		if (pptype instanceof ExprTypeCallProgramPoint) {
			ExprTypeCallProgramPoint ppt = (ExprTypeCallProgramPoint)pptype;
			return ppt.getInv();
		} else if (pptype instanceof ExprTypeAtLineProgramPoint) {
			return ((ExprTypeAtLineProgramPoint) pptype).getStatement();
		} else {
			return null;
		}
	}

	public Version getVersion() {
		return pptype.getVersion();
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
		if (pptype instanceof ExprTypeAtLineProgramPoint) {
			return TwoLibraryModel.getLineNr(((ExprTypeAtLineProgramPoint) pptype).getStatement());
		}
		return pptype.getLine();
	}
	
	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t == instance) {
			return true;
		}
		if (t instanceof ExprTypePlace) {
			ExprTypePlace o = (ExprTypePlace) t;
			return isLocal == o.isLocal; 
		}
		return false;
	}

	/**
	 * returns the name of this place as used in the generated boogie file
	 * (e.g. lib1_C.m#int_notifyMe_0) 
	 * @param model 
	 */
	public String getBoogiePlaceName(TwoLibraryModel model) {
		String methodName = ((MethodInvocation)getASTNode()).getName().getFullyQualifiedName();
		return model.getSrc(getVersion()).getBoogiePlaceName(getEnclosingClassType(), getLineNr(), methodName);
	}
	
	public boolean isLocalPlace() {
		return isLocal;
	}
	
	public boolean isCallPlace() {
		return pptype instanceof ExprTypeCallProgramPoint;
	}
}

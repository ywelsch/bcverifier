package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import b2bpl.bytecode.JClassType;

import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypePredefinedPlace extends ExprTypePlace {

	private final Version version;
	private final int line;
	private final MethodInvocation invocation;
	private final TwoLibraryModel model;

	public ExprTypePredefinedPlace(ExprTypeCallProgramPoint programPoint) {
		this.model = programPoint.getModel();
		this.version = programPoint.getVersion();
		this.line = programPoint.getLine();
		this.invocation = programPoint.getInv();
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t.getClass().equals(ExprTypePlace.class)) {
			return true;
		}
		if (t instanceof ExprTypePredefinedPlace) {
			ExprTypePredefinedPlace o = (ExprTypePredefinedPlace) t;
			return (o.version == Version.BOTH || o.version == version) 
					&& (o.invocation == invocation); 
		}
		return false;
	}
	
	@Override
	public Version getVersion() {
		return version;
	}
	
	@Override
	public ASTNode getASTNode() {
		return invocation;
	}

	@Override
	public int getLineNr() {
		return line;
	}

	/**
	 * returns the name of this place as used in the generated boogie file
	 * (e.g. lib1_C.m#int_notifyMe_0) 
	 */
	public String getBoogiePlaceName() {
		String methodName = invocation.getName().getFullyQualifiedName();
		return model.getSrc(getVersion()).getBoogiePlaceName(getEnclosingClassType(), line, methodName);
	}
}

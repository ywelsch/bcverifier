package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.MethodInvocation;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeCallProgramPoint extends ExprTypeProgramPoint {

	private final MethodInvocation inv;
	private final TwoLibraryModel model;

	public MethodInvocation getInv() {
		return inv;
	}

	
	public ExprTypeCallProgramPoint(TwoLibraryModel model, Version version, int line,
			MethodInvocation inv) {
		super(version, line);
		this.model = model;
		this.inv = inv;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeCallProgramPoint) {
			ExprTypeCallProgramPoint other = (ExprTypeCallProgramPoint) t;
			// TODO
			return true;
		}
		return false;
	}

	public TwoLibraryModel getModel() {
		return model;
	}

}

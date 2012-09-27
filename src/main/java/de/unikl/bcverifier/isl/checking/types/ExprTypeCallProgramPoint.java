package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeCallProgramPoint extends ExprType {

	private final Version version; 
	private final int line; 
	private final MethodInvocation inv;
	private final TwoLibraryModel model;
	
	public Version getVersion() {
		return version;
	}

	public int getLine() {
		return line;
	}

	public MethodInvocation getInv() {
		return inv;
	}

	
	public ExprTypeCallProgramPoint(TwoLibraryModel model, Version version, int line,
			MethodInvocation inv) {
		this.model = model;
		this.version = version;
		this.line = line;
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

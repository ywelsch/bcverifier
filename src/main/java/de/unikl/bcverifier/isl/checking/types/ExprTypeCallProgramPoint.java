package de.unikl.bcverifier.isl.checking.types;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.Version;

public class ExprTypeCallProgramPoint extends ExprType {

	private final Version version; 
	private final int line; 
	private final MethodInvocation inv;
	
	public Version getVersion() {
		return version;
	}

	public int getLine() {
		return line;
	}

	public MethodInvocation getInv() {
		return inv;
	}

	
	public ExprTypeCallProgramPoint(Version version, int line,
			MethodInvocation inv) {
		this.version = version;
		this.line = line;
		this.inv = inv;
	}

	@Override
	public boolean isSubtypeOf(ExprType t) {
		// TODO Auto-generated method stub
		return false;
	}

}

package de.unikl.bcverifier.isl.checking.types;

import de.unikl.bcverifier.isl.ast.Version;

public abstract class ExprTypeProgramPoint extends ExprType {
	protected final Version version; 
	protected final int line; 
	
	protected ExprTypeProgramPoint(Version version, int line) {
		this.version = version;
		this.line = line;
	}
	
	public Version getVersion() {
		return version;
	}

	public int getLine() {
		return line;
	}

}

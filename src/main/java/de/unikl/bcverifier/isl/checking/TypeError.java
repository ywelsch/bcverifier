package de.unikl.bcverifier.isl.checking;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.parser.IslError;

public class TypeError extends IslError {
	public final ASTNode<?> node;
	
	public TypeError(ASTNode<?> node, String msg) {
		super(node, msg);
		this.node = node;
	}
	
	@Override
	public String toString() {
		return "line " + getLine() + ":" + getColumn() 
				+": "+ getMessage();
	}

	
	public int getStart() {
		return node.getStart();
	}
	
	public int getEnd() {
		return node.getEnd();
	}
	
}

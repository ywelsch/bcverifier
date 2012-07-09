package de.unikl.bcverifier.isl.checking;

import beaver.Symbol;
import de.unikl.bcverifier.isl.ast.ASTNode;

public class TypeError {
	public final ASTNode<?> node;
	public final String msg;
	
	public TypeError(ASTNode<?> node, String msg) {
		this.node = node;
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return "line " + Symbol.getLine(node.getStart()) + ":" + Symbol.getColumn(node.getStart()) 
				+": "+ msg;
	}
	
}

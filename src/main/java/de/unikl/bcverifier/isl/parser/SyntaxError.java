package de.unikl.bcverifier.isl.parser;

import beaver.Symbol;

public class SyntaxError extends IslError {

	private Symbol token;

	public SyntaxError(String msg, int line, int column, int endLine, int endColumn, Symbol token) {
		super(line, column, endLine, endColumn, msg);
		this.token = token;
	}

	@Override
	public String toString() {
		return "Line " + getLine() +":" + getColumn() + " : " + getMessage();
	}

}

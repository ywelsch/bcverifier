package de.unikl.bcverifier.isl.parser;

import beaver.Symbol;

public class SyntaxError implements ParserError {

	private String msg;
	private int line;
	private int column;
	private Symbol token;

	
	public SyntaxError(String msg, int line, int column, Symbol token) {
		this.msg = msg;
		this.line = line;
		this.column = column;
		this.token = token;
	}

	@Override
	public String toString() {
		return "Line " + line +":" + column + " : " + msg + " " + token.value;
	}
	
}

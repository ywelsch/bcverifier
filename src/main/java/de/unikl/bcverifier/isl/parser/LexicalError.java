package de.unikl.bcverifier.isl.parser;

public class LexicalError implements ParserError {

	private String msg;
	private int line;
	private int column;

	public LexicalError(String msg, int line, int column) {
		this.msg = msg;
		this.line = line;
		this.column = column;
	}
	
	@Override
	public String toString() {
		return "Lexical error in line " + line +":" + column + " : " + msg;
	}

}

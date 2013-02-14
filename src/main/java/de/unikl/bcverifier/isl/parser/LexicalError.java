package de.unikl.bcverifier.isl.parser;

public class LexicalError extends IslError {


	public LexicalError(String msg, int line, int column) {
		super(line, column, line, column, msg);
	}
	
	@Override
	public String toString() {
		return "Lexical error in line " + getLine() +":" + getColumn() + " : " + getMessage();
	}

}

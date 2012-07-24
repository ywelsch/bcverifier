package de.unikl.bcverifier.isl.parser;

public enum BinaryOperator {
	IMPLIES("==>"), RELATED("~"), MOD("%"), EQUALS("=="), 
	UNEQUALS("!="), DIV("/"), MINUS("-"), MULT("*"), 
	PLUS("+"), AND("&&"), OR("||");

	
	private String c;

	private BinaryOperator(String c) {
		 this.c = c;
	 }
	
	@Override
	public String toString() {
		return c;
	}
}

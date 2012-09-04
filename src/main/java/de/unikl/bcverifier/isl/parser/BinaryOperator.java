package de.unikl.bcverifier.isl.parser;

public enum BinaryOperator {
	IMPLIES("==>"), RELATED("~"), MOD("%"), EQUALS("=="), 
	UNEQUALS("!="), DIV("/"), MINUS("-"), MULT("*"), 
	PLUS("+"), AND("&&"), OR("||"), IFF("<==>"), LT("<"), LTEQ("<="), GT(">"), GTEQ(">=");

	
	private String c;

	private BinaryOperator(String c) {
		 this.c = c;
	 }
	
	@Override
	public String toString() {
		return c;
	}
}

package de.unikl.bcverifier.isl.parser;

public enum UnaryOperator {
	 UMINUS("-"), NOT("!");

	 private String c;

	private UnaryOperator(String c) {
		 this.c = c;
	 }
	
	@Override
	public String toString() {
		return c;
	}
	 
}

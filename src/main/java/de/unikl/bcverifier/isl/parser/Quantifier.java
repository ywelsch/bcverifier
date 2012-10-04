package de.unikl.bcverifier.isl.parser;

public enum Quantifier {
	FORALL("forall"), EXISTS("exists");

	private String c;

	private Quantifier(String c) {
		 this.c = c;
	 }
	
	@Override
	public String toString() {
		return c;
	}

}

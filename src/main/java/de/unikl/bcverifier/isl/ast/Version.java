package de.unikl.bcverifier.isl.ast;

public enum Version {
	OLD, NEW, BOTH;
	
	public String toString() {
		return super.toString().toLowerCase();
	};
}

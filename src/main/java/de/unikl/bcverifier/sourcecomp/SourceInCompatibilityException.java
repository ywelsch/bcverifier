package de.unikl.bcverifier.sourcecomp;

public class SourceInCompatibilityException extends Exception {
	public SourceInCompatibilityException(String message) {
		super("Source compatibility does not hold:\n" + message);
	}

}

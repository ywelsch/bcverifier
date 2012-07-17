package de.unikl.bcverifier.specification;

public class GenerationException extends Exception {
    private static final long serialVersionUID = -7826704299347861040L;

    public GenerationException(String msg) {
        super(msg);
    }
    
    public GenerationException(String msg, Throwable t) {
        super(msg,t);
    }
}

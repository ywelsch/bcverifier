package de.unikl.bcverifier.specification;

public class GenerationException extends Exception {
    private static final long serialVersionUID = -7826704299347861040L;

    public GenerationException(String msg) {
        super("Error in coupling invariant:\n" + msg);
    }
    
    public GenerationException(String msg, Throwable t) {
        super("Error in coupling invariant:\n" + msg,t);
    }
}

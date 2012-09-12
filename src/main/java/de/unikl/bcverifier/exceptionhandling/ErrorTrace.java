package de.unikl.bcverifier.exceptionhandling;

import java.util.List;

public class ErrorTrace {
    private int numberOfExceptions;
    private int numberVerified;
    private List<AssertionException> exceptions;
    
    public ErrorTrace(int numberOfExceptions, int numberVerified,
            List<AssertionException> exceptions) {
        super();
        this.numberOfExceptions = numberOfExceptions;
        this.numberVerified = numberVerified;
        this.exceptions = exceptions;
    }

    public int getNumberVerified() {
        return numberVerified;
    }

    public void setNumberVerified(int numberVerified) {
        this.numberVerified = numberVerified;
    }

    public int getNumberOfExceptions() {
        return numberOfExceptions;
    }
    public void setNumberOfExceptions(int numberOfExceptions) {
        this.numberOfExceptions = numberOfExceptions;
    }
    public List<AssertionException> getExceptions() {
        return exceptions;
    }
    public void setExceptions(List<AssertionException> exceptions) {
        this.exceptions = exceptions;
    }
    
}

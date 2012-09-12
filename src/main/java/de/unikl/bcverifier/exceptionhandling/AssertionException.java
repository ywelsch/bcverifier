package de.unikl.bcverifier.exceptionhandling;

import java.util.List;

public class AssertionException {
    private List<String> boogieTrace;
    private List<SimulationStep> stepsInImpl1;
    private List<SimulationStep> stepsInImpl2;
    private String failedAssertion;
    private int failedAssertionLine;
    public int getFailedAssertionLine() {
        return failedAssertionLine;
    }
    public void setFailedAssertionLine(int failedAssertionLine) {
        this.failedAssertionLine = failedAssertionLine;
    }
    public List<String> getBoogieTrace() {
        return boogieTrace;
    }
    public void setBoogieTrace(List<String> boogieTrace) {
        this.boogieTrace = boogieTrace;
    }
    public List<SimulationStep> getStepsInImpl1() {
        return stepsInImpl1;
    }
    public void setStepsInImpl1(List<SimulationStep> stepsInImpl1) {
        this.stepsInImpl1 = stepsInImpl1;
    }
    public List<SimulationStep> getStepsInImpl2() {
        return stepsInImpl2;
    }
    public void setStepsInImpl2(List<SimulationStep> stepsInImpl2) {
        this.stepsInImpl2 = stepsInImpl2;
    }
    public String getFailedAssertion() {
        return failedAssertion;
    }
    public void setFailedAssertion(String failedAssertion) {
        this.failedAssertion = failedAssertion;
    }
    public AssertionException(List<String> boogieTrace,
            List<SimulationStep> stepsInImpl1,
            List<SimulationStep> stepsInImpl2,
            String failedAssertion,
            int failedAssertionLine) {
        super();
        this.boogieTrace = boogieTrace;
        this.stepsInImpl1 = stepsInImpl1;
        this.stepsInImpl2 = stepsInImpl2;
        this.failedAssertion = failedAssertion;
        this.failedAssertionLine = failedAssertionLine;
    }
    
    
}

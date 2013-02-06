package de.unikl.bcverifier.exceptionhandling;

import java.util.List;

public class AssertionException {
    private final List<String> boogieTrace;
	private final List<SimulationStep> trace;
    private final String failedAssertion;
    
	public AssertionException(List<String> boogieTrace, List<SimulationStep> trace, String failedAssertion) {
		this.boogieTrace = boogieTrace;
		this.trace = trace;
		this.failedAssertion = failedAssertion;
	}
	public List<SimulationStep> getTrace() {
		return trace;
	}
	public String getFailedAssertion() {
		return failedAssertion;
	}
	public List<String> getBoogieTrace() {
		return boogieTrace;
	}
    
}

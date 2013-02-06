package de.unikl.bcverifier.exceptionhandling;

import de.unikl.bcverifier.exceptionhandling.Traces.TraceComment;
import de.unikl.bcverifier.isl.ast.Version;


public class SimulationStep {
	private final Version currentLib;
	private final TraceComment traceComment;
	public SimulationStep(Version currentLib, TraceComment tc) {
		this.currentLib = currentLib;
		this.traceComment = tc;
	}
	
	@Override
	public String toString() {
		return currentLib + "	" + traceComment;
	}

	public Version getLib() {
		return currentLib;
	}

	public TraceComment getTraceComment() {
		return traceComment;
	}
   
}

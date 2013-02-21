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

	public String getMessage() {
		return traceComment.getMessage();
	}

	public SimulationStep withMessage(String message) {
		return new SimulationStep(currentLib, traceComment.withMessage(message));
	}

	public boolean isTracePosition() {
		return getMessage().equals(Traces.TRACE_POSITION_MESSAGE)
				|| getMessage().equals(Traces.TRACE_POSITION_MESSAGE_CONT);
	}

	public boolean hasSameSourcePosAs(SimulationStep nextStep) {
		
		return currentLib == nextStep.currentLib
				&& traceComment.getLine() == nextStep.getTraceComment().getLine()
				&& traceComment.getFile().equals(nextStep.getTraceComment().getFile());
	}
   
	
}

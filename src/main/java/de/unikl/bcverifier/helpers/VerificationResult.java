package de.unikl.bcverifier.helpers;

import b2bpl.bpl.ast.BPLProgram;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.exceptionhandling.ErrorTrace;
import de.unikl.bcverifier.exceptionhandling.ErrorTraceParser;
import de.unikl.bcverifier.exceptionhandling.ErrorTracePrinter;
import de.unikl.bcverifier.exceptionhandling.ErrorTraceParser.TraceParseException;

public class VerificationResult {
    private boolean lastRunSuccess;
    private String lastMessage = "";
    private int lastUnreachableCodeCount = 0;
    private int lastErrorCount = 0;
    private int lastVerified = 0;
	private ErrorTrace trace;
	private final BPLProgram program;
    
    public VerificationResult(BPLProgram program) {
		this.program = program;
	}

	public static VerificationResult fromBoogie(BoogieRunner runner, BPLProgram program) {
        VerificationResult result = new VerificationResult(program);
        result.lastRunSuccess = runner.getLastReturn();
        result.lastMessage = runner.getLastMessage();
        result.lastUnreachableCodeCount = runner.getLastUnreachalbeCodeCount();
        result.lastErrorCount = runner.getLastErrorCount();
        result.lastVerified = runner.getLastVerifiedCount();
        
        return result;
    }
    
    public boolean isLastRunSuccess() {
        return lastRunSuccess;
    }
    
    public void setLastRunSuccess(boolean lastRunSuccess) {
        this.lastRunSuccess = lastRunSuccess;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public int getLastUnreachableCodeCount() {
        return lastUnreachableCodeCount;
    }
    
    public void setLastUnreachableCodeCount(int lastUnreachableCodeCount) {
        this.lastUnreachableCodeCount = lastUnreachableCodeCount;
    }
    
    public int getLastErrorCount() {
        return lastErrorCount;
    }
    
    public void setLastErrorCount(int lastErrorCount) {
        this.lastErrorCount = lastErrorCount;
    }
    
    public int getLastVerified() {
        return lastVerified;
    }
    
    public void setLastVerified(int lastVerified) {
        this.lastVerified = lastVerified;
    }

	public String getErrorTrace(boolean printBoogieTrace) {
		if (trace == null) {
			// parse the error trace
	        ErrorTraceParser etp = new ErrorTraceParser(program);
	        try {
				trace = etp.parse(lastMessage);
			} catch (TraceParseException e) {
				e.printStackTrace();
			}
		}
	    ErrorTracePrinter printer = new ErrorTracePrinter();
        printer.print(trace, printBoogieTrace);
        return printer.getOutput();
	}

	public BPLProgram getProgram() {
		return program;
	}
}

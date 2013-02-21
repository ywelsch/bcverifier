package de.unikl.bcverifier.exceptionhandling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLGotoCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLRawCommand;
import b2bpl.translation.ITranslationConstants;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.exceptionhandling.Traces.TraceComment;
import de.unikl.bcverifier.isl.ast.Version;

public class ErrorTraceParser {
    public class TraceParseException extends Exception {

        /**
         * generated
         */
        private static final long serialVersionUID = 409526870725244509L;

        public TraceParseException() {
        
        }

        public TraceParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public TraceParseException(String message) {
            super(message);
        }
        
    }
    
    private Pattern assertionLine = Pattern.compile("(.*?)\\(([0-9]+),([0-9]+)\\): Error BP5001.*");
    private Pattern labelLine = Pattern.compile("  (.*?)\\(([0-9]+),([0-9]+)\\): (.*?)([#][0-9]+)?");
    
    private Pattern countPattern = Pattern.compile("Boogie program verifier finished with (\\d+) verified, (\\d+) error(s)?");
    
    
    private List<AssertionException> exceptions = new ArrayList<AssertionException>();
    
	private final BPLProgram program;
	private Version currentLib;
	private boolean sawTracePosition = false;
	private TraceComment lastTraceComment;
    
    public ErrorTraceParser(BPLProgram program) {
    	this.program = program;
	}
    
    public ErrorTrace parse(String input) throws TraceParseException {
    	List<String> lines = getLines(input);
    	findFailedAssertions(lines);
    	
        String lastLine = lines.get(lines.size()-1);
		Matcher matcher = countPattern.matcher(lastLine);
        int verifiedCount;
        int errorCount;
        if(!matcher.matches()){
            verifiedCount = -1;
            errorCount = -1;
        } else {
            verifiedCount = Integer.parseInt(matcher.group(1));
            errorCount = Integer.parseInt(matcher.group(2));
        }
        
        return new ErrorTrace(errorCount, verifiedCount, exceptions);
    }
    
    private void findFailedAssertions(List<String> lines) throws TraceParseException {
    	List<String> trace = Lists.newArrayList();
    	currentLib = Version.BOTH;
    	String failedAssertion = null;
		for (String line : lines) {
			Matcher matcher = assertionLine.matcher(line);
			if(matcher.matches()){
				// new assertion found
				if (failedAssertion != null) {
					interpretTrace(failedAssertion, trace);
				}
				String boogieFile = matcher.group(1);
				int failedAsssertionLine = Integer.parseInt(matcher.group(2));
				try{
					List<String> boogieFileLines = FileUtils.readLines(new File(boogieFile));
					failedAssertion = getFailedAssertion(failedAsssertionLine, boogieFileLines);
				} catch(IOException ex) {
					throw new TraceParseException("Exception reading Boogie file:", ex);
				}
				trace = Lists.newArrayList();
				currentLib = Version.BOTH;
				continue;
			}
			trace.add(line);
		}
		if (failedAssertion != null) {
			interpretTrace(failedAssertion, trace);
		}
    }

	private List<String> extractLabels(List<String> trace) {
		List<String> result = Lists.newArrayListWithCapacity(trace.size());
		for (String line : trace) {
			Matcher matcher = labelLine.matcher(line);
            if(!matcher.matches()) {
            	continue;
            }
            String label = matcher.group(4);
            result.add(label);
		}
		return result;
	}

	private void interpretTrace(String failedAssertion, List<String> trace) {
		List<String> labels = extractLabels(trace);
		List<SimulationStep> steps = Lists.newArrayList();
		for (int i=0; i<labels.size(); i++) {
			String label = labels.get(i);
            if (label.startsWith("lib1_")) {
            	currentLib = Version.OLD;
            } else if (label.startsWith("lib2_")) {
            	currentLib = Version.NEW;
            }
            sawTracePosition = false;
            String nextLabel;
            if (i+1 < labels.size()) {
            	nextLabel = labels.get(i+1);
            } else {
            	nextLabel = "";
            }
            findSourceLinesForLabel(steps, label, nextLabel);
		}
		postProcess(steps);
		exceptions.add(new AssertionException(trace, steps, failedAssertion));
	}

	/**
	 * postprocessing for the steps.
	 * for example merge steps with meaningful names with simple trace-positions 
	 */
	private void postProcess(List<SimulationStep> steps) {
		for (int i=0; i<steps.size(); i++) {
			SimulationStep step = steps.get(i);
			if (step.getTraceComment().usesNextPos()) {
				// use source from next step
				for (int j=i+1; j<steps.size(); j++) {
					SimulationStep nextStep = steps.get(j);
					if (nextStep.getTraceComment().hasSourceInfo()) {
						steps.set(i, nextStep.withMessage(step.getMessage()));
						break;
					}
				}
			} else if (step.getTraceComment().usesLastPos()) {
				// use source from previous step
				for (int j=i-1; j>=0; j--) {
					SimulationStep prevStep = steps.get(j);
					if (prevStep.getTraceComment().hasSourceInfo()) {
						steps.set(i, prevStep.withMessage(step.getMessage()));
						break;
					}
				}
			}
		}
		
		for (int i=0; i<steps.size()-1; i++) {
			// merge with nextStep if next step is just a trace position without message
			SimulationStep step = steps.get(i);
			SimulationStep nextStep = steps.get(i+1);
			if (nextStep.isTracePosition() && step.hasSameSourcePosAs(nextStep)) {
				steps.remove(i+1);
				i--;
			}
		}
		
		for (int i=1; i<steps.size(); i++) {
			// merge with previous step if previous step is just a trace position without message
			SimulationStep step = steps.get(i);
			SimulationStep prevStep = steps.get(i-1);
			if (prevStep.isTracePosition() && step.hasSameSourcePosAs(prevStep)) {
				steps.remove(i-1);
				i--;
			}
		}
		
	}

	private List<String> getLines(String input) {
    	Scanner scanner = new Scanner(input);
        List<String> result = Lists.newArrayList();
		while (scanner.hasNextLine()) {
          result.add(scanner.nextLine());
        }
		scanner.close();
        return result;
	}

	private void findSourceLinesForLabel(List<SimulationStep> steps, String label, String nextLabel) {
		BPLBasicBlock block = getBlockWithLabel(label);
		for (String comment : block.getComments()) {
			interpretTraceComment(steps, comment);
		}
		for (BPLCommand cmd : block.getCommands()) {
			if (cmd instanceof BPLRawCommand) {
				BPLRawCommand rawCmd = (BPLRawCommand) cmd;
				String cmdStr = rawCmd.getCommandString();
				if (!cmdStr.startsWith("// ")) {
					continue;
				}
				interpretTraceComment(steps, cmdStr.substring(3));
			}
		}
		if (block.getTransferCommand() instanceof BPLGotoCommand) {
			BPLGotoCommand gotoCmd = (BPLGotoCommand) block.getTransferCommand();
			if (gotoCmd.getTargetLabels().length == 1) {
				// there is only one goto label
				String gotoLabel = gotoCmd.getTargetLabels()[0];
				if (!gotoLabel.equals(nextLabel)) {
					// follow the gotoLabel
					// (sometimes boogie omits labels in the trace if there is only one goto target)
					findSourceLinesForLabel(steps, gotoLabel, nextLabel);	
				}
			}
		}
	}

	private void interpretTraceComment(List<SimulationStep> steps, String comment) {
		if (Traces.isTraceComment(comment)) {
			TraceComment c = Traces.parseComment(comment);
			if (c.equals(lastTraceComment)) {
				// only use each position once
				return;
			}
			lastTraceComment = c;
			if (c.getMessage().equals(Traces.TRACE_POSITION_MESSAGE)) {
				if (sawTracePosition ) {
					// use dots to indicate that there was no branch inbetween
					c = c.withMessage(Traces.TRACE_POSITION_MESSAGE_CONT);
				}
				sawTracePosition = true;
			} else {
				sawTracePosition = false;
			}
			steps.add(new SimulationStep(currentLib, c));
		}
	}

	private BPLBasicBlock getBlockWithLabel(String label) {
		BPLImplementation func = getCheckLibrariesFunc();
		BPLImplementationBody body = func.getBody();
		for (BPLBasicBlock block: body.getBasicBlocks()) {
			if (block.getLabel().equals(label)) {
				return block;
			}
		}
		throw new Error("Block " + label + " not found.");
	}

	private BPLImplementation getCheckLibrariesFunc() {
		for (BPLDeclaration decl : program.getDeclarations()) {
			if (decl instanceof BPLProcedure) {
				BPLProcedure func = (BPLProcedure) decl;
				if (func.getName().equals(ITranslationConstants.CHECK_LIBRARIES_PROCEDURE_NAME)) {
					return func.getImplementation();
				}
			}
		}
		throw new Error("checkLibraries func not found.");
	}

	/** fetches the given line and also all preceding comments 
	 * @param boogieFileLines */
	private String getFailedAssertion(int line, List<String> boogieFileLines) {
		line--;
		String origLine = boogieFileLines.get(line);
		if (!origLine.trim().startsWith("assert")) {
			// special case: error is not at an assert so
			// it must be because of loop unrolling
			return "!Reached maximum loop unrolling.";
		}
		
		String result = "";
		while (line > 0) {
			line--;
			String l = boogieFileLines.get(line);
			if (!l.matches("^\\s*//.*")) {
				break;
			}
			String comment = l.substring(l.indexOf("//")+3);
			if (comment.startsWith("#")) {
				break;
			}
			result = comment + "\n" + result;
		}
		if (result.isEmpty()) {
			return origLine;
		}
		return result;
	}
    
    public BPLProgram getProgram() {
		return program;
	}
    
}

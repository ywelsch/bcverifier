package de.unikl.bcverifier.exceptionhandling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.exceptionhandling.Traces.TraceComment;
import de.unikl.bcverifier.isl.ast.Version;

public class ErrorTracePrinter {
    protected List<String> lines = new ArrayList<String>();
    StringBuffer currentLine = new StringBuffer();

    
	public void reset() {
        lines = new ArrayList<String>();
        currentLine = new StringBuffer();
    }
    
    public void print(ErrorTrace trace, boolean printBoogieTrace){
        println(String.format("%d assertions failed:", trace.getNumberOfExceptions()));
        println();
        
        int i=0;
        
        for(AssertionException ex : trace.getExceptions()){
        	i++;
        	if (trace.getNumberOfExceptions() > 1) {
        		printHeading(i + ". problem:");
        	}
        	
        	if (ex.getFailedAssertion().startsWith("!")) {
        		println(esc(ex.getFailedAssertion().substring(1)));
        	} else {
        		println("The following assertion might not hold: ");
        		printFailedAssertion(esc(ex.getFailedAssertion()));
        	}
            
        	if (ex.getTrace().isEmpty()) {
        		// do not print empty traces
        		continue;
        	}
        	
            println("Execution Trace:");
            Version lastLib = null;
            
            List<String> column1 = Lists.newArrayList();
            List<String> column2 = Lists.newArrayList(); 
            
            for (SimulationStep step : ex.getTrace()) {
            	Version lib = step.getLib();
            	if (lastLib != lib) {
            		column1.add("Steps in " + lib.toString().toLowerCase() + " library:");
            		column2.add("");
            		lastLib = lib;
            	}
            	
            	TraceComment tc = step.getTraceComment();
            	if (tc.getFile().isEmpty()) {
            		column1.add("");
            	} else if (tc.getFile().equals("%")) {
            		column1.add(esc(tc.getMessage()));
            		column2.add("");
            		continue;
            	} else {
            		column1.add(makeLink(lastLib, tc.getFile(), tc.getLine()));
            	}
				column2.add(esc(tc.getMessage()));
            	
            }
            
            printTable(column1, column2);
            if (ex.getFailedAssertion().startsWith("!")) {
            	println(esc("--> " + ex.getFailedAssertion().substring(1)));
        	} else {
        		printFailedAssertion(esc("--> Violation: " + ex.getFailedAssertion()));
        	}
            
            println();
            if(printBoogieTrace){
                for(String line : ex.getBoogieTrace()){
                    println(line);
                }
            }
            println();
            println();
        }
    }

	protected void printFailedAssertion(String msg) {
		println(msg);
	}

	protected void printHeading(String s) {
		println();
		println();
		println(s);
		println();
	}

	protected String esc(String s) {
		return s;
	}

	protected String makeLink(Version lastLib, String file, int line) {
		return "(" + file + ".java:" + line + ")";
	}

	protected void printTable(List<String> column1, List<String> column2) {
		int column1Size = 0;
		for (int i=0; i<column1.size(); i++) {
			if (!column2.get(i).isEmpty()) {
				column1Size = Math.max(column1Size, column1.get(i).length());
			}
		}
		for (int i=0; i<column1.size(); i++) {
			print("  ");
			print(column1.get(i));
			for (int j=column1.get(i).length(); j<column1Size+2; j++) {
				print(" ");
			}
			println(column2.get(i));
		}
	}
    
    // Uttility methods
    /////////////////////
    
    protected void print(String s) {
        currentLine.append(s);
    }
    
	protected void println(String s) {
        print(s);
        println();
    }
    
    protected void println() {
        lines.add(currentLine.toString());
        currentLine = new StringBuffer();
    }
    
    protected void flush() {
        if(currentLine.length()>0){
            println();
        }
    }
    
    public void output(PrintStream out) {
        flush();
        for(String line : lines){
            out.println(line);
        }
    }
    
    public List<String> getLines() {
        flush();
        return lines;
    }
    
    public String getOutput() {
        flush();
        StringBuffer buffer = new StringBuffer();
        for(String line : lines) {
            buffer.append(String.format("%s%n", line));
        }
        return buffer.toString();
    }
}

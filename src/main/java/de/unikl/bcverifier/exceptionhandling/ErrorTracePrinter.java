package de.unikl.bcverifier.exceptionhandling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.exceptionhandling.Traces.TraceComment;
import de.unikl.bcverifier.isl.ast.Version;

public class ErrorTracePrinter {
    List<String> lines = new ArrayList<String>();
    StringBuffer currentLine = new StringBuffer();

    public void reset() {
        lines = new ArrayList<String>();
        currentLine = new StringBuffer();
    }
    
    public void print(ErrorTrace trace, boolean printBoogieTrace){
        println(String.format("%d assertions failed:", trace.getNumberOfExceptions()));
        println();
        
        for(AssertionException ex : trace.getExceptions()){
        	if (ex.getFailedAssertion().startsWith("!")) {
        		println(ex.getFailedAssertion().substring(1));
        	} else {
        		println("The following assertion might not hold: ");
        		println(ex.getFailedAssertion());
        	}
            
            println("Execution Trace:");
            int indent = 0;
            Version lastLib = null;
            
            List<String> column1 = Lists.newArrayList();
            List<String> column2 = Lists.newArrayList(); 
            
            for (SimulationStep step : ex.getTrace()) {
            	Version lib = step.getLib();
            	if (lastLib != lib) {
            		column1.add("Steps in " + lib.toString().toLowerCase() + " library:");
            		column2.add("");
            		indent = 0;
            		lastLib = lib;
            	}
            	
            	TraceComment tc = step.getTraceComment();
            	if (tc.getFile().isEmpty()) {
            		column1.add("");
            	} else if (tc.getFile().equals("%")) {
            		column1.add(tc.getMessage());
            		column2.add("");
            		continue;
            	} else {
            		column1.add("(" + tc.getFile() + ".java:" + tc.getLine() + ")");
            	}
            	String msg = tc.getMessage();
				column2.add(msg);
            	
            }
            
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
    
    // Uttility methods
    /////////////////////
    
	private void print(String s) {
        currentLine.append(s);
    }
    
    private void println(String s) {
        print(s);
        println();
    }
    
    private void println() {
        lines.add(currentLine.toString());
        currentLine = new StringBuffer();
    }
    
    private void flush() {
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

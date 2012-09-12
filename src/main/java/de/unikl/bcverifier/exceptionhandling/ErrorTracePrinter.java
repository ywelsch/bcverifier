package de.unikl.bcverifier.exceptionhandling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.unikl.bcverifier.exceptionhandling.SimulationStep.Action;
import de.unikl.bcverifier.exceptionhandling.SimulationStep.Direction;

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
            print("Assertion("+ex.getFailedAssertionLine()+"): ");
            println(ex.getFailedAssertion());
            
            println("Steps in implementation 1:");
            List<SimulationStep> stepsInImpl1 = ex.getStepsInImpl1();
            for(int i=0; i<stepsInImpl1.size(); i++){
                print(stepsInImpl1.get(i));
                if(i<stepsInImpl1.size() - 1) {
                    print(" -> ");
                }
            }
            println();
            
            println("Steps in implementation 2:");
            List<SimulationStep> stepsInImpl2 = ex.getStepsInImpl2();
            for(int i=0; i<stepsInImpl2.size(); i++){
                print(stepsInImpl2.get(i));
                if(i<stepsInImpl2.size() - 1) {
                    print(" -> ");
                }
            }
            println();
            if(printBoogieTrace){
                for(String line : ex.getBoogieTrace()){
                    println(line);
                }
                println();
            }
        }
    }
    
    private void print(SimulationStep simulationStep) {
        Action action = simulationStep.getAction();
        switch(action) {
        case CONSTRUCTOR_CALL:
            print("call constructor");
            print(simulationStep.getDirection());
            print(" ");
            print(simulationStep.getCalledMethod());
            break;
        case LOCAL_CHECK:
            print("check at ");
            print(simulationStep.getLocalPlace());
            break;
        case LOCAL_CONTINUE:
            print("continue at ");
            print(simulationStep.getLocalPlace());
            break;
        case METHOD_CALL:
            print("call");
            print(simulationStep.getDirection());
            print(" ");
            print(simulationStep.getCalledMethod());
            break;
        case METHOD_RETURN:
            print("return");
            print(simulationStep.getDirection());
            print(" from ");
            print(simulationStep.getInMethod());
        }
    }
    
    private void print(Direction dir) {
        switch(dir) {
        case IN:
            print("[<]");
            break;
        case OUT:
            print("[>]");
            break;
        case INTERN:
            print("[-]");
            break;
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

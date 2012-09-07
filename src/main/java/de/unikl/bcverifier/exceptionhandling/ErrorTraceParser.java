package de.unikl.bcverifier.exceptionhandling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.unikl.bcverifier.exceptionhandling.SimulationStep.Action;
import de.unikl.bcverifier.exceptionhandling.SimulationStep.Direction;

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
    
    private enum State {
        FIND_ASSERTION,
        FIND_LIBRARY_ACTION,
        FIND_CALLED_METHOD,
        FIND_ACTION_FOR_METHOD,
        FIND_CHECK
    }
    
    private Pattern assertionLine = Pattern.compile("(.*?)\\(([0-9]+),([0-9]+)\\): Error BP5001.*");
    private Pattern labelLine = Pattern.compile("  (.*?)\\(([0-9]+),([0-9]+)\\): (.*?)([#][0-9]+)?");
    
    private Pattern libraryActionLabel = Pattern.compile("preconditions_(call|return|local|constructor)");
    private Pattern calledMethodLabel = Pattern.compile("lib(1|2)_(.*)");
    //TODO the following patterns do not work if the method name used in the library implementation includes an underscore
    private Pattern boundaryReturnOutLabel = Pattern.compile("lib(1|2)_([^_]+)_exit_boundary_return");
    private Pattern internReturnOutLabel = Pattern.compile("lib(1|2)_([^_]+)_exit_intern_return");
    private Pattern boundaryCallLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_([0-9])+_boundary");
    private Pattern internCallLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_([0-9])+_intern");
    private Pattern boundaryReturnInLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_([0-9])+_boundary_return");
    private Pattern internReturnInLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_([0-9])+_intern_return");
    private Pattern localCheckLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_check");
    private Pattern localContinueLabel = Pattern.compile("lib(1|2)_([^_]+)_([^_]+)_cont");
    
    
    private State state = State.FIND_ASSERTION;
    
    private List<AssertionException> exceptions = new ArrayList<AssertionException>();
    
    private String boogieFile;
    private List<String> boogieFileLines;
    private int failedAsssertionLine;
    private String failedAssertion;
    private Action firstAction;
    private List<SimulationStep> stepsInImpl1 = new ArrayList<SimulationStep>();
    private List<SimulationStep> stepsInImpl2 = new ArrayList<SimulationStep>();
    private List<String> thisExceptionLines = new ArrayList<String>();
    
    public List<AssertionException> parse(String input) throws TraceParseException {
        exceptions.clear();
        stepsInImpl1.clear();
        stepsInImpl2.clear();
        thisExceptionLines.clear();
        Scanner scanner = new Scanner(input);
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          step(line);
        }
        
        //the last exception still has to be added
        AssertionException ex = new AssertionException(thisExceptionLines, stepsInImpl1, stepsInImpl2, failedAssertion, failedAsssertionLine);
        exceptions.add(ex);
        
        return exceptions;
    }
    
    private void step(String line) throws TraceParseException{
        Matcher matcher;
        switch(state) {
            case FIND_ASSERTION:
                matcher = assertionLine.matcher(line);
                if(!matcher.matches()){
                    throw new TraceParseException("First assertion line not found.");
                }
                boogieFile = matcher.group(1);
                try{
                boogieFileLines = FileUtils.readLines(new File(boogieFile));
                } catch(IOException ex) {
                    throw new TraceParseException("Exception reading Boogie file:", ex);
                }
                failedAsssertionLine = Integer.parseInt(matcher.group(2));
                failedAssertion = boogieFileLines.get(failedAsssertionLine-1);
                state = State.FIND_LIBRARY_ACTION;
                thisExceptionLines.add(line);
                break;
            case FIND_LIBRARY_ACTION:
                if(!lookForAssertionBegin(line)){
                    matcher = labelLine.matcher(line);
                    if(!matcher.matches())
                        return; //the current line is not a label line (but what could it be?)
                    String label = matcher.group(4);
                    matcher = libraryActionLabel.matcher(label);
                    if(!matcher.matches())
                        return; // label we found was not a precondition label -> action unknown
                    String action = matcher.group(1);
                    if(action.equals("call")){
                        firstAction = Action.METHOD_CALL;
                        state = State.FIND_CALLED_METHOD;
                    } else if(action.equals("return")){
                        firstAction = Action.METHOD_RETURN;
                        state = State.FIND_ACTION_FOR_METHOD;
                    } else if(action.equals("local")){
                        firstAction = Action.LOCAL_CONTINUE;
                        state = State.FIND_ACTION_FOR_METHOD;
                    } else if(action.equals("constructor")){
                        firstAction = Action.CONSTRUCTOR_CALL;
                        state = State.FIND_CALLED_METHOD;
                    }
                }
                thisExceptionLines.add(line);
                break;
            case FIND_CALLED_METHOD:
                if(!lookForAssertionBegin(line)){
                    matcher = labelLine.matcher(line);
                    if(!matcher.matches())
                        return; //the current line is not a label line (but what could it be?)
                    String label = matcher.group(4);
                    matcher = calledMethodLabel.matcher(label);
                    if(!matcher.matches()){
                        throw new TraceParseException("Method called on the library could not be determined.");
                    }
                    String libImpl = matcher.group(1);
                    String methodSig = matcher.group(2);
                    
                    List<SimulationStep> list;
                    if(libImpl.equals("1")){
                        list = stepsInImpl1;
                    } else if(libImpl.equals("2")){
                        list = stepsInImpl2;
                    } else {
                        throw new TraceParseException("Wrong library implementation: "+libImpl);
                    }
                    
                    list.add(new SimulationStep(Action.METHOD_CALL, Direction.IN, null, methodSig, null));
                    state = State.FIND_ACTION_FOR_METHOD;
                }
                thisExceptionLines.add(line);
                break;
            case FIND_ACTION_FOR_METHOD:
                if(!lookForAssertionBegin(line)){
                    matcher = labelLine.matcher(line);
                    if(!matcher.matches())
                        return; //the current line is not a label line (but what could it be?)
                    String label = matcher.group(4);
                    
                    boolean success = false;
                    matcher = boundaryReturnOutLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        SimulationStep step = new SimulationStep(Action.METHOD_RETURN, Direction.OUT, methodName);
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                            if(firstAction == Action.METHOD_CALL || firstAction == Action.CONSTRUCTOR_CALL)
                                state = State.FIND_CALLED_METHOD;
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                            state = State.FIND_CHECK;
                        }
                        success = true;
                    }
                    matcher = internReturnOutLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        SimulationStep step = new SimulationStep(Action.METHOD_RETURN, Direction.INTERN, methodName);
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                        }
                        success = true;
                    }
                    matcher = boundaryCallLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String calledMethodName = matcher.group(3);
                        String invocationCount = matcher.group(4);
                        SimulationStep step = new SimulationStep(Action.METHOD_CALL, Direction.OUT, methodName, calledMethodName, null);//TODO add invocation count
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                            if(firstAction == Action.METHOD_CALL || firstAction == Action.CONSTRUCTOR_CALL)
                                state = State.FIND_CALLED_METHOD;
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                            state = State.FIND_CHECK;
                        }
                        success = true;
                    }
                    matcher = internCallLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String calledMethodName = matcher.group(3);
                        String invocationCount = matcher.group(4);
                        SimulationStep step = new SimulationStep(Action.METHOD_CALL, Direction.INTERN, methodName, calledMethodName, null);//TODO add invocation count
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                        }
                        state = State.FIND_CALLED_METHOD; //FIXME this reports the method call twice and the next will be the method including the class as boundary call --> wrong
                        success = true;
                    }
                    matcher = boundaryReturnInLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String calledMethodName = matcher.group(3);
                        String invocationCount = matcher.group(4);
                        SimulationStep step = new SimulationStep(Action.METHOD_RETURN, Direction.IN, calledMethodName);//TODO add invocation count
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                        }
                        success = true;
                    }
                    matcher = internReturnInLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String calledMethodName = matcher.group(3);
                        String invocationCount = matcher.group(4);
                        SimulationStep step = new SimulationStep(Action.METHOD_RETURN, Direction.INTERN, calledMethodName);//TODO add invocation count
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                        }
                        success = true;
                    }
                    matcher = localCheckLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String placeName = matcher.group(3);
                        SimulationStep step = new SimulationStep(Action.LOCAL_CHECK, Direction.INTERN, methodName, null, placeName);
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                            if(firstAction == Action.METHOD_CALL || firstAction == Action.CONSTRUCTOR_CALL)
                                state = State.FIND_CALLED_METHOD;
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                            state = State.FIND_CHECK;
                        }
                        success = true;
                    }
                    matcher = localContinueLabel.matcher(label);
                    if(!success && matcher.matches()){
                        String impl = matcher.group(1);
                        String methodName = matcher.group(2);
                        String placeName = matcher.group(3);
                        SimulationStep step = new SimulationStep(Action.LOCAL_CONTINUE, Direction.INTERN, methodName, null, placeName);
                        if(impl.equals("1")){
                            stepsInImpl1.add(step);
                        } else if(impl.equals("2")) {
                            stepsInImpl2.add(step);
                        }
                        success = true;
                    }
                }
                thisExceptionLines.add(line);
                break;
            case FIND_CHECK:
                if(!lookForAssertionBegin(line)){
                    matcher = labelLine.matcher(line);
                    if(!matcher.matches())
                        return; //the current line is not a label line (but what could it be?)
                    String label = matcher.group(4);
                    //TODO
                }
                thisExceptionLines.add(line);
                break;
        }
    }
    
    private boolean lookForAssertionBegin(String line) {
        Matcher matcher;
        matcher = assertionLine.matcher(line);
        if(!matcher.matches()){
            return false;
        }
        
        AssertionException ex = new AssertionException(thisExceptionLines, stepsInImpl1, stepsInImpl2, failedAssertion, failedAsssertionLine);
        exceptions.add(ex);
        
        stepsInImpl1.clear();
        stepsInImpl2.clear();
        thisExceptionLines.clear();
        boogieFile = matcher.group(1);
        failedAsssertionLine = Integer.parseInt(matcher.group(2));
        state = State.FIND_LIBRARY_ACTION;
        return true;
    }
    
}

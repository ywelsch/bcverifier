package de.unikl.bcverifier.specification;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import b2bpl.bpl.IBPLVisitor;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLLiteral;
import b2bpl.bpl.ast.BPLVariableExpression;

import de.unikl.bcverifier.Configuration;

public class BoogieGenerator extends AbstractGenerator {
    private List<String> invariants = new ArrayList<String>();
    private List<String> localInvariants = new ArrayList<String>();
    private List<String> preconditions = new ArrayList<String>();
    private LocalPlaceDefinitions localPlaces;
    
    private static final Pattern sectionHeader = Pattern.compile(">>>([\\w_]+)");
    private static final Pattern sectionEnd = Pattern.compile("<<<");
    private static final Pattern lineWithoutComment = Pattern.compile("\\s*(.*?)(?:\\s*//.*)?");
    
    public enum Section {
        invariant, local_invariant, preconditions, places 
    }
    
    public BoogieGenerator(Configuration config) throws GenerationException {
        super(config);
        parseInput();
    }
    
    private void parseInput() throws GenerationException {
        BufferedReader reader = new BufferedReader(getReader());
        String line;
        Matcher matcher;
        try {
            while((line = reader.readLine()) != null){
                matcher = sectionHeader.matcher(line);
                if(matcher.matches()){
                    switch(Section.valueOf(matcher.group(1))){
                    case invariant:
                        parseInvariant(reader);
                        break;
                    case local_invariant:
                        parseLocalInvariant(reader);
                        break;
                    case preconditions:
                        parsePreconditions(reader);
                        break;
                    case places:
                        parsePlaces(reader);
                        break;
                    default:
                        Logger.getLogger(BoogieGenerator.class).warn("Unkown section "+matcher.group(1)+" found in specification file.");
                        break;
                    }
                }
            }
        } catch(IOException e){
            throw new GenerationException("Error reading specification file", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void parsePlaces(BufferedReader reader) throws GenerationException {
        Pattern p = Pattern.compile("([a-zA-Z0-9_]*)\\s*[=]\\s*(old|new)\\s+(\\d*)\\s+(.*)");
        
        HashMap<Integer,List<Place>> oldMap = new HashMap<Integer, List<Place>>();
        HashMap<Integer,List<Place>> newMap = new HashMap<Integer, List<Place>>();
        LineIterator lineIterator = IOUtils.lineIterator(reader);
        String nextLine;
        Matcher m;
        int lineNumber;
        List<Place> currentPlaceList;
        while(lineIterator.hasNext()){
            nextLine = lineIterator.next();
            m = p.matcher(nextLine);
            if(sectionEnd.matcher(nextLine).matches()){
                break;
            } else if(m.matches()){
                lineNumber = Integer.parseInt(m.group(3));
                if(m.group(2).equals("old")){
                    currentPlaceList = oldMap.get(lineNumber);
                    if(currentPlaceList == null){
                        currentPlaceList = new ArrayList<Place>();
                        oldMap.put(lineNumber, currentPlaceList);
                    }
                } else { // new
                    currentPlaceList = newMap.get(lineNumber);
                    if(currentPlaceList == null){
                        currentPlaceList = new ArrayList<Place>();
                        newMap.put(lineNumber, currentPlaceList);
                    }
                }
                currentPlaceList.add(new Place(m.group(1), m.group(4)));
            } else  {
                Logger.getLogger(BoogieGenerator.class).warn("Unmatched line in preconditions: "+nextLine);
            }
        }

        this.localPlaces =  new LocalPlaceDefinitions(oldMap, newMap);
    }

    private void parsePreconditions(BufferedReader reader) throws GenerationException {
        String line;
        Matcher matcher;
        String precond;
        try {
            while((line = reader.readLine()) != null){
                matcher = lineWithoutComment.matcher(line);
                if(sectionEnd.matcher(line).matches()){
                    break;
                } else if(matcher.matches()){
                    precond = matcher.group(1);
                    if(!precond.isEmpty()){
                        preconditions.add(precond);
                    }
                } else  {
                    Logger.getLogger(BoogieGenerator.class).warn("Unmatched line in preconditions: "+line);
                }
            }
        } catch(IOException e){
            throw new GenerationException("Preconditions could not be parsed", e);
        }
    }

    private void parseLocalInvariant(BufferedReader reader) throws GenerationException {
        String line;
        Matcher matcher;
        String inv;
        try {
            while((line = reader.readLine()) != null){
                matcher = lineWithoutComment.matcher(line);
                if(sectionEnd.matcher(line).matches()){
                    break;
                } else if(matcher.matches()){
                    inv = matcher.group(1);
                    if(!inv.isEmpty()){
                        localInvariants.add(inv);
                    }
                } else  {
                    Logger.getLogger(BoogieGenerator.class).warn("Unmatched line in local invariant: "+line);
                }
            }
        } catch(IOException e){
            throw new GenerationException("Local invariant could not be parsed", e);
        }
    }

    private void parseInvariant(BufferedReader reader) throws GenerationException {
        String line;
        Matcher matcher;
        String inv;
        try {
            while((line = reader.readLine()) != null){
                matcher = lineWithoutComment.matcher(line);
                if(sectionEnd.matcher(line).matches()){
                    break;
                } else if(matcher.matches()){
                    inv = matcher.group(1);
                    if(!inv.isEmpty()){
                        invariants.add(inv);
                    }
                } else  {
                    Logger.getLogger(BoogieGenerator.class).warn("Unmatched line in invariant: "+line);
                }
            }
        } catch(IOException e){
            throw new GenerationException("Invariant could not be parsed", e);
        }
    }
    
    @Override
	public List<SpecInvariant> generateInvariant() throws GenerationException {
    	List<SpecInvariant> result = new ArrayList<SpecInvariant>();
		for (String inv : invariants) {
			result.add(new SpecInvariant(new BPLVariableExpression(inv), null));
		}
		return result;
	}
    
    @Override
    public List<String> generateLocalInvariant() {
        return localInvariants;
    }
    
    @Override
    public LocalPlaceDefinitions generateLocalPlaces() throws GenerationException {
        if(localPlaces != null)
            return localPlaces;
        else
            return super.generateLocalPlaces();
    }
    
    @Override
    public List<String> generatePreconditions() {
        return preconditions;
    }

}

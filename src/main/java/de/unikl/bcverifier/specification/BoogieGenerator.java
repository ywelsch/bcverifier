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

import com.beust.jcommander.internal.Lists;

import b2bpl.bpl.IBPLVisitor;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLLiteral;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;

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
        Pattern p = Pattern.compile("([a-zA-Z0-9_]*)\\s*[=]\\s*(old|new)\\s+(\\d*)\\s+([(].*?[)])(\\s+([(].*?[)]))?(\\s+([(].*?[)]))?");
        
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
                String newStallCondition = m.group(6);
                String oldStallCondition = makeOld(m.group(6));
                String newMeasure = m.group(8);
                String oldMeasure = makeOld(m.group(8));
                if (newStallCondition != null && m.group(2).equals("old") && newMeasure == null) {
                	throw new GenerationException("Places in old implementation that stall must have a measure");
                }
                if (newStallCondition != null && m.group(2).equals("new") && newMeasure != null) {
                	throw new GenerationException("Places in new implementation that stall do not need a measure");
                }
                Place place = new Place(m.group(2).equals("old"), m.group(1), null, true, m.group(4), oldStallCondition, oldMeasure, newStallCondition, newMeasure, Collections.<String>emptyList());
                currentPlaceList.add(place);
                Logger.getLogger(BoogieGenerator.class).debug("Parsed place :" + place); 
            } else  {
                Logger.getLogger(BoogieGenerator.class).warn("Unmatched line in preconditions: "+nextLine);
            }
        }

        this.localPlaces =  new LocalPlaceDefinitions(oldMap, newMap);
    }
    
    private String makeOld(String s) {
    	if (s == null) return s;
    	s = replaceMapCall(s, ITranslationConstants.HEAP1, ITranslationConstants.OLD_HEAP1);
    	s = replaceMapCall(s, ITranslationConstants.HEAP2, ITranslationConstants.OLD_HEAP2);
    	s = replaceMapCall(s, ITranslationConstants.STACK1, ITranslationConstants.OLD_STACK1);
    	s = replaceMapCall(s, ITranslationConstants.STACK2, ITranslationConstants.OLD_STACK2);
    	s = replaceMapCall(s, ITranslationConstants.SP_MAP1_VAR, ITranslationConstants.OLD_SP_MAP1_VAR);
    	s = replaceMapCall(s, ITranslationConstants.SP_MAP2_VAR, ITranslationConstants.OLD_SP_MAP2_VAR);
    	return s;
    }
    
    private String replaceMapCall(String s, String oldM, String newM) {
    	return s.replaceAll(Pattern.quote(oldM + "["), newM + "[");
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
    public List<SpecInvariant> generateLocalInvariant() {
        List<SpecInvariant> result = new ArrayList<SpecInvariant>();
		for (String inv : localInvariants) {
			result.add(new SpecInvariant(new BPLVariableExpression(inv), null));
		}
		return result;
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

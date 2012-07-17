package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

public class MultiFileGenerator extends AbstractGenerator {
    private static final Logger log = Logger.getLogger(MultiFileGenerator.class);
    
    private static final Pattern lineWithoutComment = Pattern.compile("\\s*(.*?)\\s*//.*");
    
    private File invariantFile;
    private File localInvariantFile;
    private File preconditionFile;
    
    public MultiFileGenerator(File invariantFile, File localInvariantFile, File preconditionFile) {
        super((Reader)null);
        this.invariantFile = invariantFile;
        this.localInvariantFile = localInvariantFile;
        this.preconditionFile = preconditionFile;
    }

    @Override
    public List<String> generateInvariant() {
        List<String> invariants = new ArrayList<String>();
        if(invariantFile != null){
            try {
                List<String> lines = FileUtils.readLines(invariantFile, "UTF-8");
                String inv;
                Matcher matcher;
                for(String line : lines){
                    matcher = lineWithoutComment.matcher(line);
                    if(matcher.matches()){
                        inv = matcher.group(1);
                        if(!inv.isEmpty())
                            invariants.add(inv);
                    }
                }
            } catch (IOException e) {
                log.error("Error reading invariant file", e);
            }
        }
        
        return invariants;
    }

    @Override
    public List<String> generateLocalInvariant() {
        List<String> invariants = new ArrayList<String>();
        if(localInvariantFile != null){
            try {
                List<String> lines = FileUtils.readLines(localInvariantFile, "UTF-8");
                String inv;
                Matcher matcher;
                for(String line : lines){
                    matcher = lineWithoutComment.matcher(line);
                    if(matcher.matches()){
                        inv = matcher.group(1);
                        if(!inv.isEmpty())
                            invariants.add(inv);
                    }
                }
            } catch (IOException e) {
                log.error("Error reading local invariant file", e);
            }
        }
        
        return invariants;
    }

    @Override
    public List<String> generatePreconditions() {
        List<String> preconditions = new ArrayList<String>();
        if(preconditionFile != null){
            try {
                List<String> lines = FileUtils.readLines(preconditionFile, "UTF-8");
                String inv;
                Matcher matcher;
                for(String line : lines){
                    matcher = lineWithoutComment.matcher(line);
                    if(matcher.matches()){
                        inv = matcher.group(1);
                        if(!inv.isEmpty())
                            preconditions.add(inv);
                    }
                }
            } catch (IOException e) {
                log.error("Error reading local invariant file", e);
            }
        }
        
        return preconditions;
    }
    
    private LocalPlaceDefinitions parseLocalPlaces(File localPlaces) {
        FileReader reader = null;
        Pattern p = Pattern.compile("([a-zA-Z0-9_]*)\\s*[=]\\s*(old|new)\\s+(\\d*)\\s+(.*)");
        
        HashMap<Integer,List<Place>> oldMap = new HashMap<Integer, List<Place>>();
        HashMap<Integer,List<Place>> newMap = new HashMap<Integer, List<Place>>();
        try {
            reader = new FileReader(localPlaces);
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            String nextLine;
            Matcher m;
            int lineNumber;
            List<Place> currentPlaceList;
            while(lineIterator.hasNext()){
                nextLine = lineIterator.next();
                m = p.matcher(nextLine);
                if(m.matches()){
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
                }
            }
            
            return new LocalPlaceDefinitions(oldMap, newMap);
        } catch (FileNotFoundException e) {
            log.warn("Could not read local places from "+localPlaces, e);
            return null;
        } finally {
            if(reader!=null)
                IOUtils.closeQuietly(reader);
        }
    }
    
}

package de.unikl.bcverifier.helpers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

public class BCCheckDefinition {
    private File libDir;
    private File invariant;
    private File preconditions;
    private String[] flags;
    private int expectedErrors;
    private int expectedDeadCodePoints;
    private int loopUnrollCap;
    private File localInv;

    public File getLocalInv() {
        return localInv;
    }

    public File getLibDir() {
        return libDir;
    }

    public File getInvariant() {
        return invariant;
    }

    public File getPreconditions() {
        return preconditions;
    }

    public String[] getFlags() {
        return flags;
    }

    public int getExpectedErrors() {
        return expectedErrors;
    }

    public int getExpectedDeadCodePoints() {
        return expectedDeadCodePoints;
    }

    public int getLoopUnrollCap() {
        return loopUnrollCap;
    }

    public BCCheckDefinition(File libDir, File invariant, File localInv, File preconditions,
            String[] flags, int expectedErrors, int expectedDeadCodePoints,
            int loopUnrollCap) {
        this.libDir = libDir;
        this.invariant = invariant;
        this.localInv = localInv;
        this.preconditions = preconditions;
        this.flags = flags;
        this.expectedErrors = expectedErrors;
        this.expectedDeadCodePoints = expectedDeadCodePoints;
        this.loopUnrollCap = loopUnrollCap;
    }

    public static List<BCCheckDefinition> parseDefinitions(File rootDir, File csvFile){
        LabeledCSVParser parser;
        File invFile;
        File preconditionsFile;
        File localInvariantFile;
        String localInvariantFileName;
        String precondFileName;
        String[] generatorFlags;
        int expectedErrorCount;
        int deadCodePoints;
        int loopUnrollCap;
        
        List<BCCheckDefinition> libTestCases = new ArrayList<BCCheckDefinition>();
        try{
            parser = new LabeledCSVParser(new CSVParser(FileUtils.openInputStream(csvFile)));
            while(parser.getLine() != null){
                invFile = new File(rootDir, parser.getValueByLabel("invariant_file"));
                localInvariantFileName = parser.getValueByLabel("local_invariant");
                if(!localInvariantFileName.isEmpty()){
                    localInvariantFile = new File(rootDir, localInvariantFileName);
                } else {
                    localInvariantFile = null;
                }
                precondFileName = parser.getValueByLabel("preconditions_file");
                if(!precondFileName.isEmpty()){
                    preconditionsFile = new File(rootDir, precondFileName);
                } else {
                    preconditionsFile = null;
                }
                generatorFlags = parser.getValueByLabel("flags").split("[ ]+");
                expectedErrorCount = Integer.parseInt(parser.getValueByLabel("expected_errors"));
                deadCodePoints = Integer.parseInt(parser.getValueByLabel("dead_code_points"));
                loopUnrollCap = Integer.parseInt(parser.getValueByLabel("loop_unroll_cap"));
                libTestCases.add(new BCCheckDefinition(rootDir, invFile, localInvariantFile, preconditionsFile, generatorFlags, expectedErrorCount, deadCodePoints, loopUnrollCap));
            }
        } catch(IOException e){
            Logger.getLogger(BCCheckDefinition.class).warn("Could not open check definition file for library "+rootDir.getName());
        }
        return libTestCases;
    }
    
    @Override
    public String toString() {
        URI libDirUri = libDir.toURI();
        StringBuilder builder = new StringBuilder();
        builder.append(libDir.getName());
        builder.append(": ");
        builder.append("inv: ");
        builder.append(libDirUri.relativize(invariant.toURI()));
        builder.append(", localInv: ");
        if(localInv != null){
            builder.append(libDirUri.relativize(localInv.toURI()));
        } else {
            builder.append("none");
        }
        builder.append(", pre: ");
        if(preconditions != null){
            builder.append(libDirUri.relativize(preconditions.toURI()));
        } else {
            builder.append("none");
        }
        builder.append(", flags: ");
        builder.append(Arrays.toString(flags));
        builder.append(", errors: ");
        builder.append(expectedErrors);
        builder.append(", dead code: ");
        builder.append(expectedDeadCodePoints);
        builder.append(", loop unroll: ");
        builder.append(loopUnrollCap);
        return builder.toString();
    }
}

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

import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

public class BCCheckDefinition {
    private File libDir;
    private File specification;
    private String[] flags;
    private int expectedErrors;
    private int expectedDeadCodePoints;
    private int loopUnrollCap;
    private int checkIndex;
	private CheckRunException exception = null;

    public int getCheckIndex() {
        return checkIndex;
    }

    public File getSpecification() {
        return specification;
    }

    public File getLibDir() {
        return libDir;
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

    public BCCheckDefinition(int index, File libDir, File specification,
            String[] flags, int expectedErrors, int expectedDeadCodePoints,
            int loopUnrollCap) {
        this.checkIndex = index;
        this.libDir = libDir;
        this.specification = specification;
        this.flags = flags;
        this.expectedErrors = expectedErrors;
        this.expectedDeadCodePoints = expectedDeadCodePoints;
        this.loopUnrollCap = loopUnrollCap;
    }

    public static List<BCCheckDefinition> parseDefinitions(File rootDir, File csvFile){
        LabeledCSVParser parser;
        File specFile;
        String[] generatorFlags;
        int expectedErrorCount;
        int deadCodePoints;
        int loopUnrollCap;
        
        List<BCCheckDefinition> libTestCases = new ArrayList<BCCheckDefinition>();
        try{
            parser = new LabeledCSVParser(new CSVParser(FileUtils.openInputStream(csvFile)));
            int i = 0;
            while(parser.getLine() != null){
                specFile = new File(rootDir, parser.getValueByLabel("specification"));
                generatorFlags = parser.getValueByLabel("flags").split("[ ]+");
                expectedErrorCount = Integer.parseInt(parser.getValueByLabel("expected_errors"));
                deadCodePoints = Integer.parseInt(parser.getValueByLabel("dead_code_points"));
                loopUnrollCap = Integer.parseInt(parser.getValueByLabel("loop_unroll_cap"));
                libTestCases.add(new BCCheckDefinition(i, rootDir, specFile, generatorFlags, expectedErrorCount, deadCodePoints, loopUnrollCap));
                i++;
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
        builder.append("spec: ");
        builder.append(libDirUri.relativize(specification.toURI()));
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

	public void setException(CheckRunException e) {
		this.exception = e;
	}
	
	public CheckRunException getException() {
		return exception;
	}
}

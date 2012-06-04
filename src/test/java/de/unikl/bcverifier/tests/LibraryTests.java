package de.unikl.bcverifier.tests;

import static org.junit.Assert.*;
import static junitparams.JUnitParamsRunner.$;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

@RunWith(JUnitParamsRunner.class)
public class LibraryTests {	
	File libpath = new File("libraries");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	Object[] params() {
		List<File> dirs = new ArrayList<File>();
		for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
            dirs.add(new File(libpath, path));
        }
		return dirs.toArray();
	}
	
	File lib(String name){
	    return new File(libpath, name);
	}
	
	Object[] librariesToCheck() {
	    String libraryToCheck = System.getProperty("library");
	    if(libraryToCheck!=null){
	        return buildTestCase(new File(libraryToCheck)).toArray();
	    } else {
	        List<Object> testSets = new ArrayList<Object>();
	        List<Object[]> libTestCases;
	        for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
	            libTestCases = buildTestCase(new File(libpath, path));
	            testSets.addAll(libTestCases);
	        }
	        return testSets.toArray();
	    }
	}

    private List<Object[]> buildTestCase(File libDir) {
        LabeledCSVParser parser;
        File testsFile;
        File invFile;
        File preconditionsFile;
        String precondFileName;
        String[] generatorFlags;
        int expectedErrorCount;
        int deadCodePoints;
        int loopUnrollCap;
        testsFile = new File(libDir, "tests.csv");
        
        List<Object[]> libTestCases = new ArrayList<Object[]>();
        try{
            parser = new LabeledCSVParser(new CSVParser(FileUtils.openInputStream(testsFile)));
            while(parser.getLine() != null){
                invFile = new File(libDir, parser.getValueByLabel("invariant_file"));
                precondFileName = parser.getValueByLabel("preconditions_file");
                if(!precondFileName.isEmpty()){
                    preconditionsFile = new File(libDir, precondFileName);
                } else {
                    preconditionsFile = null;
                }
                generatorFlags = parser.getValueByLabel("flags").split("[ ]+");
                expectedErrorCount = Integer.parseInt(parser.getValueByLabel("expected_errors"));
                deadCodePoints = Integer.parseInt(parser.getValueByLabel("dead_code_points"));
                loopUnrollCap = Integer.parseInt(parser.getValueByLabel("loop_unroll_cap"));
                libTestCases.add($(libDir, invFile, preconditionsFile, generatorFlags, expectedErrorCount, deadCodePoints, loopUnrollCap));
            }
        } catch(IOException e){
            Logger.getLogger(LibraryTests.class).warn("Could not open tests file for library "+libDir.getName());
        }
        return libTestCases;
    }
	
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(File dir, File invariant, File preconditions, String[] generatorFlags, int expectedErrorCount, int expectedDeadCodePoints, int loopUnrollCap) throws TranslationException {
		Configuration config = new Configuration();
		JCommander parser = new JCommander();
		parser.addObject(config);
		parser.setProgramName("Main");
		
		try {
            parser.parseWithoutValidation(generatorFlags);
        } catch (ParameterException e) {
            Logger.getLogger(LibraryTests.class).warn(e);
        }
		
		File specificationFile = new File(dir, "bpl/output.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setInvariant(invariant);
		config.setConfigFile(preconditions);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
		config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(loopUnrollCap);
		Library library = new Library(config);
		library.compile();
		library.translate();
		library.check();
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == expectedErrorCount);
	}
	
	@Test @Parameters(method = "librariesToCheck")
    public void smokeTestLibrary(File dir, File invariant, File preconditions, String[] generatorFlags, int expectedErrorCount, int expectedDeadCodePoints, int loopUnrollCap) throws TranslationException, IOException {
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(generatorFlags);
        } catch (ParameterException e) {
            Logger.getLogger(LibraryTests.class).warn(e);
        }
        
        File invFile = new File(dir, "bpl/inv.bpl");
        File specificationFile = new File(dir, "bpl/output.bpl");
        File lib1 = new File(dir, "old");
        File lib2 = new File(dir, "new");
        config.setInvariant(invFile);
        config.setConfigFile(preconditions);
        config.setLibraries(lib1, lib2);
        config.setNullChecks(false);
        config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(loopUnrollCap);
        config.setSmokeTestOn(true);
        Library library = new Library(config);
        library.compile();
        library.translate();
        library.check();
        assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == expectedErrorCount);
        
        File boogieOutput = new File(dir, "bpl/boogie_output.txt");
        FileUtils.write(boogieOutput, BoogieRunner.getLastMessage());
        
        assertTrue(String.format("Expected %d dead code points, but got %d", expectedDeadCodePoints, BoogieRunner.getLastUnreachalbeCodeCount()), BoogieRunner.getLastUnreachalbeCodeCount() == expectedDeadCodePoints);
    }
	
	@Test @Parameters(method = "params")
	public void genLibrary(File dir) throws TranslationException {
		Configuration config = new Configuration();
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/output.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setInvariant(invFile);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
        config.setAction(VerifyAction.TYPECHECK);
		Library library = new Library(config);
		library.compile();
		library.translate();
		library.check();
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

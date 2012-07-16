package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.BCCheckDefinition;

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
	        List<BCCheckDefinition> libTestCases;
	        for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
	            libTestCases = buildTestCase(new File(libpath, path));
	            testSets.addAll(libTestCases);
	        }
	        return testSets.toArray();
	    }
	}

    private List<BCCheckDefinition> buildTestCase(File libDir) {
        return BCCheckDefinition.parseDefinitions(libDir, new File(libDir, "tests.csv"));
    }
	
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(BCCheckDefinition test) throws TranslationException {
		Configuration config = new Configuration();
		JCommander parser = new JCommander();
		parser.addObject(config);
		parser.setProgramName("Main");
		
		try {
            parser.parseWithoutValidation(test.getFlags());
        } catch (ParameterException e) {
            Logger.getLogger(LibraryTests.class).warn(e);
        }
		
		File specificationFile = new File(test.getLibDir(), "bpl/output.bpl");
		File lib1 = new File(test.getLibDir(), "old");
		File lib2 = new File(test.getLibDir(), "new");
		config.setInvariant(test.getInvariant());
		config.setConfigFile(test.getPreconditions());
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
		config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(test.getLoopUnrollCap());
        TranslationController tc = new TranslationController();
		Library library = new Library(config);
		library.setTranslationController(tc);
		library.compile();
		library.translate();
		library.check();
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == test.getExpectedErrors());
	}
	
	@Test @Parameters(method = "librariesToCheck")
    public void smokeTestLibrary(BCCheckDefinition test) throws TranslationException, IOException {
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(test.getFlags());
        } catch (ParameterException e) {
            Logger.getLogger(LibraryTests.class).warn(e);
        }
        
        File specificationFile = new File(test.getLibDir(), "bpl/output.bpl");
        File lib1 = new File(test.getLibDir(), "old");
        File lib2 = new File(test.getLibDir(), "new");
        config.setInvariant(test.getInvariant());
        config.setConfigFile(test.getPreconditions());
        config.setLibraries(lib1, lib2);
        config.setNullChecks(false);
        config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(test.getLoopUnrollCap());
        config.setSmokeTestOn(true);
        TranslationController tc = new TranslationController();
        Library library = new Library(config);
        library.setTranslationController(tc);
        library.compile();
        library.translate();
        library.check();
        assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == test.getExpectedErrors());
        
        File boogieOutput = new File(test.getLibDir(), "bpl/boogie_output.txt");
        FileUtils.write(boogieOutput, BoogieRunner.getLastMessage());
        
        assertTrue(String.format("Expected %d dead code points, but got %d", test.getExpectedDeadCodePoints(), BoogieRunner.getLastUnreachalbeCodeCount()), BoogieRunner.getLastUnreachalbeCodeCount() == test.getExpectedDeadCodePoints());
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
        TranslationController tc = new TranslationController();
		Library library = new Library(config);
		library.setTranslationController(tc);
		library.compile();
		library.translate();
		library.check();
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

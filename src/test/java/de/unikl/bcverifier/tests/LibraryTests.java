package de.unikl.bcverifier.tests;

import static org.junit.Assert.*;
import static junitparams.JUnitParamsRunner.$;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	    return $(
	            // library name     , expected errors, expected dead code points, loop unroll cap
	            $(lib("callTest")   , 0              , 3                        , 3),
	            $(lib("cb")         , 0              , 2                        , 3),
	            $(lib("cbext")      , 0              , 1                        , 3),    //takes long to smoke
	            $(lib("cell")       , 0              , 8                        , 2),
	            $(lib("cell2")      , 0              , 9                        , 2),
	            $(lib("constructor"), 0              , 5                        , 3),
	            $(lib("freshnames") , 0              , 11                       , 3),
	            $(lib("list")       , 1              , 0                        , 3),
	            $(lib("subtypes")   , 1              , 0                        , 3),
	            $(lib("test")       , 0              , 2                        , 3)
	            );
	}
	
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(File dir, int expectedErrorCount, int expectedDeadCodePoints, int loopUnrollCap) throws TranslationException {
		Configuration config = new Configuration();
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/specification.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setInvariant(invFile);
		config.setLibraries(lib1, lib2);
		config.setNullChecks(false);
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
    public void smokeTestLibrary(File dir, int expectedErrorCount, int expectedDeadCodePoints, int loopUnrollCap) throws TranslationException, IOException {
        Configuration config = new Configuration();
        File invFile = new File(dir, "bpl/inv.bpl");
        File specificationFile = new File(dir, "bpl/specification.bpl");
        File lib1 = new File(dir, "old");
        File lib2 = new File(dir, "new");
        config.setInvariant(invFile);
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
		File specificationFile = new File(dir, "bpl/specification.bpl");
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

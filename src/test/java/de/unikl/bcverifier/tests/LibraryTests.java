package de.unikl.bcverifier.tests;

import static org.junit.Assert.*;
import static junitparams.JUnitParamsRunner.$;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

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
	    // library name, expected errors, expected dead code points
	    return $(
	            $(lib("callTest")   , 0, 0),
	            $(lib("cb")         , 0, 0),
	            $(lib("cbext")      , 0, 0),
	            $(lib("cell")       , 0, 0),
	            $(lib("cell2")      , 0, 0),
	            $(lib("constructor"), 0, 0),
	            $(lib("freshnames") , 0, 0),
	            $(lib("list")       , 1, 0),
	            $(lib("subtypes")   , 1, 0),
	            $(lib("test")       , 0, 0)
	            );
	}
	
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(File dir, int expectedErrorCount, int expectedDeadCodePoints) throws TranslationException {
		Configuration config = new Configuration();
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/specification.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setInvariant(invFile);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
		Library library = new Library(config);
		library.compile();
		library.translate();
		library.check(true);
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == expectedErrorCount);
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
		Library library = new Library(config);
		library.compile();
		library.translate();
		library.check(false);
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

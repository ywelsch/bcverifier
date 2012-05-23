package de.unikl.bcverifier.tests;

import static org.junit.Assert.*;

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
	
	@Test @Parameters(method = "params")
	public void verifyLibrary(File dir) throws TranslationException {
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/specification.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		Library library = new Library(invFile, lib1, lib2, specificationFile);
		library.compile();
		library.translate();
		library.check(true);
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
	
	@Test @Parameters(method = "params")
	public void genLibrary(File dir) throws TranslationException {
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/specification.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		Library library = new Library(invFile, lib1, lib2, specificationFile);
		library.compile();
		library.translate();
		library.check(false);
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

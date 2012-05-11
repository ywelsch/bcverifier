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
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

@RunWith(JUnitParamsRunner.class)
public class LibraryTests {	
	File libpath = new File("libraries/");
	
	Object[] parametersForVerifyLibrary() {
		List<File> dirs = new ArrayList<File>();
		for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
            dirs.add(new File(libpath, path));
        }
		return dirs.toArray();
	}
	
	@Test @Parameters
	public void verifyLibrary(File dir) throws TranslationException {
		Library library = new Library(dir);
		library.compile();
		library.translate();
		library.check(true);
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}

}

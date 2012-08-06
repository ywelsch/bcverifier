package de.unikl.bcverifier.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import de.unikl.bcverifier.helpers.BCCheckDefinition;

public class AbstractLibraryTests {
	File libpath = new File("libraries");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.WARN);
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
}

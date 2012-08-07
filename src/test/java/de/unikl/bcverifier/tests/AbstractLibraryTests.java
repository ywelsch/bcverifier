package de.unikl.bcverifier.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;

import static junitparams.JUnitParamsRunner.$;

import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

public class AbstractLibraryTests {
	File libpath = new File("libraries");
	File truespec = new File(libpath, "true/bpl/spec.isl");
	
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
	
	Object[] boogieFilesToCheck() throws CheckRunException {
	    File outputdir = new File(FileUtils.getTempDirectory(), "BCCheck_"+System.currentTimeMillis());
	    outputdir.mkdirs();
	    File tmpLibPath;
	    
	    List<Object> testSets = new ArrayList<Object>();
        List<BCCheckDefinition> libTestCases;
        for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
            libTestCases = buildTestCase(new File(libpath, path));
            for(BCCheckDefinition test : libTestCases){
                tmpLibPath = new File(outputdir, path);
                tmpLibPath.mkdirs();
                CheckRunner.generate(test, tmpLibPath);
                testSets.add($(new File(tmpLibPath, "output"+test.getCheckIndex()+".bpl"), test));
            }
        }
        return testSets.toArray();
	}
	
	Object[] libraryFolders() {
		List<File> dirs = new ArrayList<File>();
		for(String path : libpath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
            dirs.add(new File(libpath, path));
        }
		return dirs.toArray();
	}

    private List<BCCheckDefinition> buildTestCase(File libDir) {
        return BCCheckDefinition.parseDefinitions(libDir, new File(libDir, "tests.csv"));
    }
}

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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

public class AbstractLibraryTestsNG {
	File libpath = new File("libraries");
	File truespec = new File(libpath, "true/bpl/spec.isl");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.INFO);
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
	
	@DataProvider(name = "boogieFiles", parallel = true)
	Object[][] boogieFilesToCheck() throws CheckRunException {
	    String libraryToCheck = System.getProperty("library");
	    if(libraryToCheck!=null){
	        Logger.getLogger(AbstractLibraryTestsNG.class).info("Generating Boogie files for library "+libraryToCheck+".");
	        List<BCCheckDefinition> libTestCases = buildTestCase(new File(libraryToCheck));
	        List<Object> testSets = new ArrayList<Object>();
	        File bplDir;
	        for(BCCheckDefinition test : libTestCases){
	            bplDir = new File(test.getLibDir(), "bpl");
	            CheckRunner.generate(test, bplDir);
	            testSets.add(new Object[]{test, new File(bplDir, "output"+test.getCheckIndex()+".bpl")});
	        }
	        Logger.getLogger(AbstractLibraryTestsNG.class).info("Finished generating Boogie files.");
	        return testSets.toArray(new Object[testSets.size()][2]);
	    } else {
    	    Logger.getLogger(AbstractLibraryTestsNG.class).info("Generating Boogie files.");
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
                    testSets.add(new Object[]{test, new File(tmpLibPath, "output"+test.getCheckIndex()+".bpl")});
                }
            }
            Logger.getLogger(AbstractLibraryTestsNG.class).info("Finished generating Boogie files.");
            return testSets.toArray(new Object[testSets.size()][2]);
	    }
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
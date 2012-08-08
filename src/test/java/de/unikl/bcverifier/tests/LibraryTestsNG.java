package de.unikl.bcverifier.tests;

import static org.testng.AssertJUnit.assertTrue;
import java.io.File;

import org.testng.annotations.Test;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;
import de.unikl.bcverifier.helpers.BCCheckDefinition;

public class LibraryTestsNG extends AbstractLibraryTestsNG {
	
	@Test(dataProvider = "boogieFiles")
	public void verifyBoogieFiles(BCCheckDefinition test, File boogieFile) throws BoogieRunException{
	    BoogieRunner runner = new BoogieRunner();
	    runner.setVerify(true);
	    runner.runBoogie(boogieFile);
	    assertTrue(runner.getLastMessage(), runner.getLastErrorCount() == test.getExpectedErrors());
	}
}

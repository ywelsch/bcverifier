package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.VerificationResult;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

@RunWith(JUnitParamsRunner.class)
public class LibraryTests extends AbstractLibraryTests {
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(BCCheckDefinition test) throws CheckRunException {
	    VerificationResult result = CheckRunner.runCheck(test);
		assertTrue(result.getLastMessage(), result.isLastRunSuccess());
	}
	
	@Ignore @Test @Parameters(method = "boogieFilesToCheck")
	public void verifyBoogieFiles(File boogieFile, BCCheckDefinition test) throws BoogieRunException{
	    BoogieRunner runner = new BoogieRunner();
	    runner.setVerify(true);
	    runner.runBoogie(boogieFile);
	    assertTrue(runner.getLastMessage(), runner.getLastErrorCount() == test.getExpectedErrors());
	}
}

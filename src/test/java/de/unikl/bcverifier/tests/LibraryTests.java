package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

@RunWith(JUnitParamsRunner.class)
public class LibraryTests extends AbstractLibraryTests {
	@Ignore @Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(BCCheckDefinition test) throws CheckRunException {
		assertTrue(BoogieRunner.getLastMessage(), CheckRunner.runCheck(test));
	}
	
	@Test @Parameters(method = "boogieFilesToCheck")
	public void verifyBoogieFiles(File boogieFile, BCCheckDefinition test) throws BoogieRunException{
	    BoogieRunner.setVerify(true);
	    BoogieRunner.runBoogie(boogieFile);
	    assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastErrorCount() == test.getExpectedErrors());
	}
}

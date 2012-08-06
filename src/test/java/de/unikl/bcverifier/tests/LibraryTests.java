package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

@RunWith(JUnitParamsRunner.class)
public class LibraryTests extends AbstractLibraryTests {	
	@Test @Parameters(method = "librariesToCheck")
	public void verifyLibrary(BCCheckDefinition test) throws CheckRunException {
		assertTrue(BoogieRunner.getLastMessage(), CheckRunner.runCheck(test));
	}
}

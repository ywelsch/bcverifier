package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.VerificationResult;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

@RunWith(JUnitParamsRunner.class)
public class SmokeLibraryTests {
	@Test @Parameters(method = "librariesToCheck")
    public void smokeTestLibrary(BCCheckDefinition test) throws CheckRunException {
        VerificationResult result = CheckRunner.runSmokeTest(test);
	    assertTrue(String.format("Expected %d dead code points, but got %d", test.getExpectedDeadCodePoints(), result.getLastUnreachableCodeCount()),
	            result.isLastRunSuccess());
    }
}

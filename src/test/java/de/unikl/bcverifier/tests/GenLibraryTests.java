package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.VerificationResult;
import de.unikl.bcverifier.sourcecomp.SourceInCompatibilityException;
import de.unikl.bcverifier.specification.GenerationException;

@RunWith(JUnitParamsRunner.class)
public class GenLibraryTests extends AbstractLibraryTests {
	@Test @Parameters(method = "libraryFolders")
	public void genLibrary(File dir) throws TranslationException, GenerationException, SourceInCompatibilityException, CompileException {
		Configuration config = new Configuration();
		File invFile = new File(dir, "bpl/spec.bsl");
		if (!invFile.exists()) {
			invFile = truespec;
		}
		File specificationFile = new File(dir, "bpl/output.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setSpecification(invFile);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
        config.setAction(VerifyAction.TYPECHECK);
		Library library = new Library(config);
		VerificationResult result = library.runLifecycle();
		assertTrue(result.getLastMessage(), result.isLastRunSuccess());
	}
}

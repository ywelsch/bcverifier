package de.unikl.bcverifier.tests;

import java.io.File;

import junit.framework.Assert;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.sourcecomp.SourceInCompatibilityException;
import de.unikl.bcverifier.specification.GenerationException;

@RunWith(JUnitParamsRunner.class)
public class SourceCompatibilityTests extends AbstractLibraryTests {
	@Test @Parameters(method = "libraryFolders")
	public void checkSourceCompatibility(File dir) throws TranslationException, GenerationException, SourceInCompatibilityException, CompileException {
		try {
		Configuration config = new Configuration();
		File invFile = truespec;
		File specificationFile = new File(dir, "bpl/output.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setSpecification(invFile);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
        config.setAction(VerifyAction.NONE);
        config.setCheckSourceCompatibility(true);
		Library library = new Library(config);
		library.runLifecycle();
		Assert.assertTrue(true);
		} catch (SourceInCompatibilityException si) {
			Assert.assertTrue(si.getMessage(), false);
		}
	}
}

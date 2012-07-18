package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.specification.GenerationException;
import de.unikl.bcverifier.specification.GeneratorFactory;

public class SingleLibraryTest {
	File dir = new File("libraries/cell");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	@Test
	public void verifySingleLibrary() throws TranslationException, GenerationException {
		Configuration config = new Configuration();
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/output.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		config.setSpecification(invFile);
		config.setLibraries(lib1, lib2);
		config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        TranslationController tc = new TranslationController();
		Library library = new Library(config, GeneratorFactory.getGenerator(config));
		library.setTranslationController(tc);
		library.compile();
		library.translate();
		library.check();
		System.out.println(BoogieRunner.getLastMessage());
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

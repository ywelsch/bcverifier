package de.unikl.bcverifier.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class SingleLibraryTest {
	File dir = new File("libraries/cbext");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	@Test
	public void verifySingleLibrary() throws TranslationException {
		File invFile = new File(dir, "bpl/inv.bpl");
		File specificationFile = new File(dir, "bpl/specification.bpl");
		File lib1 = new File(dir, "old");
		File lib2 = new File(dir, "new");
		Library library = new Library(invFile, lib1, lib2, specificationFile);
		library.compile();
		library.translate(true);
		library.check(true);
		System.out.println(BoogieRunner.getLastMessage());
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

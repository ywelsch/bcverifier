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
	File libpath = new File("libraries/cbext");
	
	@BeforeClass
	public static void oneTimeSetup() {
		Logger.getRootLogger().setLevel(Level.WARN);
	}
	
	@Test
	public void verifySingleLibrary() throws TranslationException {
		Library library = new Library(libpath);
		library.compile();
		library.translate();
		library.check(true);
		System.out.println(BoogieRunner.getLastMessage());
		assertTrue(BoogieRunner.getLastMessage(), BoogieRunner.getLastReturn());
	}
}

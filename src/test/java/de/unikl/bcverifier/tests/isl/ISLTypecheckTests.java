package de.unikl.bcverifier.tests.isl;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import beaver.Parser.Exception;

import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.isl.ast.CompilationUnit;

public class ISLTypecheckTests extends ISLTest {

	@Test
	public void operators() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant 3 + 2 > 4;",
				"invariant true ==> false;",
				"invariant forall old Cell c1, new Cell c2 :: c1.c ~ c2.c1;",
				"invariant forall old Cell c1, old Cell c2 :: c1.c != c2.c;"
				);
		testTypeCheckOk(
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	@Test
	public void compareDifferentVersion() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall old Cell c1, new Cell c2 :: c1.c == c2.c1;"
				);
		testTypeCheckError("Cannot compare",
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	
	@Test
	public void compareDifferentTypes() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant 3 != true;"
				);
		testTypeCheckError("Cannot compare",
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	@Test
	public void multErr() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant 3 * true;"
				);
		testTypeCheckError("Expected expression of type int",
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	@Test
	public void strangeInstanceof() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				// TODO should this really be allowed?
				"invariant forall new Cell c :: c.c1 instanceof (if c.f then new Cell else new Cell);"
				);
		testTypeCheckOk( 
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
}

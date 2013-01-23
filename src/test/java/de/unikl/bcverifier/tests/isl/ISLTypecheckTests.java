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
				"invariant 3 + 2 * -1 > 4;",
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
	
	@Test
	public void invalidMemberAccess() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall int i :: i.x == 0;"
				);
		testTypeCheckError("Left hand side of member access is of type int", 
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	@Test
	public void tooManyArgs() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call notifyRec in line 24 of new Observable nosync;",
				"invariant eval(pcall, 1, 2, 3);"
				);
		testTypeCheckError("Too many arguments", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void missingArgs() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call notifyRec in line 24 of new Observable nosync;",
				"invariant eval(pcall);"
				);
		testTypeCheckError("Missing arguments", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void invariantType() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant 42;"
				);
		testTypeCheckError("Expected expression of type boolean but found int", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void invalidIf() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant if true then 3 else false;"
				);
		testTypeCheckError("Types int and boolean are not equal", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void invalidInstanceof1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant 3 instanceof new Observer;"
				);
		testTypeCheckError("instanceof expects a java type", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void invalidInstanceof2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall new Observer o :: o instanceof o;"
				);
		testTypeCheckError("instanceof expects a reference to a java type", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void invalidInstanceof3() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall new Observer o :: o instanceof old Observer;"
				);
		testTypeCheckError("same library implementation", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void fulyQualifiedName() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall new cell.Cell c :: c instanceof new cell.Cell;"
				);
		testTypeCheckOk( 
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
	}
	
	@Test
	public void lineNrProgramPointFail1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = line 24 of new java.lang.Object;"
				);
		testTypeCheckError("java.lang.Object is not part of the given library", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void localPlaceFail1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place p = line 24 of old Observable stall when 3 < 4",
				"	with measure 5 < 6;"
				);
		testTypeCheckError("int but found bool", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void lineNrProgramPointFail2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = line 24 of int;"
				);
		testTypeCheckError("must refer to a Java type", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call notifyRec in line 24 of new java.lang.Object;"
				);
		testTypeCheckError("java.lang.Object is not part of the given library", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call notifyRec in line 24 of int;"
				);
		testTypeCheckError("must refer to a Java type", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail3() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call notifyRec in line 23 of new Observable;"
				);
		testTypeCheckError("No method call found in this line", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail4() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place pcall = call m in line 24 of new Observable;"
				);
		testTypeCheckError("No method call found in this line", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail5() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"place pcall = call notifyRec in line 24 of new Observable when 3 > 2;"
				);
		testTypeCheckError("Observable places must not have a condition", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	@Test
	public void callProgramPointFail6() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"place pcall = call notifyRec in line 24 of new Observable stall when 3 > 2;"
				);
		testTypeCheckError("Observable places must not have a stall condition", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	

	@Test
	public void duplicateName() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"var int x = 1;",
				"var int x = 2;"
				);
		testTypeCheckError("An element with name x is already defined", 
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
}

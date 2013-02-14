package de.unikl.bcverifier.tests.isl;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import beaver.Parser.Exception;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.isl.ast.CompilationUnit;

public class ISLParserTest extends ISLTest {

	
	@Test
	public void nullExpr() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant null.f == null;"
				);
		testTypeCheckError("Field f could not be found",
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void localPlaceAtCall1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place callNotifyMe1 = call notifyMe in line 8 of old C;"
				);
		testTypeCheckError("sync support for local places",
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void localPlaceAtCall_nosync() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place callNotifyMe1 = call notifyMe in line 8 of old C nosync;"
				);
		testTypeCheckOk(
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void err_placecheck2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"place callNotifyMe1 = line 8 of old C;"
				);
		testTypeCheckError("Observable places can not be defined within the library implementation",
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void predefinedplace_nosplit() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"place callNotifyMe1 = call notifyMe in line 8 of old C nosplit;"
				);
		testTypeCheckOk(
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
		translateAndPrint(cu);
	}
	
	
	@Test
	public void iframeDestroy() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(Files.toString(new File("./examples/iframeDestroy/bpl/spec.isl"), Charsets.UTF_8));
		testTypeCheckOk(
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void cell() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall old Cell o1, new Cell o2 ::",
					"o1 ~ o2 ==>",
						"if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2;");
		testTypeCheckOk(
				new File("./examples/cell/old"), 
				new File("./examples/cell/new"), cu);
		System.out.println("cell output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void cb() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk("invariant forall old A a :: a.g % 2 == 0;");
		testTypeCheckOk(
				new File("./examples/cb/old"), 
				new File("./examples/cb/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void localLoop() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(Files.toString(new File("./examples/localLoop/bpl/spec4.isl"), Charsets.UTF_8));
		testTypeCheckOk(
				new File("./examples/localLoop/old"), 
				new File("./examples/localLoop/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void localLoop_err1() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
			"local place inLoop1 = line 14 of old C;",
			"local place inLoop2 = line 8 of new C;",
			"// both libraries are in the loop at the same time.	", 
			"	local invariant at(inLoop1) <==> at(inLoop2);",
			"//the values of the variables are the same",
			"	local invariant at(inLoop1) && at(inLoop2) ==> ",
			"		   stack(inLoop1, n) == stack(inLoop2, n)",
			"		&& stack(inLoop1, x) == stack(inLoop2, x)",
			"		&& stack(inLoop1, i) == stack(inLoop2, i);"
			);
		testTypeCheckError("No statement found",
				new File("./examples/localLoop/old"), 
				new File("./examples/localLoop/new"), cu);
	}
	

	@Test
	public void obool() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;",
				"invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;",
				// internal:
				"invariant forall old OBool o :: createdByLibrary(o.g) && !exposed(o.g);",
				"invariant forall new OBool o :: createdByLibrary(o.g) && !exposed(o.g);",
				// nonnull:
				"invariant forall old OBool o :: o.g != null;",
				"invariant forall new OBool o :: o.g != null;",
				// unique
				"invariant forall old OBool o1, old OBool o2 :: o1 != o2 ==> o1.g != o2.g;",
				"invariant forall new OBool o1, new OBool o2 :: o1 != o2 ==> o1.g != o2.g;"
				);
		testTypeCheckOk(
				new File("./examples/obool/old"), 
				new File("./examples/obool/new"), cu);
		System.out.println("obool output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void testVars() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
		"var binrelation bij = add(empty(), null, null);",
		"var old Node x1 = null;",
		"var new util.Node x2 = null;",
		"var int i = 42;",
		"var boolean b = true;"		
				);
		testTypeCheckOk(
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
		translateAndPrint(cu);
	}
	
	@Test
	public void testVars_typeNotFound() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
		"var new does.not.Exist x = null;"
				);
		testTypeCheckError("Could not find class does.not.Exist",
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
		translateAndPrint(cu);
	}
	
	@Test
	public void testInvalidComparison2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
		"var old Node n1 = null;",
		"var int n2 = 0;",
		"invariant n1 == n2;"
				);
		testTypeCheckError("Cannot compare types",
				new File("./examples/list/old"), 
				new File("./examples/list/new"), cu);
	}
	
	
	
	
	
}

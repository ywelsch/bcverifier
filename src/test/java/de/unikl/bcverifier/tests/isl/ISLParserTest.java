package de.unikl.bcverifier.tests.isl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


import org.apache.wicket.util.string.Strings;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLExpression;
import beaver.Parser.Exception;
import de.unikl.bcverifier.LibraryCompiler;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.Statement;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;
import de.unikl.bcverifier.librarymodel.LibrarySource;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;
import de.unikl.bcverifier.specification.SpecExpr;

public class ISLParserTest {

	@Test
	public void err_placecheck() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"local place callNotifyMe1 = call notifyMe in line 8 of old C;"
				);
		testTypeCheckError("Invalid place definition",
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void err_placecheck2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"predefined place callNotifyMe1 = line 8 of old C;"
				);
		testTypeCheckError("Invalid place definition",
				new File("./examples/iframeDestroy/old"), 
				new File("./examples/iframeDestroy/new"), cu);
	}
	
	@Test
	public void predefinedplace_splitvc() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"predefined place (splitvc) callNotifyMe1 = call notifyMe in line 8 of old C;"
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
	public void localLoop_err2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
			"local place inLoop1 = line 7 of old C when at(inLoop2);",
			"local place inLoop2 = line 8 of new C;",
			"// both libraries are in the loop at the same time.	", 
			"	local invariant at(inLoop1) <==> at(inLoop2);",
			"//the values of the variables are the same",
			"	local invariant at(inLoop1) && at(inLoop2) ==> ",
			"		   stack(inLoop1, n) == stack(inLoop2, n)",
			"		&& stack(inLoop1, x) == stack(inLoop2, x)",
			"		&& stack(inLoop1, i) == stack(inLoop2, i);"
			);
		testTypeCheckError("Function 'at' must not be used in local place definitions.",
				new File("./examples/localLoop/old"), 
				new File("./examples/localLoop/new"), cu);
	}

	@Test
	public void obool() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;",
				"invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;",
				// internal:
				"invariant forall old OBool o :: !createdByCtxt(o.g) && !exposed(o.g);",
				"invariant forall new OBool o :: !createdByCtxt(o.g) && !exposed(o.g);",
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
	public void obool2() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				// internal:
				"invariant forall old OBool o :: !createdByCtxt(o.g) && !exposed(o.g);",
				"invariant forall new OBool o :: !createdByCtxt(o.g) && !exposed(o.g);",
				// nonnull:
				"invariant forall old OBool o :: o.g != null;",
				"invariant forall new OBool o :: o.g != null;",
				// unique
				"invariant exists Bijection relbij :: forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g instanceof old Bool && o2.g instanceof new Bool && related(relbij, o1.g, o2.g);",
				"invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;",
				"invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;"
				);
		testTypeCheckOk(
				new File("./examples/obool/old"), 
				new File("./examples/obool/new"), cu);
		System.out.println("obool2 output:");
		translateAndPrint(cu);
	}
	
	
	protected CompilationUnit testParseOk(String ... lines) throws IOException, Exception {
		ISLCompiler parser = new ISLCompiler(Strings.join("\n", lines));
		Object res = parser.parse();
		for (ParserError err : parser.getErrors()) {
			System.err.println(err);
			assertTrue("" + err, false);
		}
		assertNotNull(res);
		return (CompilationUnit) res;
	}
	
	protected void testTypeCheckOk(File oldLib, File newLib, CompilationUnit cu) throws CompileException {
		TwoLibraryModel twoLibraryModel = new TwoLibraryModel(oldLib, newLib);
		cu.setTwoLibraryModel(twoLibraryModel);
		cu.setBuiltinFunctions(new BuiltinFunctions(twoLibraryModel ));
		cu.typecheck();
		for (TypeError err : cu.getErrors()) {
			System.err.println(err);
			assertTrue("" + err, false);
		}
	}
	
	protected void testTypeCheckError(String expected, File oldLib, File newLib, CompilationUnit cu) throws CompileException {
		TwoLibraryModel twoLibraryModel = new TwoLibraryModel(oldLib, newLib);
		cu.setTwoLibraryModel(twoLibraryModel);
		cu.setBuiltinFunctions(new BuiltinFunctions(twoLibraryModel ));
		cu.typecheck();
		boolean errFound = false;
		for (TypeError err : cu.getErrors()) {
			System.err.println(err);
			if (err.toString().contains(expected)) {
				errFound = true;
			}
		}
		assertTrue(errFound);
	}
	
	protected void translateAndPrint(CompilationUnit cu) {
		PrintWriter pw = new PrintWriter(System.out);
		
		pw.println("Invariants: ");
		for (SpecExpr inv : cu.generateInvariants()) {
			print(pw, inv);
		}
		pw.println("Local Invariants: ");
		for (SpecExpr inv : cu.generateLocalInvariants()) {
			print(pw, inv);
		}
		pw.println("Prelude Additions: ");
		for (String s : cu.generatePreludeAddition()) {
			pw.println(s);
		}
		pw.println("Prelude Additions: ");
		for (String s : cu.generatePreconditions()) {
			pw.println(s);
		}
		pw.flush();
	}

	private void print(PrintWriter pw, SpecExpr inv) {
		BPLPrinter printer = new BPLPrinter(pw);
		pw.println("// " + inv.getComment());
		pw.println("// welldefinedness:");
		inv.getWelldefinednessExpr().accept(printer);
		pw.println();
		pw.println("// translated:");
		inv.getExpr().accept(printer);
		pw.flush();
		pw.println();
	}
	
	
	
	
	
}

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
import de.unikl.bcverifier.isl.checking.LibEnvironment;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;
import de.unikl.bcverifier.librarymodel.LibrarySource;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;
import de.unikl.bcverifier.specification.SpecInvariant;

public class ISLParserTest {

	
	@Test
	public void cell() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(
				"invariant forall old Cell o1, new Cell o2 ::",
					"o1 ~ o2 ==>",
						"if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2;");
		testTypeCheckOk(
				new File("./libraries/cell/old"), 
				new File("./libraries/cell/new"), cu);
		System.out.println("cell output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void cb() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk("invariant forall old A a :: a.g % 2 == 0;");
		testTypeCheckOk(
				new File("./libraries/cb/old"), 
				new File("./libraries/cb/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void localLoop() throws IOException, Exception, CompileException {
		CompilationUnit cu = testParseOk(Files.toString(new File("./libraries/localLoop/bpl/spec4.isl"), Charsets.UTF_8));
		testTypeCheckOk(
				new File("./libraries/localLoop/old"), 
				new File("./libraries/localLoop/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
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
				new File("./libraries/obool/old"), 
				new File("./libraries/obool/new"), cu);
		System.out.println("obool output:");
		translateAndPrint(cu);
		
		System.out.println("well definedness:");
		for (Statement s : cu.getStatementList()) {
			Invariant i = (Invariant) s;
			BPLExpression df = i.getExpr().translateExprWellDefinedness();
			PrintWriter pw = new PrintWriter(System.out);
			BPLPrinter printer = new BPLPrinter(pw);
			df.accept(printer);
			pw.flush();
			System.out.println();
		}
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
	
	protected void translateAndPrint(CompilationUnit cu) {
		PrintWriter pw = new PrintWriter(System.out);
		for (SpecInvariant inv : cu.generateInvariants()) {
			print(pw, inv);
		}
		
		for (SpecInvariant inv : cu.generateLocalInvariants()) {
			print(pw, inv);
		}
		
		
	}

	private void print(PrintWriter pw, SpecInvariant inv) {
		BPLPrinter printer = new BPLPrinter(pw);
		pw.println("// " + inv.getComment());
		pw.println("// welldefinedness:");
		inv.getWelldefinednessExpr().accept(printer);
		pw.println();
		pw.println("// translated:");
		inv.getInvExpr().accept(printer);
		pw.flush();
		pw.println();
	}
	
	
	
	
	
}

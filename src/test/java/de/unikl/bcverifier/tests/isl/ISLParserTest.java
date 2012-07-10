package de.unikl.bcverifier.tests.isl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


import org.apache.wicket.util.string.Strings;
import org.junit.Assert;
import org.junit.Test;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLExpression;
import beaver.Parser.Exception;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.checking.LibEnvironment;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;

public class ISLParserTest {

	
	@Test
	public void cell() throws IOException, Exception {
		CompilationUnit cu = testParseOk(
				"forall old Cell o1, new Cell o2 [",
					"o1 ~ o2 ==>",
						"if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2",
				"]");
		testTypeCheckOk(
				new File("./libraries/cell/old"), 
				new File("./libraries/cell/new"), cu);
		System.out.println("cell output:");
		translateAndPrint(cu);
	}
	
	@Test
	public void cb() throws IOException, Exception {
		CompilationUnit cu = testParseOk("forall old A a [ a.g % 2 == 0 ]");
		testTypeCheckOk(
				new File("./libraries/cb/old"), 
				new File("./libraries/cb/new"), cu);
		System.out.println("cb output:");
		translateAndPrint(cu);
	}

	@Test
	public void obool() throws IOException, Exception {
		CompilationUnit cu = testParseOk(
				"forall old Bool o1, new Bool o2 [o1 ~ o2 ==> o1.f == o2.f]",
				"forall old OBool o1, new OBool o2 [o1 ~ o2 ==> o1.g.f != o2.g.f]",
				// internal:
				"forall old OBool o [!createdByCtxt(o.g) && !exposed(o.g)]",
				"forall new OBool o [!createdByCtxt(o.g) && !exposed(o.g)]",
				// nonnull:
				"forall old OBool o [o.g != null]",
				"forall new OBool o [o.g != null]",
				// unique
				"forall old OBool o1, old OBool o2 [o1 != o2 ==> o1.g != o2.g]",
				"forall new OBool o1, new OBool o2 [o1 != o2 ==> o1.g != o2.g]"
				);
		testTypeCheckOk(
				new File("./libraries/obool/old"), 
				new File("./libraries/obool/new"), cu);
		System.out.println("obool output:");
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
	
	protected void testTypeCheckOk(File oldLib, File newLib, CompilationUnit cu) {
		cu.setLibEnvironment(new LibEnvironment(oldLib, newLib));
		cu.typecheck();
		for (TypeError err : cu.getErrors()) {
			System.err.println(err);
			assertTrue("" + err, false);
		}
	}
	
	protected void translateAndPrint(CompilationUnit cu) {
		List<BPLExpression> translated = cu.translate();
		for (BPLExpression e : translated) {
			PrintWriter pw = new PrintWriter(System.out);
			BPLPrinter printer = new BPLPrinter(pw);
			e.accept(printer);
			pw.flush();
			System.out.println();
		}
	}
	
	
	
	
	
}

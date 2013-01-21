package de.unikl.bcverifier.tests.isl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.wicket.util.string.Strings;
import org.testng.Assert;

import b2bpl.bpl.BPLPrinter;
import beaver.Parser.Exception;
import de.unikl.bcverifier.LibraryCompiler;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;
import de.unikl.bcverifier.specification.SpecExpr;

public class ISLTest {

	public ISLTest() {
		super();
	}

	protected CompilationUnit testParseOk(String ... lines) throws IOException, Exception {
		ISLCompiler parser = new ISLCompiler(Strings.join("\n", lines));
		Object res = parser.parse();
		for (ParserError err : parser.getErrors()) {
			System.err.println(err);
			Assert.fail(err.toString());
		}
		Assert.assertNotNull(res);
		return (CompilationUnit) res;
	}

	protected void testTypeCheckOk(File oldLib, File newLib, CompilationUnit cu)
			throws CompileException {
				doCheck(oldLib, newLib, cu);
				for (TypeError err : cu.getErrors()) {
					System.err.println(err);
					Assert.fail("" + err);
				}
			}

	protected void testTypeCheckError(String expected, File oldLib, File newLib,
			CompilationUnit cu) throws CompileException {
				doCheck(oldLib, newLib, cu);
				StringBuilder errors = new StringBuilder();
				for (TypeError err : cu.getErrors()) {
					errors.append("\n" + err.toString());
				}
				if (!errors.toString().contains(expected)) {
					Assert.assertEquals(errors.toString(), expected);
				}
			}

	private void doCheck(File oldLib, File newLib, CompilationUnit cu)
			throws CompileException {
				LibraryCompiler.compile(oldLib);
				LibraryCompiler.compile(newLib);
				TwoLibraryModel twoLibraryModel = new TwoLibraryModel(oldLib, newLib);
				cu.setTwoLibraryModel(twoLibraryModel);
				cu.setBuiltinFunctions(new BuiltinFunctions(twoLibraryModel ));
				cu.typecheck();
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
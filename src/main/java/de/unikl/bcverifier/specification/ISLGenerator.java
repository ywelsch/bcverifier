package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLExpression;
import beaver.Parser.Exception;
import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.checking.LibEnvironment;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;

public class ISLGenerator extends AbstractGenerator {


	public ISLGenerator(Configuration config) {
		super(config);
	}


	private boolean initDone = false;
	private CompilationUnit cu;



	private void init() throws GenerationException {
		if (initDone) {
			return;
		}
		try {
			ISLCompiler compiler = new ISLCompiler(getReader());
			cu = compiler.parse();
			for (ParserError err : compiler.getErrors()) {
				throw new GenerationException(err.toString());
			}
			File oldLib = getConfig().library1();
			File newLib = getConfig().library2();
			cu.setLibEnvironment(new LibEnvironment(oldLib, newLib));
			cu.typecheck();
			for (TypeError err : cu.getErrors()) {
				throw new GenerationException(err.toString());
			}
			
		} catch (IOException e) {
			throw new GenerationException("", e);
		} catch (Exception e) {
			throw new GenerationException("", e);
		}
		initDone = true;
	}


	@Override
	public List<String> generateInvariant() throws GenerationException {
		init();
		List<BPLExpression> invariants = cu.translate();
		List<String> result = new ArrayList<String>();
		for (BPLExpression inv : invariants) {
			result.add(exprToString(inv));
		}
		return result;
	}


	private String exprToString(BPLExpression inv) {
		Writer s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		BPLPrinter printer = new BPLPrinter(pw );
		inv.accept(printer);
		pw.flush();
		String string = s.toString();
		return string;
	}
}

package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
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
			StringBuilder errors = new StringBuilder();
			for (ParserError err : compiler.getErrors()) {
				errors.append(err);
				errors.append("\n");
			}
			if (errors.length() > 0) {
				throw new GenerationException(errors.toString());
			}
			File oldLib = getConfig().library1();
			File newLib = getConfig().library2();
			cu.setLibEnvironment(new LibEnvironment(oldLib, newLib));
			cu.typecheck();
			errors = new StringBuilder();
			for (TypeError err : cu.getErrors()) {
				errors.append(err);
				errors.append("\n");
			}
			if (errors.length() > 0) {
				throw new GenerationException(errors.toString());
			}
		} catch (IOException e) {
			throw new GenerationException("", e);
		} catch (Exception e) {
			throw new GenerationException("", e);
		}
		initDone = true;
	}


	
    @Override
	public List<SpecInvariant> generateInvariant() throws GenerationException {
    	init();
    	return cu.generateInvariants();
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

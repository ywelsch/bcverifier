package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableDeclaration;
import b2bpl.bpl.ast.BPLVariableExpression;
import beaver.Parser.Exception;
import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;
import de.unikl.bcverifier.isl.checking.LibEnvironment;
import de.unikl.bcverifier.isl.checking.TypeError;
import de.unikl.bcverifier.isl.parser.ISLCompiler;
import de.unikl.bcverifier.isl.parser.ParserError;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ISLGenerator extends AbstractGenerator {


	private TwoLibraryModel twoLibraryModel;



	public ISLGenerator(Configuration config, TwoLibraryModel twoLibraryModel) {
		super(config);
		this.twoLibraryModel = twoLibraryModel;
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
			cu.setTwoLibraryModel(twoLibraryModel);
			cu.setBuiltinFunctions(new BuiltinFunctions(twoLibraryModel));
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

	@Override
	public List<SpecInvariant> generateLocalInvariant()	throws GenerationException {
		init();
		return cu.generateLocalInvariants();
	}
	
	@Override
	public LocalPlaceDefinitions generateLocalPlaces() throws GenerationException {
		init();
		return cu.generatePlaces();
	}
	
	@Override
	public List<String> generatePreludeAddition() throws GenerationException {
		init();
		return cu.generatePreludeAddition();
	}
	
	@Override
	public List<String> generatePreconditions() throws GenerationException {
		init();
		return cu.generatePreconditions();
	}
	
	@Override
	public List<String> generateGlobalAssignments() throws GenerationException {
		return cu.generateGlobalAssignments();
	}
	
	@Override
	public List<BPLVariableDeclaration> generateGlobalVariables() throws GenerationException {
		return cu.generateGlobalVariables();
	}
	
	@Override
	public List<String> generateInitialAssignments() throws GenerationException {
		return cu.generateInitialAssignments();
	}
}

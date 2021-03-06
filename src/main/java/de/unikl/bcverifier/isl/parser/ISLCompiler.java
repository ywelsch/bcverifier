package de.unikl.bcverifier.isl.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import beaver.Parser;
import beaver.Parser.Exception;
import beaver.Symbol;
import de.unikl.bcverifier.isl.ast.CompilationUnit;

public class ISLCompiler {
	
	private Reader in;
	private List<IslError> errors = new ArrayList<IslError>();

	public ISLCompiler(String input) {
		in = new StringReader(input);
	}
	
	public ISLCompiler(Reader in) {
		this.in = in;
	}
	
	public CompilationUnit parse() throws IOException, Exception {
		ISLScanner scanner = new ISLScanner(in);
		ISLParser parser = new ISLParser();
		Object r = null;
		try {
			r = parser.parse(scanner);
			errors = parser.getErrors();
		} catch (Parser.Exception e) {
			errors.addAll(parser.getErrors());
			errors.add(new SyntaxError(e.getMessage(), 0, 0, 0, 0, new Symbol("")));
		}
		
		if (r instanceof CompilationUnit) {
			return (CompilationUnit) r;
		} else {
			return null;
		}
	}

	public List<IslError> getErrors() {
		return errors;
	}
	
}

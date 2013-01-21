package de.unikl.bcverifier;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import b2bpl.Project;
import b2bpl.bytecode.ITroubleReporter;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.TroubleMessage;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.LibrarySource;


public class LibraryCompiler {
    public static class CompileException extends Exception{
        private static final long serialVersionUID = 4817956211311407538L;

        public CompileException(String msg) {
            super(msg);
        }
    }
    
    public static void compile(File libraryPath) throws CompileException {
    	StringWriter outWriter = new StringWriter();
    	StringWriter errWriter = new StringWriter();
    	boolean res = BatchCompiler.compile(new String[] {"-source", "5", "-target", "5", "-g", "-preserveAllLocals", "-nowarn", "-noExit", libraryPath.getAbsolutePath()}, new PrintWriter(outWriter), new PrintWriter(errWriter), null);
    	if (!res) {
    		String errorString = errWriter.toString();
    		throw new CompileException("Files could not be compiled\n" + errorString);
    	}
    }

	public static LibrarySource computeAST(File libraryPath, Version version) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		Object[] sourceFiles = FileUtils.listFiles(libraryPath, new String[] { "java" }, true).toArray();
		String[] sources = new String[sourceFiles.length];
		for (int i = 0; i < sourceFiles.length; i++) {
			sources[i] = ((File)sourceFiles[i]).getAbsolutePath();
		}
		parser.setEnvironment(new String[0], new String[]{libraryPath.getAbsolutePath()}, null, true);
		Requestor req = new Requestor();
		parser.createASTs(sources, null, new String[0], req, null);
		LibrarySource source = new LibrarySource(version);
		source.setUnits(req.getScannedUnits());
		Project project = Project.fromCommandLine(Library.listLibraryClassFiles(libraryPath), new PrintWriter(System.out));
		ITroubleReporter troubleReporter = new ITroubleReporter() {
			@Override
			public void reportTrouble(TroubleMessage message) {
				throw new RuntimeException(message.getPosition().getMethod().getQualifiedName() 
						+ "  " +  message.getDescriptionString());
			}
		};
		JClassType[] classTypes = Library.setProjectAndLoadTypes(project, troubleReporter);
		source.setClassTypes(classTypes);
		return source;
	}
	
	private static class Requestor extends FileASTRequestor {
		final List<CompilationUnit> scannedUnits = new ArrayList<CompilationUnit>();
		public List<CompilationUnit> getScannedUnits() {
			return scannedUnits;
		}
		@Override 
		public void acceptAST(String sourceFilePath, CompilationUnit ast) {
			scannedUnits.add(ast);
		}
	}
}

package de.unikl.bcverifier.sourcecomp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JDTTest {
	public static void main(String...args) {
		new JDTTest().setup();
	}
	
	public void setup() {
		ASTParser parser1 = ASTParser.newParser(AST.JLS4);
		parser1.setKind(ASTParser.K_COMPILATION_UNIT);
		parser1.setResolveBindings(true);
		ASTParser parser2 = ASTParser.newParser(AST.JLS4);
		parser2.setKind(ASTParser.K_COMPILATION_UNIT);
		parser2.setResolveBindings(true);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		//JavaCore.setComplianceOptions(JavaCore.IGNORE, options);
		parser1.setCompilerOptions(options);
		parser2.setCompilerOptions(options);
		String[] sources1 = getSourceFiles("libraries/cell/old");
		String[] sources2 = getSourceFiles("libraries/cell/new");
		parser1.setEnvironment(new String[0], new String[]{new File("libraries/cell/old").getAbsolutePath()}, null, true);
		Requestor req1 = new Requestor();
		parser1.createASTs(sources1, null, new String[0], req1, null);
		parser2.setEnvironment(new String[0], new String[]{new File("libraries/cell/new").getAbsolutePath()}, null, true);
		Requestor req2 = new Requestor();
		parser2.createASTs(sources2, null, new String[0], req2, null);
		List<CompilationUnit> allUnits = new ArrayList<CompilationUnit>();
		allUnits.addAll(req1.getScannedUnits());
		allUnits.addAll(req2.getScannedUnits());
		for (CompilationUnit cu : allUnits) {
			cu.accept(new ASTVisitor() {
				@Override public boolean visit(TypeDeclaration td) {
					ITypeBinding b = td.resolveBinding();
					System.out.println(b.getBinaryName());
					for (IMethodBinding m : b.getDeclaredMethods()) {
						System.out.println(m);
					}
					return true;
				}
			});
		}
	}
	
	private String[] getSourceFiles(String libName) {
		Object[] sourceFiles = FileUtils.listFiles(new File(libName), new String[] { "java" }, true).toArray();
		String[] sources = new String[sourceFiles.length];
		for (int i = 0; i < sourceFiles.length; i++) {
			sources[i] = ((File)sourceFiles[i]).getAbsolutePath();
		}
		return sources;
	}
	
	class Requestor extends FileASTRequestor {
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

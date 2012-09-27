package de.unikl.bcverifier.librarymodel;

import java.io.File;
import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.objectweb.asm.Opcodes;

import de.unikl.bcverifier.LibraryCompiler;
import de.unikl.bcverifier.isl.ast.Version;



public class TwoLibraryModel {
	private LibrarySource src1, src2;


	public TwoLibraryModel(File library1, File library2) {
		src1 = LibraryCompiler.computeAST(library1, Version.OLD);
    	src2 = LibraryCompiler.computeAST(library2, Version.NEW);
	}

	protected String getOpCode(int op) {
		for (Field f : Opcodes.class.getFields()) {
			try {
				if (f.getType().equals(int.class) && ((Integer) f.get(null)) == op) {
					return f.getName();
				}
			} catch (Exception e) {
				System.err.println("constant: " + f.getName());
				e.printStackTrace();
			}
		}
		return "op" +op;
	}

	

	public LibrarySource getSrc1() {
		return src1;
	}

	public void setSrc1(LibrarySource src1) {
		this.src1 = src1;
	}

	public LibrarySource getSrc2() {
		return src2;
	}

	public void setSrc2(LibrarySource src2) {
		this.src2 = src2;
	}

	public LibrarySource getSrc(Version version) {
		switch (version) {
		case OLD:
			return src1;
		case NEW:
			return src2;
		default:
			throw new Error("Version must be OLD or NEW.");
		}
	}
	
	public static int getLineNr(ASTNode node) {
		ASTNode n = node;
		while (n != null && !(n instanceof CompilationUnit)) {
			n = n.getParent();
		}
		if (n instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) n;
			return cu.getLineNumber(node.getStartPosition());
		}
		return 0;
	}

}

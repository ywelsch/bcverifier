package de.unikl.bcverifier.librarymodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifierClassVisitor;

import de.unikl.bcverifier.LibraryCompiler;
import de.unikl.bcverifier.isl.ast.Version;



public class TwoLibraryModel {
	private LibrarySource src1, src2;


	public TwoLibraryModel(File library1, File library2) {
		src1 = LibraryCompiler.computeAST(library1);
    	src2 = LibraryCompiler.computeAST(library2);
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

	/**
	 * loads the type with the given name and version
	 * returns null when the type was not found
	 * 
	 * @param version OLD or NEW
	 * @param name simple name or fully qualified name of the type
	 */
	public ITypeBinding loadType(Version version, String name) {
		switch (version) {
		case NEW:
			return src2.resolveType(name);
		case OLD:
			return src1.resolveType(name);
		case BOTH:
		default:
			throw new Error("not implemented");
		}
	}
	
	public ASTNode findDeclaringNode(Version version, IBinding binding) {
		switch (version) {
		case NEW:
			return src2.findDeclaringNode(binding);
		case OLD:
			return src1.findDeclaringNode(binding);
		case BOTH:
		default:
			throw new Error("not implemented");
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

	public AsmClassNodeWrapper getClassNodeWrapper(Version version, ITypeBinding tb) {
		switch (version) {
		case NEW:
			return src2.getClassNodeWrapper(tb);
		case OLD:
			return src1.getClassNodeWrapper(tb);
		case BOTH:
		default:
			throw new Error("not implemented");
		}
	}

}

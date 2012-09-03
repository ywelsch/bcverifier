package de.unikl.bcverifier.isl.checking;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

import b2bpl.bytecode.TypeLoader;

public class BytecodeHelper {
	
	
//	static String getLocalVarName(TwoLibraryModel model, JavaVariableDef v, int lineNr) {
//		IVariableBinding b = v.getBinding();
//		ASTNode varDef = model.findDeclaringNode(v.getVersion(), b);
//		TypeDeclaration t = getParentTypeDeclaration(varDef);
//		ITypeBinding tb = t.resolveBinding();
//		MethodDeclaration m = getParentMethodDeclaration(varDef);
//		
//		
//		
//		ClassNode cn = model.getClassNode(v.getVersion(), tb);
//		
//		for (MethodNode meth : (List<MethodNode>) cn.methods) {
//			if (meth.name.equals(m.getName())) {
//				// TODO check signature
//				for (LocalVariableNode local : (List<LocalVariableNode>) meth.localVariables) {
//					if (local.name.equals(b.getName())) {
//						int start = local.start.getLabel().getOffset();
//						int end = local.start.getLabel().getOffset();
//						
//					}
//				}
//			}
//		}
//		return "???";
//	}

	private static MethodDeclaration getParentMethodDeclaration(ASTNode node) {
		ASTNode n = node;
		while (n!= null) {
			if (n instanceof MethodDeclaration) {
				return (MethodDeclaration) n;
			}
			n = n.getParent();
		}
		return null;
	}

	private static TypeDeclaration getParentTypeDeclaration(ASTNode node) {
		ASTNode n = node;
		while (n!= null) {
			if (n instanceof TypeDeclaration) {
				return (TypeDeclaration) n;
			}
			n = n.getParent();
		}
		return null;
	}

}

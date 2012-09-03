package de.unikl.bcverifier.librarymodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;



public class AsmBytecodeHelper {
	
	
	/**
	 * load all classnodes from the given folder and save them into the asmClasses map
	 * 
	 */
	public static Map<String, ClassNode> loadClasses(File directory) {
		Map<String, ClassNode> asmClasses = new HashMap<String, ClassNode>();
		loadClasses(asmClasses, directory);
		return asmClasses;
	}
	
	private static void loadClasses(Map<String, ClassNode> asmClasses, File f) {
		for (File child : f.listFiles()) {
			loadClasses(asmClasses, "", child);
		}
	}

	private static void loadClasses(Map<String, ClassNode> asmClasses, String pack, File f) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				String childPack;
				if (pack.isEmpty()) {
					childPack = f.getName();
				} else {
					childPack = pack + "." + f.getName();
				}
				loadClasses(asmClasses, childPack, child);
			}
		} else if (f.getName().endsWith(".class")) {
			try {
				InputStream is = new FileInputStream(f);
				ClassReader cr = new ClassReaderWithLabelOffsets(is);
				
				
				ClassNode cn = new ClassNode();
				cr.accept(cn, 0);
				String classname;
				if (pack.isEmpty()) {
					classname = f.getName().replace(".class", "");
				} else {
					classname = pack + "." + f.getName().replace(".class", "");
				}
				asmClasses.put(classname, cn);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<Pair<Integer, Integer>> calculateLineNumbersToProgramCounter(ClassNode classNode) {
		final List<Pair<Integer, Integer>> lineToPc = new ArrayList<Pair<Integer,Integer>>();
		
		@SuppressWarnings("unchecked")
		List<MethodNode> methods = (List<MethodNode>) classNode.methods;
		
		for (MethodNode m : methods) {
			m.accept(new MethodVisitor() {
				
				private int lastNineNr = -1;
				private int pc = 0;
				
				
				private void visitSomeInstruction() {
					if (lastNineNr >= 0) {
						lineToPc.add(Pair.of(lastNineNr, pc));
						lastNineNr = -1;
					}
					pc ++;
				}
				
				@Override
				public void visitLineNumber(int line, Label label) {
					lastNineNr = line;
				}
				
				@Override
				public void visitLabel(Label l) {
					pc = l.getOffset();
				}
				
				@Override
				public void visitLocalVariable(String arg0, String arg1, String arg2,
						Label arg3, Label arg4, int arg5) {
					// TODO visit local var
				}
				
				
				@Override
				public void visitVarInsn(int arg0, int arg1) {
					visitSomeInstruction();						
				}

				@Override
				public void visitTypeInsn(int arg0, String arg1) {
					visitSomeInstruction();	
					
				}
				
				@Override
				public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
						String arg3) {
				}
				
				@Override
				public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
						Label[] arg3) {
					visitSomeInstruction();	
					
				}
				
				@Override
				public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
						boolean arg2) {
					return null;
				}
				
				@Override
				public void visitMultiANewArrayInsn(String arg0, int arg1) {
					visitSomeInstruction();	
					
				}
				
				@Override
				public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
					visitSomeInstruction();	
					
				}
				
				@Override
				public void visitMaxs(int arg0, int arg1) {
					visitSomeInstruction();	
					
				}
				
				@Override
				public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
					visitSomeInstruction();	
					
				}
				
				
				@Override
				public void visitLdcInsn(Object arg0) {
					visitSomeInstruction();	
				}
				
				
				
				@Override
				public void visitJumpInsn(int arg0, Label arg1) {
					visitSomeInstruction();	
				}
				
				@Override
				public void visitIntInsn(int arg0, int arg1) {
					visitSomeInstruction();	
				}
				
				@Override
				public void visitInsn(int arg0) {
					visitSomeInstruction();	
				}
				
				@Override
				public void visitIincInsn(int arg0, int arg1) {
					visitSomeInstruction();	
				}
				
				@Override
				public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
						Object[] arg4) {
				}
				
				@Override
				public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
				}
				
				@Override
				public void visitEnd() {
				}
				
				@Override
				public void visitCode() {
				}
				
				@Override
				public void visitAttribute(Attribute arg0) {
				}
				
				@Override
				public AnnotationVisitor visitAnnotationDefault() {
					return null;
				}
				
				@Override
				public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
					return null;
				}
			});
		}
		
		
		for (Entry<Integer, Integer> info : lineToPc) {
			System.out.println("~~~ line " + info.getKey() + " --> " + info.getValue() );
		}
		return lineToPc;
	}
}

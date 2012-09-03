package de.unikl.bcverifier.librarymodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


public class AsmClassNodeWrapper {

	private ClassNode classNode;
	
	// list of (line number, program counter)
	private Map<Integer, List<Integer>> lineToPc = new HashMap<Integer, List<Integer>>();
	private Map<Integer, MethodNode> lineToMethod = new HashMap<Integer, MethodNode>();
	private Map<MethodNode, List<LocalVarInfo>> methodToLocalVars = new HashMap<MethodNode, List<LocalVarInfo>>();

	public AsmClassNodeWrapper(ClassNode classNode) {
		this.classNode = classNode;
		init();
	}
	
	/**
	 * returns a list of all program counter values belonging to the given line 
	 */
	public List<Integer> getProgramCounterForLine(int line) {
		return lineToPc.get(line);
	}
	
	public LocalVarInfo getLocalVar(int line, String name) {
		MethodNode method = lineToMethod.get(line);
		List<Integer> pcs = getProgramCounterForLine(line);
		for (LocalVarInfo localVar : methodToLocalVars.get(method)) {
			if (! localVar.getName().equals(name)) {
				continue;
			}
			for (int pc : pcs) {
				if (pc >= localVar.getStart() && pc <= localVar.getEnd()) {
					return localVar;
				}
			}
		}
		return null;
	}

	private void init() {
		@SuppressWarnings("unchecked")
		List<MethodNode> methods = (List<MethodNode>) classNode.methods;

		for (final MethodNode currentMethod : methods) {
			currentMethod.accept(new MethodVisitor() {

				private int lastNineNr = -1;
				private int pc = 0;


				private void visitSomeInstruction() {
					if (lastNineNr >= 0) {
						List<Integer> pcs = lineToPc.get(lastNineNr);
						if (pcs == null) {
							pcs = new ArrayList<Integer>();
							lineToPc.put(lastNineNr, pcs);
						}
						pcs.add(pc);
						lastNineNr = -1;
					}
					pc ++;
				}

				@Override
				public void visitLineNumber(int line, Label label) {
					lineToMethod.put(line, currentMethod);
					lastNineNr = line;
				}

				@Override
				public void visitLabel(Label l) {
					pc = l.getOffset();
				}

				@Override
				public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
					List<LocalVarInfo> locals = methodToLocalVars.get(currentMethod);
					if (locals == null) {
						locals = new ArrayList<LocalVarInfo>();
						methodToLocalVars.put(currentMethod, locals);
					}
					LocalVarInfo var = new LocalVarInfo(name, index, start.getOffset(), end.getOffset());
					locals.add(var);
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
	}

}

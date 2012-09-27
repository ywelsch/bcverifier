package de.unikl.bcverifier.librarymodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.objectweb.asm.tree.ClassNode;

import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.InstructionHandle;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.instructions.Instruction;
import b2bpl.bytecode.instructions.InvokeInstruction;
import b2bpl.translation.MethodTranslator;

import com.google.common.collect.Maps;

import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.isl.ast.Version;

public class LibrarySource {
	private Version version;
	private List<CompilationUnit> units;
	private Map<String, ClassNode> asmClasses;
	private Map<String, AsmClassNodeWrapper> asmClassWrappers;
	private Map<String, JClassType> classTypes;
	// map: class -> calledFuncName -> lineNr -> boogie place-name 
	private Map<JClassType, Map<String, Map<Integer, String>>> predefinedPlaceNames = Maps.newHashMap();

	public LibrarySource(Version version) {
		this.version = version;
	}
	
	public List<CompilationUnit> getUnits() {
		return units;
	}

	public void setUnits(List<CompilationUnit> units) {
		this.units = units;
	}


	/**
	 * resolves well known types like java.lang.Object 
	 */
	private ITypeBinding resolveWellKnownType(String typeName) {
		for (CompilationUnit cu : units) {
			AST b = cu.getAST();
			ITypeBinding javaLangObject = b.resolveWellKnownType(typeName);
			return javaLangObject;
		}
		return null;
	}
	
	/**
	 * loads the type with the given name
	 * returns null when the type was not found
	 */
	public ITypeBinding resolveType(final String name) {
		ITypeBinding t = resolveWellKnownType(name);
		if (t != null) {
			return t;
		}
		
		final List<ITypeBinding> result = new ArrayList<ITypeBinding>();
		for (CompilationUnit cu : units) {
			cu.accept(new ASTVisitor() {
				
				@Override
				public boolean visit(TypeDeclaration node) {
					ITypeBinding binding = node.resolveBinding();
					if (binding.getQualifiedName().equals(name)) {
						// fully qualified name matches
						result.add(binding);
						return false;
					} else if (node.getName().getFullyQualifiedName().equals(name)) {
						// simple name matches
						result.add(binding);
					}
					return true;
				}
				
			});
		}
		if (result.size() == 1) {
			return result.get(0);
		}
		return null;
	}
	
	public ASTNode findDeclaringNode(IBinding binding) {
		for (CompilationUnit cu : units) {
			ASTNode node = cu.findDeclaringNode(binding);
			if (node != null) {
				return node;
			}
		}
		return null;
	}

	public void setAsmClasses(Map<String, ClassNode> asmClasses) {
		this.asmClasses = asmClasses;
		this.asmClassWrappers = new HashMap<String, AsmClassNodeWrapper>();
	}

	public AsmClassNodeWrapper getClassNodeWrapper(ITypeBinding tb) {
		String qualifiedName = tb.getQualifiedName();
		AsmClassNodeWrapper wr = asmClassWrappers.get(qualifiedName);
		if (wr == null) {
			wr = new AsmClassNodeWrapper(asmClasses.get(qualifiedName));
			asmClassWrappers.put(qualifiedName, wr);
		}
		return wr;
	}

	public void setClassTypes(JClassType[] classTypes) {
		this.classTypes = Maps.newHashMap();
		for (JClassType ct : classTypes) {
			this.classTypes.put(ct.getName(), ct);
		}
	}
	
	public JClassType getClassType(String qualifiedName) {
		return classTypes.get(qualifiedName);
	}

	public JClassType getClassType(ITypeBinding tb) {
		return getClassType(tb.getQualifiedName());
	}

	public String getBoogiePlaceName(ITypeBinding enclosingClassType, int line, String methodName) {
		JClassType ct = getClassType(enclosingClassType);
		Map<String, Map<Integer, String>> pp = predefinedPlaceNames.get(ct);
		if (pp == null) {
			pp = buildBoogiePlaceTable(ct);
			predefinedPlaceNames.put(ct, pp);
		}
		Map<Integer, String> occurences = pp.get(methodName);
		if (occurences == null) {
			throw new Error("Could not find any call to " + methodName);
		}
		String result = occurences.get(line);
		if (result == null) {
			throw new Error("No call to " + methodName + " found in line " + line);
		}
		return result;
	}

	private Map<String, Map<Integer, String>> buildBoogiePlaceTable(JClassType ct) {
		Map<String, Map<Integer, String>> result = Maps.newHashMap();
		TranslationController tc = new TranslationController();
		if (version == Version.OLD) {
			tc.enterRound1();
		} else {
			tc.enterRound2();
		}
		for (BCMethod meth : ct.getMethods()) {
			for (InstructionHandle insHandle : meth.getInstructions()) {
				Instruction ins = insHandle.getInstruction();
				if (ins instanceof InvokeInstruction) {
					InvokeInstruction invokeInstruction = (InvokeInstruction) ins;
					String methodName = invokeInstruction.getMethodName();
					String invokedMethodName = MethodTranslator.getMethodName(invokeInstruction.getMethod());
	                String boogiePlaceName = tc.buildPlace(MethodTranslator.getProcedureName(meth), invokedMethodName);
	                Map<Integer, String> r = result.get(methodName);
	                if (r == null) {
	                	r = Maps.newHashMap();
	                	result.put(methodName, r);
	                }
	                r.put(insHandle.getSourceLine(), boogiePlaceName);
				}
			}
		}
		return result ;
	}

}

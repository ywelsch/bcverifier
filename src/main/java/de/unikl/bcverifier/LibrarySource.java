package de.unikl.bcverifier;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;

public class LibrarySource {
	private List<CompilationUnit> units;

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

}

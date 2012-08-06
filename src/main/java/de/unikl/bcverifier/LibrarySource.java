package de.unikl.bcverifier;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class LibrarySource {
	private List<CompilationUnit> units;

	public List<CompilationUnit> getUnits() {
		return units;
	}

	public void setUnits(List<CompilationUnit> units) {
		this.units = units;
	}

	public ASTNode loadType(String qualifiedName) {
		List<ASTNode> result = new ArrayList<ASTNode>();
		for (CompilationUnit cu : units) {
			ASTNode decl = cu.findDeclaringNode(qualifiedName);
			if (decl != null) {
				result.add(decl);
			}
		}
		if (result.size() == 0) {
			throw new Error("could not find type " + qualifiedName);
		} else if (result.size() == 1) {
			return result.get(0);
		} else {
			throw new Error("Ambigious name: " + qualifiedName);
		}
	}

}

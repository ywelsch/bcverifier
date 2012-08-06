package de.unikl.bcverifier;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class LibrarySource {
	private List<CompilationUnit> units;

	public List<CompilationUnit> getUnits() {
		return units;
	}

	public void setUnits(List<CompilationUnit> units) {
		this.units = units;
	}

}

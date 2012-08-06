package de.unikl.bcverifier;

import org.eclipse.jdt.core.dom.ITypeBinding;

import de.unikl.bcverifier.isl.ast.Version;


public class TwoLibraryModel {
	private LibrarySource src1, src2;

	public TwoLibraryModel(LibrarySource libsrc1, LibrarySource libsrc2) {
		this.src1 = libsrc1;
		this.src2 = libsrc2;
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

}

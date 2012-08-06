package de.unikl.bcverifier;

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

	public Class<?> loadType(Version version, String qualifiedName) {
		switch (version) {
		case NEW:
			src2.loadType(qualifiedName);
		case OLD:
			src1.loadType(qualifiedName);
		case BOTH:
		default:
			throw new Error("not implemented");
		}
	}

}

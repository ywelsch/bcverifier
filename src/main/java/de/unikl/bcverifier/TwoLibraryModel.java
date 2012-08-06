package de.unikl.bcverifier;


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

}

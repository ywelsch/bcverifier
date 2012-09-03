package de.unikl.bcverifier.librarymodel;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;

/*
 * extended ClassReader which memorizes the offsets of labels
 * as suggested in http://osdir.com/ml/java.objectweb.asm/2008-03/msg00031.html
 * 
 */
public class ClassReaderWithLabelOffsets extends ClassReader {

	public ClassReaderWithLabelOffsets(InputStream is) throws IOException {
		super(is);
	}
	
	@Override
	protected Label readLabel(int offset, Label[] ls) {
		Label s = super.readLabel(offset, ls);
		LabelWithOffset r = new LabelWithOffset(offset, s);
		ls[offset] = r;
		return r;
	}
	
}


class LabelWithOffset extends Label {

	private int offset;
	private Label label;

	public LabelWithOffset(int offset, Label label) {
		this.offset = offset;
		this.info = label.info;
		this.label = label;
	}
	
	@Override
	public int getOffset() {
		return offset;
	}
	
	@Override
	public String toString() {
		return "label(" + offset + ")";
	}
}
package de.unikl.bcverifier.librarymodel;

public class LocalVarInfo {

	private String name;
	private int index;
	private int start;
	private int end;

	public LocalVarInfo(String name, int index, int start, int end) {
		this.name = name;
		this.index = index;
		this.start = start;
		this.end = end;
	}

	@Override
	public String toString() {
		return "LocalVarInfo [name=" + name + ", index=" + index + ", start="
				+ start + ", end=" + end + "]";
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
	
	
}

package de.unikl.bcverifier.web.backend;

import java.io.Serializable;
import java.util.List;

public class Example implements Serializable {
	private String id;
	private List<String> lib1files;
	private List<String> lib2files;
	private String invariant;
	private int unrollCount;
	
	@Override
	public String toString() {
		return getId();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getLib1files() {
		return lib1files;
	}
	public void setLib1files(List<String> lib1files) {
		this.lib1files = lib1files;
	}
	public List<String> getLib2files() {
		return lib2files;
	}
	public void setLib2files(List<String> lib2files) {
		this.lib2files = lib2files;
	}
	public String getInvariant() {
		return invariant;
	}
	public void setInvariant(String invariant) {
		this.invariant = invariant;
	}
	public int getUnrollCount() {
		return unrollCount;
	}
	public void setUnrollCount(int unrollCount) {
		this.unrollCount = unrollCount;
	}
}
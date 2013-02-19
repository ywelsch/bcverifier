package de.unikl.bcverifier.web.backend;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import de.unikl.bcverifier.web.HomePage;

public class ExampleLoader {
	private Example loadExample(String dir, String description, String unrollCount, String invFileName) {
		ClassLoader loader = HomePage.class.getClassLoader();
		try {
		    FileSystemManager fsManager = VFS.getManager();
			FileObject topDir = fsManager.resolveFile(loader.getResource(dir).toURI().toString());
			FileObject oldDir = topDir.getChild("old");
			FileObject newDir = topDir.getChild("new");
			FileObject invFile = topDir.getChild("bpl").getChild(invFileName);
			
			class JavaFileSelector implements FileSelector {

				public boolean includeFile(FileSelectInfo fileInfo)
						throws Exception {
					return fileInfo.getFile().getName().getBaseName().endsWith(".java");
				}

				public boolean traverseDescendents(FileSelectInfo fileInfo)
						throws Exception {
					return true;
				}
				
			}
			
			FileObject[] oldJavaFiles = oldDir.findFiles(new JavaFileSelector());
			FileObject[] newJavaFiles = newDir.findFiles(new JavaFileSelector());
	        Example ex = new Example();
			ex.setId(description);
			ex.setDir(dir);
			ex.setInvariant(IOUtils.toString(invFile.getContent().getInputStream()));
			List<String> lib1files = new ArrayList<String>();
			for (FileObject f : oldJavaFiles) {
				lib1files.add(IOUtils.toString(f.getContent().getInputStream()));
			}
			ex.setLib1files(lib1files);
			List<String> lib2files = new ArrayList<String>();
			for (FileObject f : newJavaFiles) {
				lib2files.add(IOUtils.toString(f.getContent().getInputStream()));
			}
			ex.setLib2files(lib2files);
			ex.setUnrollCount(Integer.parseInt(unrollCount));
			return ex;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Example> loadExamples() {
		List<Example> examples = new ArrayList<Example>();
		String[][] TO_LOAD = new String[][] { 
				new String[]{ "cell", "Cell example", "1", "spec.isl" },
				new String[]{ "cb", "Callback example", "3", "spec.isl" },
				new String[]{ "cbalt", "Callback example (alt.1)", "3", "spec.isl" },
				new String[]{ "cbalt", "Callback example (alt.2)", "3", "spec2.isl" },
				new String[]{ "cblock", "CallbackLock example", "3", "spec.isl" },
				new String[]{ "awk", "Awkward example (static)", "5", "spec.isl" },
				new String[]{ "awk2", "Awkward example", "5", "spec.isl" },
				new String[]{ "obool", "OBool example", "7", "spec.isl" },
				new String[]{ "obool", "OBool example (alt.1)", "7", "spec2.isl" },
				new String[]{ "obool", "OBool example (alt.2)", "7", "spec4.isl" },
				new String[]{ "obool2", "OBool example (alt.3)", "7", "spec3.isl" },
				new String[]{ "subtypes", "Subtypes example", "2", "spec.isl" },
				new String[]{ "subtypes3", "Subtypes example (alt.1)", "4", "spec.isl" },
				new String[]{ "oneOffLoop", "OneOffLoop example", "2", "spec.isl" },
				new String[]{ "oneOffLoop", "OneOffLoop example (alt.1)", "2", "spec2.isl" },
				new String[]{ "cubes", "Cubes example", "2", "spec.isl" },
				new String[]{ "diverge", "Divergence example", "2", "spec.isl" },
				new String[]{ "diverge2", "Divergence example (alt)", "2", "spec.isl" },
				new String[]{ "measure", "Termination example", "2", "spec.isl" },
				new String[]{ "predefinedplaces", "ComplexPlaces example", "2", "spec.isl" },
				new String[]{ "list", "ObservableList example", "6", "spec.isl" }
		};
		for (String[] entry : TO_LOAD) {
			Example ex = loadExample(entry[0], entry[1], entry[2], entry[3]);
			if (ex != null) {
				examples.add(ex);
			}
		}
    	return examples;
	}
}

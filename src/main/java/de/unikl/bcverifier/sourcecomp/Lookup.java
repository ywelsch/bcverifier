package de.unikl.bcverifier.sourcecomp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.unikl.bcverifier.isl.ast.Version;

public class Lookup {
	private final File oldLibFolder;
	private final File newLibFolder;
	private URLClassLoader oldClassLoader;
	private URLClassLoader newClassLoader;
	private final List<String> allOldTypes;

	public Lookup(File oldLibFolder, File newLibFolder) {
		this.oldLibFolder = oldLibFolder;
		this.newLibFolder = newLibFolder;
		try {
			oldClassLoader = new URLClassLoader(new URL[] {oldLibFolder.toURI().toURL()});
			newClassLoader = new URLClassLoader(new URL[] {newLibFolder.toURI().toURL()});
		} catch (MalformedURLException e) {
			// TODO
			e.printStackTrace();
		}
		allOldTypes = new ArrayList<String>();
		Collection<File> oldClassFiles = FileUtils.listFiles(oldLibFolder,
                new String[] { "class" }, true);
		for (File f : oldClassFiles) {
			String path = oldLibFolder.toURI().relativize(f.toURI()).getPath();
			path = path.substring(0, path.length() - 6); // remove .class extension
			path = path.replace(File.separatorChar, '.');
			allOldTypes.add(path);
		}
	}

	public Class<?> loadClass(Version version, String name) {
		switch (version) {
		case BOTH:
			throw new Error("not implemented");
		case NEW:
			return delegateLoad(newLibFolder, newClassLoader, name);
		case OLD:
			return delegateLoad(oldLibFolder, oldClassLoader, name);
		}
		return null;
	}

	private Class<?> delegateLoad(File folder, URLClassLoader cl, String name) {
		try {
			return cl.loadClass(name);
		} catch (ClassNotFoundException e) {
		} catch (NoClassDefFoundError e) {
		}
		return null;
	}

	public List<String> getAllOldTypes() {
		return allOldTypes;
	}
}

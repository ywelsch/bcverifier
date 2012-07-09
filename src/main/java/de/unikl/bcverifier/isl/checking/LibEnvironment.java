package de.unikl.bcverifier.isl.checking;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import de.unikl.bcverifier.isl.ast.Version;

public class LibEnvironment {
	
	
	
	private File oldLibFolder;
	private File newLibFolder;
	private URLClassLoader oldClassLoader;
	private URLClassLoader newClassLoader;

	public LibEnvironment(File oldLibFolder, File newLibFolder) {
		this.oldLibFolder = oldLibFolder;
		this.newLibFolder = newLibFolder;
		try {
			oldClassLoader = new URLClassLoader(new URL[] {oldLibFolder.toURI().toURL()});
			newClassLoader = new URLClassLoader(new URL[] {newLibFolder.toURI().toURL()});
		} catch (MalformedURLException e) {
			// TODO
			e.printStackTrace();
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
			try {
				// try to find package:
				String packageName = findPackageName(folder, "", name);
				if (packageName != null) {
					return cl.loadClass(packageName + name) ;
				}
			} catch (ClassNotFoundException e1) {
				// ignore
			}
		}
		return null;
	}

	private String findPackageName(File folder, String packagename, String classname) {
		for (File f: folder.listFiles()) {
			if (f.isDirectory()) {
				String p = findPackageName(f, f.getName() + "." + packagename, classname);
				if (p != null) {
					return p;
				}
			} else if (f.getName().equals(classname+".class")) {
				return packagename;
			}
		}
		return null;
	}

}

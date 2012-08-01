package de.unikl.bcverifier.sourcecomp;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.unikl.bcverifier.Configuration;
import static de.unikl.bcverifier.isl.ast.Version.NEW;
import static de.unikl.bcverifier.isl.ast.Version.OLD;

public class SourceCompChecker {
	private final Lookup lookup;
	public SourceCompChecker(Configuration c) {
		lookup = new Lookup(c.library1(), c.library2());
	}

	public void check() throws SourceInCompatibilityException {
		checkR1();
		checkR2();
		checkR3();
		checkR4();
	}
	
	private void checkR1() throws SourceInCompatibilityException {
		for (String type : lookup.getAllOldTypes()) {
			Class<?> t1 = lookup.loadClass(OLD, type);
			if (t1.isLocalClass() || t1.isLocalClass()) continue;
			if (t1.isInterface()) {
				if (Modifier.isPublic(t1.getModifiers())) {
					Class<?> t2 = lookup.loadClass(NEW, type);
					if (t2 == null || !t2.isInterface() || !Modifier.isPublic(t2.getModifiers())) {
						throw new SourceInCompatibilityException("Old library implementation has a public interface " + type + " which does not exist in new library implementation");
					}
				}
				continue;
			}
			if (t1.isEnum()) {
				if (Modifier.isPublic(t1.getModifiers())) {
					Class<?> t2 = lookup.loadClass(NEW, type);
					if (t2 == null || !t2.isEnum() || !Modifier.isPublic(t2.getModifiers())) {
						throw new SourceInCompatibilityException("Old library implementation has a public enum " + type + " which does not exist in new library implementation");
					}
				}
				break;
			}
			if (!t1.isInterface()) { // should be class
				if (Modifier.isPublic(t1.getModifiers())) {
					Class<?> t2 = lookup.loadClass(NEW, type);
					if (t2 == null || t2.isInterface() || t2.isEnum() || t2.isLocalClass() || t2.isMemberClass() || !Modifier.isPublic(t2.getModifiers())) {
						throw new SourceInCompatibilityException("Old library implementation has a public class " + type + " which does not exist in new library implementation");
					}
				}
			}
		}
	}
	
	private void checkR2() throws SourceInCompatibilityException {
		for (String type : lookup.getAllOldTypes()) {
			Class<?> t1 = lookup.loadClass(OLD, type);
			if (t1.isLocalClass() || t1.isLocalClass()) continue;
			if (Modifier.isPublic(t1.getModifiers()) && !Modifier.isFinal(t1.getModifiers())) {
				Class<?> t2 = lookup.loadClass(NEW, type);
				if (Modifier.isFinal(t2.getModifiers())) {
					throw new SourceInCompatibilityException("Non-final type " + type + " cannot be made final");
				}
			}
		}
	}
	
	private void checkR3() throws SourceInCompatibilityException {
		for (String type : lookup.getAllOldTypes()) {
			Class<?> t1 = lookup.loadClass(OLD, type);
			if (t1.isLocalClass() || t1.isLocalClass()) continue;
			if (t1.isInterface() && Modifier.isPublic(t1.getModifiers())) {
				Class<?> t2 = lookup.loadClass(NEW, type);
				for (Method m1 : t2.getMethods()) {
					if (methodWithSameSig(t2, m1) == null) {
						throw new SourceInCompatibilityException("Method " + m1 + " for type " + type + " not found in new implementation");
					}
				}
			}
		}
	}

	private void checkR4() throws SourceInCompatibilityException {
		
	}

	private Method methodWithSameSig(Class<?> t2, Method m1) {
		for (Method m2 : t2.getMethods()) {
			if (m1.getName().equals(m2.getName())) {
				if (m1.getReturnType().getName().equals(m2.getReturnType().getName())) {
					if (m1.getParameterTypes().length == m2.getParameterTypes().length) {
						boolean forall = true;
						for (int i = 0; i < m1.getParameterTypes().length; i++) {
							forall = forall && (m1.getParameterTypes()[i].getName().equals(m2.getParameterTypes()[i].getName()));
						}
						if (forall) {
							return m2;
						}
					}
				}
			}
		}
		return null;
	}
}

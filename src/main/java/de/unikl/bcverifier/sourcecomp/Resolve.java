package de.unikl.bcverifier.sourcecomp;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Resolve {
	Map<Class<?>,Set<Method>> methods = new HashMap<Class<?>,Set<Method>>();
	
	public Resolve(List<String> allOldTypes) {
		
	}

	public Set<Method> instanceMethods(Class<?> c) {
		Set<Method> result = methods.get(c);
		if (result == null) {
			result = new HashSet<Method>();
			populateMethods(c, result);
		}
		return result;
	}

	private void populateMethods(Class<?> c, Set<Method> result) {
		// Direct methods
		for (Method m : c.getDeclaredMethods()) {
			if (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
				if (!Modifier.isStatic(m.getModifiers())) {
					result.add(m);
				}
			}
		}
		Class<?> superClass = c.getSuperclass();
		if (superClass != null) {
			for (Method m : instanceMethods(superClass)) {
				result.add(m);
			}
		}
		for (Class<?> superI : c.getInterfaces()) {
			//TODO:
		}
		
	}
}

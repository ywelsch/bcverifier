package de.unikl.bcverifier.sourcecomp;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.TwoLibraryModel;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import static de.unikl.bcverifier.isl.ast.Version.NEW;
import static de.unikl.bcverifier.isl.ast.Version.OLD;

public class SourceCompChecker {
	private final TwoLibraryModel libmodel;
	public SourceCompChecker(Configuration c, TwoLibraryModel libmodel) {
		this.libmodel = libmodel;
	}

	public void check() throws SourceInCompatibilityException {
		final List<TypeDeclaration> tds = new ArrayList<TypeDeclaration>();
		for (CompilationUnit cu : libmodel.getSrc1().getUnits()) {
			cu.accept(new ASTVisitor() {
				@Override public boolean visit(TypeDeclaration td) {
					tds.add(td);
					return true;
				}
			});
		}
		List<ITypeBinding> pubTypes = new ArrayList<ITypeBinding>();
		for (TypeDeclaration td : tds) {
			ITypeBinding tb1 = td.resolveBinding();
			if (Modifier.isPublic(tb1.getModifiers()) && !tb1.isAnonymous() && !tb1.isLocal() && !tb1.isMember()) {
				ITypeBinding tb2 = libmodel.loadType(NEW, tb1.getQualifiedName());
				checkR1(tb1,tb2);
				checkR2R9(tb1,tb2);
				checkR3(tb1,tb2);
				checkR4(tb1,tb2);
				checkR6(tb1,tb2);
				pubTypes.add(tb1);
			}
		}
		// Type hierarchy preserved:
		for (ITypeBinding tb1 : pubTypes) {
			for (ITypeBinding tb2 : pubTypes) {
				if (tb1.isAssignmentCompatible(tb2)) {
					ITypeBinding tb1p = libmodel.loadType(NEW, tb1.getQualifiedName());
					ITypeBinding tb2p = libmodel.loadType(NEW, tb2.getQualifiedName());
					if (!tb1p.isAssignmentCompatible(tb2p)) {
						throw new SourceInCompatibilityException("Subtype relation between " + tb1.getQualifiedName() + " and " + tb2.getQualifiedName() + " not preserved");
					}
				}
			}
		}
		
	}
	
	private void checkR1(ITypeBinding t1, ITypeBinding t2) throws SourceInCompatibilityException {
		if (t1.isInterface()) {
			if (t2 == null || !t2.isInterface() || !Modifier.isPublic(t2.getModifiers())) {
				throw new SourceInCompatibilityException("Old library implementation has a public interface " + t1.getQualifiedName() + " which does not exist in new library implementation");
			}
			return;
		}
		if (t1.isEnum()) {
			if (t2 == null || !t2.isEnum() || !Modifier.isPublic(t2.getModifiers())) {
				throw new SourceInCompatibilityException("Old library implementation has a public enum " + t1.getQualifiedName() + " which does not exist in new library implementation");
			}
			return;
		}
		if (t1.isClass()) { // should be class
			if (t2 == null || !t2.isClass() || !Modifier.isPublic(t2.getModifiers())) {
				throw new SourceInCompatibilityException("Old library implementation has a public class " + t1.getQualifiedName() + " which does not exist in new library implementation");
			}
			return;
		}
	}
	
	private void checkR2R9(ITypeBinding t1, ITypeBinding t2) throws SourceInCompatibilityException {
		if (!Modifier.isFinal(t1.getModifiers()) && Modifier.isFinal(t2.getModifiers())) {
			throw new SourceInCompatibilityException("Non-final type " + t1.getQualifiedName() + " cannot be made final");
		}
		if (!Modifier.isAbstract(t1.getModifiers()) && Modifier.isAbstract(t2.getModifiers())) {
			throw new SourceInCompatibilityException("Non-abstract type " + t1.getQualifiedName() + " cannot be made abstract");
		}
	}
	
	private void checkR3(ITypeBinding t1, ITypeBinding t2) throws SourceInCompatibilityException {
		if (t1.isInterface()) {
			List<IMethodBinding> ml2 = getMethods(t2);
			for (IMethodBinding m2 : ml2) {
				ITypeBinding[] params = m2.getParameterTypes();
				String[] paramsString = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					paramsString[i] = params[i].getQualifiedName();
				}
				IMethodBinding m1 = Bindings.findMethodInHierarchy(t1, m2.getName(), paramsString);
				if (m1 == null) {
					throw new SourceInCompatibilityException("Method " + m2 + " for type " + t2.getQualifiedName() + " not found in old implementation");
				}
			}
		}
	}
	
	private void checkR4(ITypeBinding t1, ITypeBinding t2) throws SourceInCompatibilityException {
		if (t1.isClass() && !Modifier.isFinal(t1.getModifiers())) {
			List<IMethodBinding> ml2 = getMethods(t2);
			for (IMethodBinding m2 : ml2) {
				ITypeBinding[] params = m2.getParameterTypes();
				String[] paramsString = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					paramsString[i] = params[i].getQualifiedName();
				}
				IMethodBinding m1 = Bindings.findMethodInHierarchy(t1, m2.getName(), paramsString);
				if (m1 == null) {
					throw new SourceInCompatibilityException("Method " + m2 + " for type " + t2.getQualifiedName() + " not found in old implementation");
				} else {
					if (Modifier.isFinal(m2.getModifiers()) && !Modifier.isFinal(m1.getModifiers())) {
						throw new SourceInCompatibilityException("Non-final method " + m1 + " for type " + t1.getQualifiedName() + " can not become final");
					}
					if (Modifier.isAbstract(m2.getModifiers()) && !Modifier.isAbstract(m1.getModifiers())) {
						throw new SourceInCompatibilityException("Non-abstract method " + m1 + " for type " + t1.getQualifiedName() + " can not become abstract");
					}
					if (Modifier.isPublic(m1.getModifiers()) && !Modifier.isPublic(m2.getModifiers())) {
						throw new SourceInCompatibilityException("Cannot decrease accessibility of method " + m2 + " in type " + t2.getQualifiedName());
					}
					if (Modifier.isProtected(m1.getModifiers()) && !(Modifier.isPublic(m2.getModifiers()) || Modifier.isProtected(m2.getModifiers()))) {
						throw new SourceInCompatibilityException("Cannot decrease accessibility of method " + m2 + " in type " + t2.getQualifiedName());
					}
				}
			}
		}
	}
	
	private void checkR6(ITypeBinding t1, ITypeBinding t2) throws SourceInCompatibilityException {
		List<IMethodBinding> ml1 = getMethods(t1);
		for (IMethodBinding m1 : ml1) {
			ITypeBinding[] params = m1.getParameterTypes();
			String[] paramsString = new String[params.length];
			for (int i = 0; i < params.length; i++) {
				paramsString[i] = params[i].getQualifiedName();
			}
			IMethodBinding m2 = Bindings.findMethodInHierarchy(t2, m1.getName(), paramsString);
			if (m2 == null) {
				throw new SourceInCompatibilityException("Method " + m1 + " for type " + t1.getQualifiedName() + " not found in new implementation");
			}
		}
		// static methods and constructors
		for (IMethodBinding m1 : t1.getDeclaredMethods()) {
			if (Modifier.isPublic(m1.getModifiers()) || Modifier.isProtected(m1.getModifiers())) {
				ITypeBinding[] params = m1.getParameterTypes();
				String[] paramsString = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					paramsString[i] = params[i].getQualifiedName();
				}
				IMethodBinding m2 = Bindings.findMethodInType(t2, m1.getName(), paramsString);
				if (m1.isConstructor()) {
					if (m2 == null) {
						throw new SourceInCompatibilityException("Constructor " + m1 + " for type " + t1.getQualifiedName() + " not found in new implementation");
					} else {
						if (Modifier.isPublic(m1.getModifiers()) && !Modifier.isPublic(m2.getModifiers())) {
							throw new SourceInCompatibilityException("Cannot decrease accessibility of constructor " + m2 + " in type " + t2.getQualifiedName());
						}
						if (Modifier.isProtected(m1.getModifiers()) && !(Modifier.isPublic(m2.getModifiers()) || Modifier.isProtected(m2.getModifiers()))) {
							throw new SourceInCompatibilityException("Cannot decrease accessibility of constructor " + m2 + " in type " + t2.getQualifiedName());
						}
					}
				}
				if (Modifier.isStatic(m1.getModifiers())) {
					if (m2 == null) {
						throw new SourceInCompatibilityException("Static method " + m1 + " for type " + t1.getQualifiedName() + " not found in new implementation");
					} else {
						if (Modifier.isPublic(m1.getModifiers()) && !Modifier.isPublic(m2.getModifiers())) {
							throw new SourceInCompatibilityException("Cannot decrease accessibility of static method " + m2 + " in type " + t2.getQualifiedName());
						}
						if (Modifier.isProtected(m1.getModifiers()) && !(Modifier.isPublic(m2.getModifiers()) || Modifier.isProtected(m2.getModifiers()))) {
							throw new SourceInCompatibilityException("Cannot decrease accessibility of static method " + m2 + " in type " + t2.getQualifiedName());
						}
					}
				}
			}
 		}
	}
	
	private List<IMethodBinding> getMethods(ITypeBinding t) {
		List<IMethodBinding> result = new ArrayList<IMethodBinding>();
		for (IMethodBinding m : t.getDeclaredMethods()) {
			if (!m.isConstructor() && !Modifier.isStatic(m.getModifiers()) && (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()) || (m.getDeclaringClass() != null && m.getDeclaringClass().isInterface()))) {
				result.add(m);
			}
		}
		for (ITypeBinding st : Bindings.getAllSuperTypes(t)) {
			for (IMethodBinding m : st.getDeclaredMethods()) {
				if (!m.isConstructor() && !Modifier.isStatic(m.getModifiers()) && (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()) || (m.getDeclaringClass() != null && m.getDeclaringClass().isInterface()))) {	
					result.add(m);
				}
			}
		}
		// filter methods that are overridden:
		List<IMethodBinding> overridden = new ArrayList<IMethodBinding>();
		for (IMethodBinding m1 : result) {
			for (IMethodBinding m2 : result) {
				if (m1.overrides(m2)) {
					overridden.add(m2);
				}
			}
		}
		result.removeAll(overridden);
		return result;
		//StubUtility2:
		//getOverridableMethods
	}
	
	private boolean isObjectClass(ITypeBinding t) {
		return t.getQualifiedName().equals("java.lang.Object");
	}


	/*private Method methodWithSameSig(Class<?> t2, Method m1) {
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
	}*/
}

package de.unikl.bcverifier.isl.checking.types;

import static de.unikl.bcverifier.isl.ast.Version.BOTH;
import static de.unikl.bcverifier.isl.ast.Version.NEW;
import static de.unikl.bcverifier.isl.ast.Version.OLD;

import org.eclipse.jdt.core.dom.ITypeBinding;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class JavaType extends ExprType {

	private Version version;
	private String qualifiedName;
	private TwoLibraryModel env;
	private ITypeBinding typeBinding;

	
	private JavaType(TwoLibraryModel env, Version version, ITypeBinding type) {
		this.env = env;
		this.version = version;
		this.qualifiedName = type.getQualifiedName();
		this.typeBinding = type;
	}
	
	public static ExprType create(ASTNode<?> loc, Version version, String qualifiedName) {
		TwoLibraryModel env = loc.attrCompilationUnit().getTwoLibraryModel();
		ITypeBinding c = env.getSrc(version).resolveType(qualifiedName);
		if (c == null) {
			loc.addError(loc, "Could not find class " + qualifiedName + " in " + version);
			return UnknownType.instance();
		}
		return JavaType.create(env, version, c);
	}
	
	
	public static ExprType create(ASTNode<?> loc, Version version, ITypeBinding type) {
		return JavaType.create(loc.attrCompilationUnit().getTwoLibraryModel(), version, type);
	}
	
	public static ExprType create(TwoLibraryModel env, Version version, ITypeBinding type) {
		if (type.isPrimitive()) {
			if (type.getQualifiedName().equals("boolean")) {
				return ExprTypeBool.instance();
			}
			if (type.getQualifiedName().equals("int")) {
				return ExprTypeInt.instance();
			}
		}
		return new JavaType(env, version, type);
	}
	
	

	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t instanceof JavaType) {
			JavaType j = (JavaType) t;
			if (j.version != Version.BOTH && j.version != version) {
				return false;
			}
			// TODO check if javatype is subtype
			return true;
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		return version + " " + qualifiedName;
	}

	public Version getVersion() {
		return version;
	}
	
	public boolean isOld() {
		return version == OLD || version == BOTH;
	}

	public boolean isNew() {
		return version == NEW || version == BOTH;
	}

	public static ExprType object(TwoLibraryModel env) {
		ITypeBinding t = env.getSrc1().resolveType(Object.class.getName());
		return new JavaType(null, Version.BOTH, t);
	}

	public static ExprType getJavaLangObject(TwoLibraryModel env, Version version) {
		ITypeBinding t = env.getSrc(version).resolveType(Object.class.getName());
		return new JavaType(env, version, t);
	}

	public ITypeBinding getTypeBinding() {
		return typeBinding;
	}
}

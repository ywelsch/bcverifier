package de.unikl.bcverifier.isl.checking.types;

import static de.unikl.bcverifier.isl.ast.Version.*;

import java.util.List;

import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.LibEnvironment;

public class JavaType extends ExprType {

	private Version version;
	private String qualifiedName;
	private Class<?> clazz;
	private TwoLibraryModel env;

	
	private JavaType(TwoLibraryModel env, Version version, Class<?> clazz) {
		this.env = env;
		this.version = version;
		this.qualifiedName = clazz.getName();
		this.clazz = clazz;
	}
	
	public static ExprType create(ASTNode<?> loc, Version version, String qualifiedName) {
		TwoLibraryModel env = loc.attrCompilationUnit().getTwoLibraryModel();
		Class<?> c = env.loadType(version, qualifiedName);
		if (c == null) {
			loc.addError(loc, "Could not find class " + qualifiedName + " in " + version);
			return UnknownType.instance();
		}
		return JavaType.create(env, version, c);
	}
	
	
	public static ExprType create(ASTNode<?> loc, Version version, Class<?> clazz) {
		return JavaType.create(loc.attrCompilationUnit().getTwoLibraryModel(), version, clazz);
	}
	
	public static ExprType create(TwoLibraryModel env, Version version, Class<?> clazz) {
		if (boolean.class.equals(clazz)) {
			return ExprTypeBool.instance();
		}
		if (int.class.equals(clazz)) {
			return ExprTypeInt.instance();
		}
		return new JavaType(env, version, clazz);
	}
	
	

	public boolean isSubtypeOf(ExprType t) {
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
	
	public Class<?> getJavaClass() {
		return clazz;
	}
	
	public boolean isOld() {
		return version == OLD || version == BOTH;
	}

	public boolean isNew() {
		return version == NEW || version == BOTH;
	}

	public static ExprType object() {
		return new JavaType(null, Version.BOTH, Object.class);	
	}

	public static ExprType getJavaLangObject(TwoLibraryModel env2, Version old) {
		// TODO Auto-generated method stub
		throw new Error();
	}
}

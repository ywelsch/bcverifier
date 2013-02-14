package de.unikl.bcverifier.isl.checking.types;

import static de.unikl.bcverifier.isl.ast.Version.BOTH;
import static de.unikl.bcverifier.isl.ast.Version.NEW;
import static de.unikl.bcverifier.isl.ast.Version.OLD;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import com.google.common.base.Preconditions;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.TypeHelper;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeJavaType extends ExprType implements ExprTypeHasMembers {

	private Version version;
	private String qualifiedName;
	private TwoLibraryModel env;
	private ITypeBinding typeBinding;
	
	private static final ExprTypeJavaType nullType = new ExprTypeJavaType();
	
	public static ExprTypeJavaType nullType() {
		return nullType;
	}

	
	private ExprTypeJavaType(TwoLibraryModel env, Version version, ITypeBinding type) {
		this.env = env;
		this.version = version;
		this.qualifiedName = type.getQualifiedName();
		this.typeBinding = type;
	}
	
	private ExprTypeJavaType() {
		// TODO Auto-generated constructor stub
	}


	public static ExprType create(ASTNode<?> loc, Version version, String qualifiedName) {
		if (version == BOTH) {
			loc.addError("No version specified for type " + qualifiedName + ".");
			return ExprTypeUnknown.instance();
		}
		TwoLibraryModel env = loc.attrCompilationUnit().getTwoLibraryModel();
		ITypeBinding c = env.getSrc(version).resolveType(qualifiedName);
		if (c == null) {
			loc.addError(loc, "Could not find class " + qualifiedName + " in " + version);
			return ExprTypeUnknown.instance();
		}
		return ExprTypeJavaType.create(env, version, c);
	}
	
	
	public static ExprType create(ASTNode<?> loc, Version version, ITypeBinding type) {
		return ExprTypeJavaType.create(loc.attrCompilationUnit().getTwoLibraryModel(), version, type);
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
		return new ExprTypeJavaType(env, version, type);
	}
	
	

	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeAny) {
			return true;
		}
		if (t instanceof ExprTypeJavaType) {
			ExprTypeJavaType j = (ExprTypeJavaType) t;
			if (this == nullType)
				return true;
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
		return new ExprTypeJavaType(null, Version.BOTH, t);
	}

	public static ExprType getJavaLangObject(TwoLibraryModel env, Version version) {
		ITypeBinding t = env.getSrc(version).resolveType(Object.class.getName());
		return new ExprTypeJavaType(env, version, t);
	}

	public ITypeBinding getTypeBinding() {
		return typeBinding;
	}


	@Override
	public IBinding findMember(String name) {
		if (typeBinding == null) {
			return null;
		}
		return Bindings.findFieldInType(typeBinding, name);
	}


	@Override
	public ExprType typeOfMemberAccess(ASTNode<?> location, String name) {
		IBinding binding = findMember(name);
		if (binding instanceof IVariableBinding) {
			IVariableBinding varBinding = (IVariableBinding) binding;
			if (TypeHelper.isStatic(varBinding)){}
			return ExprTypeJavaType.create(env, version, varBinding.getType());
		}
		location.addError("Field " + name + " could not be found in " + this);
		return ExprTypeUnknown.instance();
	}
}

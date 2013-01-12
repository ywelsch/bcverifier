package de.unikl.bcverifier.isl.checking.types;

import static de.unikl.bcverifier.isl.ast.Version.BOTH;
import static de.unikl.bcverifier.isl.ast.Version.NEW;
import static de.unikl.bcverifier.isl.ast.Version.OLD;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTypeJavaPackageRef extends ExprType implements ExprTypeHasMembers {

	private Version version;
	private String qualifiedName;
	private TwoLibraryModel env;
	
	private ExprTypeJavaPackageRef(TwoLibraryModel env, Version version, String qualifiedName) {
		this.env = env;
		this.version = version;
		this.qualifiedName = qualifiedName;
	}
	

	public static ExprType create(ASTNode<?> loc, Version version, String qualifiedName) {
		return new ExprTypeJavaPackageRef(loc.attrCompilationUnit().getTwoLibraryModel(), version, qualifiedName);
	}
	public static ExprType create(TwoLibraryModel env, Version version,
			String qualifiedName) {
		return new ExprTypeJavaPackageRef(env, version, qualifiedName);
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

	@Override
	public boolean isSubtypeOf(ExprType t) {
		if (t instanceof ExprTypeJavaPackageRef) {
			ExprTypeJavaPackageRef pr = (ExprTypeJavaPackageRef) t;
			// TODO ?
			return true;
		}
		return false;
	}


	@Override
	public IBinding findMember(String name) {
		return env.getSrc(version).resolveType(qualifiedName + "." + name);
	}


	@Override
	public ExprType typeOfMemberAccess(ASTNode<?> location, String name) {
		IBinding binding = findMember(name);
		if (binding instanceof ITypeBinding) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			return ExprTypeJavaTypeRef.create(env, version, typeBinding);
		}
		return ExprTypeJavaPackageRef.create(env, version, qualifiedName + "." +name);
	}


	
}

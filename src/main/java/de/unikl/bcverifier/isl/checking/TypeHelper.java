package de.unikl.bcverifier.isl.checking;

import java.util.Set;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import com.google.common.collect.Sets;

import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.Statement;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeHasMembers;

public class TypeHelper {

	public static void checkIfSubtype(Expr e, ExprType expected) {
		if (!(e.attrType().isSubtypeOf(expected))) {
			e.addError(e, "Expected expression of type " + expected
					+ " but found " + e.attrType());
		}
	}
	

	static String getQualifiedName(List<Ident> names) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Ident i : names) {
			if (!first) {
				sb.append(".");
			}
			first = false;
			sb.append(i.getName());
		}
		return sb.toString();
	}

	

	public static boolean isStatic(IVariableBinding field) {
		return (field.getModifiers() & Modifier.STATIC) > 0;
	}


	public static IVariableBinding attrField(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		String fieldName = e.getRight().getName();
		if (leftType instanceof ExprTypeHasMembers) {
			ExprTypeHasMembers leftT = (ExprTypeHasMembers) leftType;
			IBinding typeBinding = leftT.findMember(fieldName);
			if (typeBinding instanceof IVariableBinding) {
				return (IVariableBinding) typeBinding;
			}
		}
		return null;
	}

	
	public static <T extends de.unikl.bcverifier.isl.ast.ASTNode<?>> T getParentOfType(de.unikl.bcverifier.isl.ast.ASTNode<?> n, Class<T> type) {
		while (n != null) {
			if (type.isAssignableFrom(n.getClass())) {
				@SuppressWarnings("unchecked")
				T result = (T) n;
				return result;
			}
			n = n.getParent();
		}
		return null;
	}


	public static void checkCompilationUnit(CompilationUnit cu) {
		checkForDuplicateNames(cu);
		checkForDuplicateInvariants(cu);
	}

	private static void checkForDuplicateInvariants(CompilationUnit cu) {
		Set<String> invExprs = Sets.newHashSet();
		for (Statement s : cu.getStatements()) {
			if (s instanceof Invariant) {
				Invariant invariant = (Invariant) s;
				String expr = invariant.getExpr().print();
				boolean notExists = invExprs.add(expr);
				if (!notExists) {
					invariant.addError("An identical invariant already exists: " + expr);
				}
			}
		}
	}


	/**
	 * checks that each definition has a unique name
	 */
	private static void checkForDuplicateNames(CompilationUnit cu) {
		Set<String> names = Sets.newHashSet();
		for (Def d : cu.attrDefinedVars()) {
			String name = d.attrName();
			boolean notExists = names.add(name);
			if (!notExists && !name.startsWith("<")) {
				d.addError("An element with name " + name + " is already defined.");
			}
		}
	}


}

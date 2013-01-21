package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
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
				return (T) n;
			}
			n = n.getParent();
		}
		return null;
	}


}

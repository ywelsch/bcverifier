package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NamedTypeDef;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.PlacePosition;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.VarDef;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.translation.BuiltinFunction;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.UnknownType;

public class TypeHelper {

	public static ExprType attrType(NamedTypeDef t) {
		Version version = t.getVersion();
		String qualifiedName = getQualifiedName(t.getNames());

		return JavaType.create(t,version, qualifiedName);
	}

	private static String getQualifiedName(List<Ident> names) {
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

	public static ExprType attrType(BinaryOperation bo) {
		TwoLibraryModel env = bo.attrCompilationUnit().getTwoLibraryModel();

		Expr left = bo.getLeft();
		Expr right = bo.getRight();
		switch (bo.getOperator()) {
		case AND:
		case OR:
		case IMPLIES:
			checkIfSubtype(left, ExprTypeBool.instance());
			checkIfSubtype(right, ExprTypeBool.instance());
			return ExprTypeBool.instance();
		case RELATED:
			checkIfSubtype(left, JavaType.getJavaLangObject(env, Version.OLD));
			checkIfSubtype(right, JavaType.getJavaLangObject(env, Version.NEW));
			return ExprTypeBool.instance();
		case UNEQUALS:
		case EQUALS:
			if (!left.attrType().getClass().equals(right.attrType().getClass())) {
				bo.addError("Cannot compare types " + left.attrType() + " and " + right.attrType());
			}
			return ExprTypeBool.instance();
		case DIV:
		case MINUS:
		case MULT:
		case PLUS:
		case MOD:
			checkIfSubtype(left, ExprTypeInt.instance());
			checkIfSubtype(right, ExprTypeInt.instance());
			return ExprTypeInt.instance();
		}
		bo.addError("Operator not implemented: " + bo.getOperator());
		return UnknownType.instance();
	}

	private static void checkIfSubtype(Expr e, ExprType expected) {
		if (!(e.attrType().isSubtypeOf(expected))) {
			e.addError(e, "Expected expression of type "+expected+" but found " + e.attrType());
		}
	}

	public static ExprType attrType(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		if (leftType instanceof JavaType) {
			JavaType javaType = (JavaType) leftType;
			IVariableBinding field = e.attrField();
			if (field != null) {
				return JavaType.create(e.getRight(), javaType.getVersion(), field.getType());
			} else {
				e.addError("Could not find field " + e.getRight().getName() + " in class " + leftType);
			}
		}
		e.addError("Left hand side of member acces is of type " + leftType + " (expected Java type).");
		return UnknownType.instance();
	}

	public static ExprType attrType(IfThenElse e) {
		checkIfSubtype(e.getCond(), ExprTypeBool.instance());
		ExprType thenType = e.getThenExpr().attrType();
		ExprType elseType = e.getElseExpr().attrType();
		if (!thenType.equals(elseType)) {
			e.addError("Types " + thenType + " and " + elseType + " are not equal");
		}
		// could try to find common supertype if types are different
		return thenType;
	}

	public static IVariableBinding attrField(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		if (leftType instanceof JavaType) {
			JavaType javaType = (JavaType) leftType;
			String fieldName = e.getRight().getName();
			ITypeBinding typeBinding = javaType.getTypeBinding();
			IVariableBinding field = Bindings.findFieldInType(typeBinding, fieldName);
			// TODO search fields in super classes?
			return field;
		}
		return null;
	}

	public static ExprType attrType(UnaryOperation e) {
		switch (e.getOperator()) {
		case NOT:
			checkIfSubtype(e.getExpr(), ExprTypeBool.instance());
			return ExprTypeBool.instance();
		case UMINUS:
			checkIfSubtype(e.getExpr(), ExprTypeInt.instance());
			return ExprTypeInt.instance();
		}
		throw new Error("not implemented: " + e.getOperator());
	}

	public static ExprType attrType(FuncCall e) {
		Def def = e.attrDef();
		if (!(def instanceof BuiltinFunction)) {
			e.addError(def.attrName() + " is not a function.");
		} else {
			BuiltinFunction f = (BuiltinFunction) def;
			if (e.getNumArgument() > f.getParameterTypes().size()) {
				e.addError("Too many arguments.");
			} else if (e.getNumArgument() < f.getParameterTypes().size()) {
				e.addError("Missing arguments.");
			} else {
				for (int i=0; i<e.getNumArgument(); i++) {
					ExprType argType = e.getArgument(i).attrType();
					ExprType expectedType = f.getParameterTypes().get(i);
					if (!argType.isSubtypeOf(expectedType)) {
						e.getArgument(i).addError("found " + argType + " but expected " + expectedType);
					}
				}
			}
		}
		return def.attrType();
	}

	public static ExprType attrType(VarAccess e)  {
		Def def = e.attrDef();
		if (!(def instanceof VarDef)) {
			e.addError(def.attrName() + " is not a variable.");
		}
		return def.attrType();
	}

	public static ExprType attrType(NullConst e) {
		return JavaType.object(e.attrCompilationUnit().getTwoLibraryModel());
	}

	public static ExprType placeDefType(PlaceDef placeDef) {
		PlacePosition pos = placeDef.getPlacePosition();
		// TODO
		throw new Error("not implemented");
	}

}

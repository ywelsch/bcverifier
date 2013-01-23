package de.unikl.bcverifier.isl.checking;

import static de.unikl.bcverifier.isl.checking.TypeHelper.checkIfSubtype;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.CallProgramPoint;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.ExprTypeRef;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.InstanceofOperation;
import de.unikl.bcverifier.isl.ast.LineNrProgramPoint;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.VersionConst;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAtLineProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeCallProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeHasMembers;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaPackageRef;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaTypeRef;
import de.unikl.bcverifier.isl.checking.types.ExprTypeUnknown;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class CalculateExprType {

	public static ExprType attrType(BinaryOperation bo) {
		TwoLibraryModel env = bo.attrCompilationUnit().getTwoLibraryModel();

		Expr left = bo.getLeft();
		Expr right = bo.getRight();
		switch (bo.getOperator()) {
		case AND:
		case OR:
		case IMPLIES:
		case IFF:
			checkIfSubtype(left, ExprTypeBool.instance());
			checkIfSubtype(right, ExprTypeBool.instance());
			return ExprTypeBool.instance();
		case RELATED:
			checkIfSubtype(left, ExprTypeJavaType.getJavaLangObject(env, Version.OLD));
			checkIfSubtype(right, ExprTypeJavaType.getJavaLangObject(env, Version.NEW));
			return ExprTypeBool.instance();
		case UNEQUALS:
		case EQUALS:
			if (!left.attrType().getClass().equals(right.attrType().getClass())) {
				bo.addError("Cannot compare types " + left.attrType() + " and "
						+ right.attrType());
			} else {
				if (left.attrType() instanceof ExprTypeJavaType) {
					ExprTypeJavaType lt = (ExprTypeJavaType) left.attrType();
					ExprTypeJavaType rt = (ExprTypeJavaType) right.attrType();
					if (lt != ExprTypeJavaType.nullType()
							&& rt != ExprTypeJavaType.nullType()
							&& lt.getVersion() != rt.getVersion()) {
						bo.addError("Cannot compare objects from different library versions.");
					}
				}
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
		case GT:
		case GTEQ:
		case LT:
		case LTEQ:
			checkIfSubtype(left, ExprTypeInt.instance());
			checkIfSubtype(right, ExprTypeInt.instance());
			return ExprTypeBool.instance();
		}
		bo.addError("Operator not implemented: " + bo.getOperator());
		return ExprTypeUnknown.instance();
	}

	

	public static ExprType attrType(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		String name = e.getRight().getName();
		if (leftType instanceof ExprTypeHasMembers) {
			ExprTypeHasMembers leftType2 = (ExprTypeHasMembers) leftType;
			return leftType2.typeOfMemberAccess(e, name);
		}
		e.addError("Left hand side of member access is of type " + leftType
				+ " (expected Java type).");
		return ExprTypeUnknown.instance();
	}
	

	public static ExprType attrType(IfThenElse e) {
		checkIfSubtype(e.getCond(), ExprTypeBool.instance());
		ExprType thenType = e.getThenExpr().attrType();
		ExprType elseType = e.getElseExpr().attrType();
		if (!thenType.equals(elseType)) {
			e.addError("Types " + thenType + " and " + elseType
					+ " are not equal");
		}
		// could try to find common supertype if types are different
		return thenType;
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
			boolean hasErrors = false;
			if (e.getNumArgument() > f.getParameterTypes().size()) {
				e.addError("Too many arguments.");
				hasErrors = true;
			} else if (e.getNumArgument() < f.getParameterTypes().size()) {
				e.addError("Missing arguments.");
				hasErrors = true;
			} else {
				for (int i = 0; i < e.getNumArgument(); i++) {
					ExprType argType = e.getArgument(i).attrType();
					ExprType expectedType = f.getParameterTypes().get(i);
					if (!argType.isSubtypeOf(expectedType)) {
						e.getArgument(i).addError(
								"found " + argType + " but expected "
										+ expectedType);
						hasErrors = true;
					}
				}
			}
			if (!hasErrors) {
				return f.exactType(e);
			}
		}
		return def.attrType();
	}

	public static ExprType attrType(VarAccess e) {
		Def def = e.attrDef();
		return def.attrType();
	}

	public static ExprType attrType(NullConst e) {
		return ExprTypeJavaType.nullType();
	}
	
	public static ExprType attrType(InstanceofOperation op) {
		ExprType left = op.getLeft().attrType();
		ExprType right = op.getRight().attrType();
		if (!(left instanceof ExprTypeJavaType)) {
			op.addError("instanceof expects a java type on the left hand side but found " + left);
		} else if (!(right instanceof ExprTypeJavaTypeRef)) {			
			op.addError("instanceof expects a reference to a java type on the right hand side but found " + left);
		} else {
			if (!((ExprTypeJavaType)left).getVersion().equals(((ExprTypeJavaTypeRef)right).getVersion())) {
				op.addError("instanceof must compare type of same library implementation");
			}
		}
		return ExprTypeBool.instance();
	}
	
	public static ExprType attrType(VersionConst c) {
		return ExprTypeVersion.get(c.getVal());
	}

	public static ExprType attrType(ExprTypeRef e) {
		TwoLibraryModel env = e.attrCompilationUnit().getTwoLibraryModel();
		String name = e.getRight().getName();
		Version version = e.getVersion();
		ITypeBinding jt = env.getSrc(version).resolveType(name);
		if (jt != null) {
			return ExprTypeJavaTypeRef.create(env, version, jt);
		}
		return ExprTypeJavaPackageRef.create(env, version, name);
	}
	
	
	
	
	public static ExprType attrType(LineNrProgramPoint p) {
		ExprType type = p.getTypeDef().attrType();
		if (!(type instanceof ExprTypeJavaType)) {
			p.getTypeDef().addError("Program point must refer to a Java type.");
			return ExprTypeUnknown.instance();
		}
		ExprTypeJavaType jt = (ExprTypeJavaType) type;
		final TwoLibraryModel model = p.attrCompilationUnit()
				.getTwoLibraryModel();
		ASTNode node = model.getSrc(jt.getVersion()).findDeclaringNode(jt.getTypeBinding());

		if (node == null) {
			p.addError("Type " + type + " is not part of the given library.");
			return ExprTypeUnknown.instance();
		}
		
		Statement s = getStatementInLine(model, node, p.getProgramLineNr());
		if (s == null) {
			p.addError("No statement found in line " + p.getProgramLineNr() +".");
			return ExprTypeUnknown.instance();
		}
		// TODO check if there is exactly one statement in the line ...
		return new ExprTypeAtLineProgramPoint(jt.getVersion(), p.getProgramLineNr(),
				s);
	}
	
	public static ExprType attrType(CallProgramPoint p) {
		ExprType type = p.getTypeDef().attrType();
		if (!(type instanceof ExprTypeJavaType)) {
			p.getTypeDef().addError("Program point must refer to a Java type.");
			return ExprTypeUnknown.instance();
		}
		ExprTypeJavaType jt = (ExprTypeJavaType) type;
		final TwoLibraryModel model = p.attrCompilationUnit()
				.getTwoLibraryModel();
		ASTNode node = model.getSrc(jt.getVersion()).findDeclaringNode(jt.getTypeBinding());

		if (node == null) {
			p.addError("Type " + type + " is not part of the given library.");
			return ExprTypeUnknown.instance();
		}
		
		MethodInvocation inv = getMethodInvocationInLine(p.getFunctionName().getName(), model, node, p.getProgramLineNr());
		if (inv == null) {
			p.addError("No method call found in this line.");
			return ExprTypeUnknown.instance();
		}
		
		return new ExprTypeCallProgramPoint(model, jt.getVersion(), p.getProgramLineNr(), inv);
	}
	

	private static Statement getStatementInLine(final TwoLibraryModel model,
			ASTNode parent, final int lineNr) {
		final Statement[] result = new Statement[1];
		parent.accept(new StatementVisitor() {

			@Override
			boolean visitStatement(Statement s) {
				if (TwoLibraryModel.getLineNr(s) == lineNr) {
					result[0] = s;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	private static MethodInvocation getMethodInvocationInLine(final String funcname,
			final TwoLibraryModel model, ASTNode node, final int programLineNr) {
		final MethodInvocation[] result = new MethodInvocation[1];
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation inv) {
				if (inv.getName().getFullyQualifiedName().equals(funcname)) {
					if (TwoLibraryModel.getLineNr(inv) == programLineNr) {
						result[0] = inv;
						return false;
					}
				}
				return true;
			}
			
		});
		return result[0];
	}
	
}

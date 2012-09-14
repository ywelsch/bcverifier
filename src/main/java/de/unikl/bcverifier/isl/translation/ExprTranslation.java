package de.unikl.bcverifier.isl.translation;

import org.eclipse.jdt.core.dom.IVariableBinding;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLBinaryArithmeticExpression;
import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLIfThenElseExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLLogicalNotExpression;
import b2bpl.bpl.ast.BPLQuantifierExpression;
import b2bpl.bpl.ast.BPLQuantifierExpression.Operator;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLUnaryMinusExpression;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.BoolConst;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.ErrorExpr;
import de.unikl.bcverifier.isl.ast.ForallExpr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.IntConst;
import de.unikl.bcverifier.isl.ast.LineNrProgramPoint;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.VarDef;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.JavaVariableDef;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class ExprTranslation {

	private static final b2bpl.bpl.ast.BPLEqualityExpression.Operator NOT_EQUALS = BPLEqualityExpression.Operator.NOT_EQUALS;
	private static final b2bpl.bpl.ast.BPLEqualityExpression.Operator EQUALS = BPLEqualityExpression.Operator.EQUALS;
	private static final Operator FORALL = BPLQuantifierExpression.Operator.FORALL;
	private static final b2bpl.bpl.ast.BPLBinaryLogicalExpression.Operator AND = BPLBinaryLogicalExpression.Operator.AND;
	private static final b2bpl.bpl.ast.BPLBinaryLogicalExpression.Operator OR = BPLBinaryLogicalExpression.Operator.OR;

	public static BPLExpression translate(BinaryOperation e) {
		BPLExpression left = e.getLeft().translateExpr();
		BPLExpression right = e.getRight().translateExpr();
		switch (e.getOperator()) {
		case IMPLIES:
			return new BPLBinaryLogicalExpression(BPLBinaryLogicalExpression.Operator.IMPLIES, left, right);
		case IFF:
			return new BPLBinaryLogicalExpression(BPLBinaryLogicalExpression.Operator.EQUIVALENCE, left, right);
		case AND:
			return new BPLBinaryLogicalExpression(BPLBinaryLogicalExpression.Operator.AND, left, right);
		case OR:
			return new BPLBinaryLogicalExpression(BPLBinaryLogicalExpression.Operator.OR, left, right);
		case RELATED:
			BPLExpression related = new BPLVariableExpression("related"); //$NON-NLS-1$
			return new BPLFunctionApplication("RelNull", left, right, related ); //$NON-NLS-1$
		case EQUALS:
			return new BPLEqualityExpression(EQUALS, left, right);
		case UNEQUALS:
			return new BPLEqualityExpression(NOT_EQUALS, left, right);
		case MOD:
			return new BPLBinaryArithmeticExpression(BPLBinaryArithmeticExpression.Operator.REMAINDER, left, right);
		case DIV:
			return new BPLBinaryArithmeticExpression(BPLBinaryArithmeticExpression.Operator.DIVIDE, left, right);
		case MINUS:
			return new BPLBinaryArithmeticExpression(BPLBinaryArithmeticExpression.Operator.MINUS, left, right);
		case MULT:
			return new BPLBinaryArithmeticExpression(BPLBinaryArithmeticExpression.Operator.TIMES, left, right);
		case PLUS:
			return new BPLBinaryArithmeticExpression(BPLBinaryArithmeticExpression.Operator.PLUS, left, right);
		case GT:
			return new BPLRelationalExpression(BPLRelationalExpression.Operator.GREATER, left, right);
		case GTEQ:
			return new BPLRelationalExpression(BPLRelationalExpression.Operator.GREATER_EQUAL, left, right);
		case LT:
			return new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS, left, right);
		case LTEQ:
			return new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, left, right);
			
		}
		throw new Error("unhandled case: " + e.getOperator()); //$NON-NLS-1$
	}

	public static BPLExpression translate(BoolConst e) {
		if (e.getVal()) {
			return BPLBoolLiteral.TRUE;
		} else {
			return BPLBoolLiteral.FALSE;
		}
	}

	public static BPLExpression translate(ErrorExpr e) {
		throw new Error("Translating invalid expression."); //$NON-NLS-1$
	}

	public static BPLExpression translate(ForallExpr e) {
		BPLExpression rightExpr = e.getExpr().translateExpr();
		List<VarDef> boundVars = e.getBoundVars();
		return createForallExpr(boundVars, rightExpr);
	}

	/**
	 * creates a forall expression including the assumptions about the types
	 * of the bound variables
	 * @param rightExpr
	 * @param boundVars
	 * @return
	 */
	public static BPLExpression createForallExpr(List<VarDef> boundVars, BPLExpression rightExpr) {
		BPLVariable[] variables = new BPLVariable[boundVars.getNumChild()];
		int i = 0;
		for (VarDef boundVar : boundVars) {
			BPLType type = new BPLTypeName("Ref"); //$NON-NLS-1$
			variables[i] = new BPLVariable(boundVar.attrName(), type);
			i++;
		}
		BPLExpression leftExpr = null;
		for (VarDef boundVar : boundVars) {
			if (boundVar.attrType() instanceof JavaType) {
				JavaType javaType = (JavaType) boundVar.attrType();
				BPLExpression typeAssumtion = makeTypeAssumption(boundVar, javaType);
				if (leftExpr == null) {
					leftExpr = typeAssumtion;
				} else {
					leftExpr = new BPLBinaryLogicalExpression(AND, leftExpr, typeAssumtion);
				}
			}
			 
		}
		
		b2bpl.bpl.ast.BPLBinaryLogicalExpression.Operator implies = BPLBinaryLogicalExpression.Operator.IMPLIES;
		BPLExpression expr;
		if (leftExpr != null) {
			expr = new BPLBinaryLogicalExpression(implies, leftExpr, rightExpr);
		} else {
			expr = rightExpr;
		}
		return new BPLQuantifierExpression(FORALL, variables, expr);
	}

	/**
	 * creates a type assumption.
	 * example: Obj(heap1, o1) && RefOfType(o1, heap1, $cell.Cell) 
	 * @param boundVar
	 * @param javaType
	 * @return
	 */
	private static BPLExpression makeTypeAssumption(VarDef boundVar, JavaType javaType) {
		switch(javaType.getVersion()) {
			case NEW:
			case OLD:
				return makeTypeAssumption(boundVar, javaType, javaType.getVersion());
			case BOTH:
				return new BPLBinaryLogicalExpression(OR, 
						makeTypeAssumption(boundVar, javaType, Version.OLD), 
						makeTypeAssumption(boundVar, javaType, Version.NEW));
		}
		throw new Error();
	}

	private static BPLBinaryLogicalExpression makeTypeAssumption(VarDef boundVar, JavaType javaType,
			Version version) {
		return new BPLBinaryLogicalExpression(AND, 
				new BPLFunctionApplication("Obj",  //$NON-NLS-1$
						getHeap(version), 
						new BPLVariableExpression(boundVar.attrName())), 
				new BPLFunctionApplication("RefOfType",  //$NON-NLS-1$
						new BPLVariableExpression(boundVar.attrName()),
						getHeap(version), 
						new BPLVariableExpression("$" + javaType.getTypeBinding().getQualifiedName()) //$NON-NLS-1$
						));
	}

	public static BPLExpression getHeap(Version version) {
		if (version == Version.OLD) {
			return new BPLVariableExpression("heap1"); //$NON-NLS-1$
		} else if (version == Version.NEW) {
			return new BPLVariableExpression("heap2"); //$NON-NLS-1$
		}
		throw new Error("unhandled case: "+ version); //$NON-NLS-1$
	}

	public static BPLExpression translate(FuncCall e) {
		Def def = e.attrDef();
		if (def instanceof BuiltinFunction) {
			BuiltinFunction f = (BuiltinFunction) def;
			return f.translateCall(e.getArguments());
		}
		throw new Error("not implemented " + def);
	}

	public static BPLExpression translate(IfThenElse e) {
		return new BPLIfThenElseExpression(
				e.getCond().translateExpr(), 
				e.getThenExpr().translateExpr(), 
				e.getElseExpr().translateExpr());
	}

	public static BPLExpression translate(MemberAccess e) {
		if (e.getLeft().attrType() instanceof JavaType) {
			JavaType leftType = (JavaType) e.getLeft().attrType();
			
			IVariableBinding field = e.attrField();
			if (field == null) {
				throw new Error();
			}
			
			BPLExpression expr = new BPLArrayExpression(
					getHeap(leftType.getVersion()), 
					e.getLeft().translateExpr(),
					new BPLVariableExpression("$" + leftType.getTypeBinding().getQualifiedName() + "." + e.getRight().getName()));
			
			if (field.getType().getQualifiedName().equals("boolean")) {
				// boolean fields must be converted explicitly
				expr = new BPLFunctionApplication("int2bool", expr);
			}
			return expr;
		}
		throw new Error();
	}

	public static BPLExpression translate(VarAccess e) {
		Def def = e.attrDef();
		if (def instanceof JavaVariableDef) {
			JavaVariableDef jv = (JavaVariableDef) def;
			return BuiltinFunctions.stackProperty(
					jv.getVersion(), 
					jv.getStackPointerExpr().translateExpr(), 
					new BPLVariableExpression(jv.getRegisterName()));
		}
		return new BPLVariableExpression(e.getName().getName());
	}

	public static BPLExpression translate(IntConst e) {
		return new BPLIntLiteral(Integer.parseInt(e.getVal()));
	}

	public static BPLExpression translate(NullConst nullConst) {
		return new BPLVariableExpression("null");
	}

	public static BPLExpression translate(UnaryOperation e) {
		switch (e.getOperator()) {
		case UMINUS:
			return new BPLUnaryMinusExpression(e.getExpr().translateExpr());
		case NOT:
			return new BPLLogicalNotExpression(e.getExpr().translateExpr());
		}
		throw new Error("not implemented: " + e.getOperator());
	}

	public static BPLExpression translate(LineNrProgramPoint lineNrProgramPoint) {
		throw new Error("Cannot translate program point expressions.");
	}


}

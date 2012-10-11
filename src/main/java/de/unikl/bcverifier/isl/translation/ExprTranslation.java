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
import b2bpl.bpl.ast.BPLNullLiteral;
import b2bpl.bpl.ast.BPLQuantifierExpression;
import b2bpl.bpl.ast.BPLQuantifierExpression.Operator;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLUnaryMinusExpression;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.BoolConst;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.ErrorExpr;
import de.unikl.bcverifier.isl.ast.InstanceofOperation;
import de.unikl.bcverifier.isl.ast.QExpr;
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
import de.unikl.bcverifier.isl.checking.types.BijectionType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeLocalPlace;
import de.unikl.bcverifier.isl.checking.types.ExprTypePredefinedPlace;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.parser.Quantifier;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;

public class ExprTranslation {

	private static final b2bpl.bpl.ast.BPLEqualityExpression.Operator NOT_EQUALS = BPLEqualityExpression.Operator.NOT_EQUALS;
	private static final b2bpl.bpl.ast.BPLEqualityExpression.Operator EQUALS = BPLEqualityExpression.Operator.EQUALS;
	private static final Operator FORALL = BPLQuantifierExpression.Operator.FORALL;
	private static final Operator EXISTS = BPLQuantifierExpression.Operator.EXISTS;
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

	public static BPLExpression translate(QExpr e) {
		BPLExpression rightExpr = e.getExpr().translateExpr();
		List<VarDef> boundVars = e.getBoundVars();
		return createQExpr(e.getQuantifier(), boundVars, rightExpr);
	}

	/**
	 * creates a quantified expression including the assumptions about the types
	 * of the bound variables
	 * @param quantifier 
	 * @param rightExpr
	 * @param boundVars
	 * @return
	 */
	public static BPLExpression createQExpr(Quantifier quantifier, List<VarDef> boundVars, BPLExpression rightExpr) {
		BPLVariable[] variables = new BPLVariable[boundVars.getNumChild()];
		int i = 0;
		for (VarDef boundVar : boundVars) {
			if (boundVar.attrType() instanceof JavaType) {
				BPLType type = new BPLTypeName(ITranslationConstants.REF_TYPE); //$NON-NLS-1$
				variables[i] = new BPLVariable(boundVar.attrName(), type);
			} else if (boundVar.attrType() instanceof BijectionType) {
				BPLType type = new BPLTypeName(ITranslationConstants.BIJ_TYPE); //$NON-NLS-1$
				variables[i] = new BPLVariable(boundVar.attrName(), type);
			} else {
				throw new Error("Quantification over primitive types not supported yet.");
			}
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
			} else if (boundVar.attrType() instanceof BijectionType) {
				BijectionType bijType = (BijectionType) boundVar.attrType();
				BPLExpression typeAssumtion = makeTypeAssumption(boundVar, bijType);
				if (leftExpr == null) {
					leftExpr = typeAssumtion;
				} else {
					leftExpr = new BPLBinaryLogicalExpression(AND, leftExpr, typeAssumtion);
				}
			}
		}
		
		BPLExpression expr;
		if (leftExpr != null) {
			expr = new BPLBinaryLogicalExpression(quantifier.equals(Quantifier.FORALL) ? BPLBinaryLogicalExpression.Operator.IMPLIES : BPLBinaryLogicalExpression.Operator.AND, leftExpr, rightExpr);
		} else {
			expr = rightExpr;
		}
		return new BPLQuantifierExpression(getQuantifier(quantifier), variables, expr);
	}
	
	private static Operator getQuantifier(Quantifier quantifier) {
		if (quantifier.equals(Quantifier.FORALL)) return FORALL;
		if (quantifier.equals(Quantifier.EXISTS)) return EXISTS;
		throw new Error("Unhandled case." + quantifier); //$NON-NLS-1$
	}

	/**
	 * creates a type assumption.
	 * example: Obj(heap1, o1) && RefOfType(o1, heap1, $cell.Cell) 
	 * @param boundVar
	 * @param bijType
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
	
	private static BPLBinaryLogicalExpression makeTypeAssumption(VarDef boundVar, JavaType javaType, Version version) {
		Phase phase = boundVar.attrCompilationUnit().getPhase();
		return new BPLBinaryLogicalExpression(AND, 
				new BPLFunctionApplication(ITranslationConstants.OBJ_FUNC,  //$NON-NLS-1$
						getHeap(version, phase), 
						new BPLVariableExpression(boundVar.attrName())), 
				new BPLFunctionApplication(ITranslationConstants.REF_OF_TYPE_FUNC,  //$NON-NLS-1$
						new BPLVariableExpression(boundVar.attrName()),
						getHeap(version, phase), 
						new BPLVariableExpression("$" + javaType.getTypeBinding().getQualifiedName()) //$NON-NLS-1$
						));
	}
	
	private static BPLExpression makeTypeAssumption(VarDef boundVar, BijectionType bijType) {
		return new BPLFunctionApplication(ITranslationConstants.BIJECTIVE_FUNC, new BPLVariableExpression(boundVar.attrName()));
	}


	public static BPLExpression getHeap(Version version, Phase phase) {
		if (version == Version.OLD && phase == Phase.POST)
			return new BPLVariableExpression(ITranslationConstants.HEAP1); //$NON-NLS-1$
		if (version == Version.OLD && phase == Phase.PRE)
			return new BPLVariableExpression(ITranslationConstants.OLD_HEAP1); //$NON-NLS-1$	
		if (version == Version.NEW && phase == Phase.POST)
			return new BPLVariableExpression(ITranslationConstants.HEAP2); //$NON-NLS-1$
		if (version == Version.NEW && phase == Phase.PRE)
			return new BPLVariableExpression(ITranslationConstants.OLD_HEAP2); //$NON-NLS-1$
		throw new Error("unhandled case: "+ version); //$NON-NLS-1$
	}

	public static BPLExpression translate(FuncCall e) {
		Def def = e.attrDef();
		if (def instanceof BuiltinFunction) {
			BuiltinFunction f = (BuiltinFunction) def;
			return f.translateCall(e.attrIsInGlobalInvariant(), e.getArguments());
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
					getHeap(leftType.getVersion(), e.attrCompilationUnit().getPhase()), 
					e.getLeft().translateExpr(),
					new BPLVariableExpression("$" + leftType.getTypeBinding().getQualifiedName() + "." + e.getRight().getName()));
			
			if (field.getType().getQualifiedName().equals("boolean")) {
				// boolean fields must be converted explicitly
				expr = new BPLFunctionApplication(ITranslationConstants.INT2BOOL_FUNC, expr);
			}
			return expr;
		}
		throw new Error();
	}

	public static BPLExpression translate(VarAccess e) {
		Def def = e.attrDef();
		if (def instanceof VarDef) {
			return new BPLVariableExpression(e.getName().getName());
		} else if (def instanceof JavaVariableDef) {
			return translateJavaVariableAccess(e, (JavaVariableDef) def);
		} else if (def.attrType() instanceof ExprTypeLocalPlace) {
			ExprTypeLocalPlace localPlace = (ExprTypeLocalPlace) def.attrType();
			return translateLocalPlaceUse(e, localPlace);				
		} else if (def.attrType() instanceof ExprTypePredefinedPlace) {
			ExprTypePredefinedPlace predefinedPlace = (ExprTypePredefinedPlace) def.attrType();
			return translatePredefinedPlaceUse(e, predefinedPlace);
		}
		throw new Error("Cannot translate variable acces to " + def.getClass().getSimpleName() + 
				" with type " + def.attrType());
	}

	private static BPLExpression translatePredefinedPlaceUse(VarAccess e, ExprTypePredefinedPlace predefinedPlace) {
		String placeName = predefinedPlace.getBoogiePlaceName();
		return new BPLVariableExpression(placeName);
	}

	private static BPLExpression translateLocalPlaceUse(VarAccess e, ExprTypeLocalPlace localPlace) {
		return new BPLVariableExpression(e.getName().getName());
	}

	private static BPLExpression translateJavaVariableAccess(VarAccess e,
			JavaVariableDef jv) {
		BPLExpression expr = BuiltinFunctions.stackProperty(
				e.attrIsInGlobalInvariant(), jv.getVersion(), e.attrCompilationUnit().getPhase(),
				jv.getStackPointerExpr().translateExpr(), 
				new BPLVariableExpression(jv.getRegisterName()));
		if (e.attrType().isSubtypeOf(ExprTypeBool.instance())) {
			// boolean vars must be converted explicitly
			expr = new BPLFunctionApplication(ITranslationConstants.INT2BOOL_FUNC, expr);
		}
		return expr;
	}

	public static BPLExpression translate(IntConst e) {
		return new BPLIntLiteral(Integer.parseInt(e.getVal()));
	}

	public static BPLExpression translate(NullConst nullConst) {
		return BPLNullLiteral.NULL;
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

	public static BPLExpression translate(InstanceofOperation op) {
		JavaType jt = (JavaType) op.getRight().attrType();
		return new BPLFunctionApplication(ITranslationConstants.IS_INSTANCE_OF_FUNC, 
				op.getLeft().translateExpr(), 
				getHeap(jt.getVersion(), op.attrCompilationUnit().getPhase()), 
				new BPLVariableExpression("$" + jt.getTypeBinding().getQualifiedName()));
	}


}

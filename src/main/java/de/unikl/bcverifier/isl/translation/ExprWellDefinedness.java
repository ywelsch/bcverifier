package de.unikl.bcverifier.isl.translation;

import java.util.ArrayList;

import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIfThenElseExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLLogicalNotExpression;
import b2bpl.bpl.ast.BPLNullLiteral;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.BoolConst;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.ErrorExpr;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.InstanceofOperation;
import de.unikl.bcverifier.isl.ast.IntConst;
import de.unikl.bcverifier.isl.ast.LineNrProgramPoint;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.QExpr;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.VersionConst;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;

public class ExprWellDefinedness {

	public static BPLExpression translate(NullConst nullConst) {
		return BPLBoolLiteral.TRUE;
	}

	public static BPLExpression translate(BoolConst boolConst) {
		return BPLBoolLiteral.TRUE;
	}

	public static BPLExpression translate(VarAccess varAccess) {
		return BPLBoolLiteral.TRUE;
	}

	public static BPLExpression translate(IntConst intConst) {
		return BPLBoolLiteral.TRUE;
	}
	
	public static BPLExpression translate(UnaryOperation unaryOperation) {
		return unaryOperation.getExpr().translateExprWellDefinedness();
	}

	public static BPLExpression translate(FuncCall funcCall) {
		// all parameters must be well defined:
		ArrayList<BPLExpression> parameterchecks = new ArrayList<BPLExpression>();
		for (Expr p : funcCall.getArgumentList()) {
			parameterchecks.add(p.translateExprWellDefinedness());
		}
		Def funcDef = funcCall.attrDef();
		if (funcDef instanceof BuiltinFunction) {
			BuiltinFunction builtinFunction = (BuiltinFunction) funcDef;
			parameterchecks.add(builtinFunction.translateWelldefinedness(funcCall.attrIsInGlobalInvariant(), funcCall.getArgumentList()));
		}
		return TranslationHelper.conjunction(parameterchecks);
	}

	public static BPLExpression translate(IfThenElse e) {
		// if e then s1 else s2
		// >>> df(e) && if e then df(s1) else df(s2)
		BPLExpression[] exprs = { e.getCond().translateExprWellDefinedness(), new BPLIfThenElseExpression(
					e.getCond().translateExpr(), 
					e.getThenExpr().translateExprWellDefinedness(), 
					e.getElseExpr().translateExprWellDefinedness()) };
		return TranslationHelper.conjunction(exprs);
	}


	public static BPLExpression translate(MemberAccess e) {
		// e.f
		// >>> df(e) && tr(e) != null
		BPLExpression[] exprs = { e.getLeft().translateExprWellDefinedness(), new BPLEqualityExpression(
				BPLEqualityExpression.Operator.NOT_EQUALS, 
				e.getLeft().translateExpr(),
				BPLNullLiteral.NULL
				) };
		return TranslationHelper.conjunction(exprs);
	}

	public static BPLExpression translate(ErrorExpr errorExpr) {
		return BPLBoolLiteral.FALSE;
	}

	public static BPLExpression translate(BinaryOperation e) {
		Expr left = e.getLeft();
		Expr right = e.getRight();
		switch (e.getOperator()) {
		case AND:
		case IMPLIES:
			BPLExpression[] exprs = { left.translateExprWellDefinedness(), new BPLBinaryLogicalExpression(
					BPLBinaryLogicalExpression.Operator.IMPLIES, 
					left.translateExpr(), 
					right.translateExprWellDefinedness()) };
			// left && right
			// left ==> right
			// >>> df(left) && (tr(left) ==> df(right))
			return TranslationHelper.conjunction(exprs);
		case OR:
			BPLExpression[] exprs1 = { left.translateExprWellDefinedness(), new BPLBinaryLogicalExpression(
					BPLBinaryLogicalExpression.Operator.IMPLIES, 
					new BPLLogicalNotExpression(left.translateExpr()), 
					right.translateExprWellDefinedness()) };
			// left || right
			// >>> df(left) && (!tr(left) ==> df(right))
			return TranslationHelper.conjunction(exprs1);
		case DIV:
		case MOD:
			BPLExpression[] exprs2 = { left.translateExprWellDefinedness(), right.translateExprWellDefinedness(), new BPLEqualityExpression(
					BPLEqualityExpression.Operator.NOT_EQUALS, 
					right.translateExpr(),
					new BPLIntLiteral(0)) };
			// left / right
			// left % right
			// >>> df(left) && df(right) && tr(right) != 0
			return TranslationHelper.conjunction(exprs2);
		default:
			BPLExpression[] exprs3 = { left.translateExprWellDefinedness(), right.translateExprWellDefinedness() };
			return TranslationHelper.conjunction(exprs3); 
		}
	}

	public static BPLExpression translate(QExpr e) {
		// forall x. A(x)
		// >>> forall x. wd(A(x))
		return ExprTranslation.createQExpr(
				e.getQuantifier(),
				e.getBoundVarList(), 
				e.getExpr().translateExprWellDefinedness());
	}

	public static BPLExpression translate(LineNrProgramPoint lineNrProgramPoint) {
		throw new Error("Cannot translate program point expressions.");
	}

	public static BPLExpression translate(InstanceofOperation op) {
		return op.getLeft().translateExprWellDefinedness();
	}

	public static BPLExpression translate(VersionConst versionConst) {
		return BPLBoolLiteral.TRUE;
	}
}

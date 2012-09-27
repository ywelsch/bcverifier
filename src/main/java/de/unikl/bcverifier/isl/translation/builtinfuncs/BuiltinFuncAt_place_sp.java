package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeLocalPlace;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
/**
 * bool at(Place p, int stackPointer) 
 *
 * returns true if the current place at the given stackpointer is p
 * in the current interaction frame
 */
final class BuiltinFuncAt_place_sp extends BuiltinFunction {
	BuiltinFuncAt_place_sp() {
		super("at", 
				ExprTypeBool.instance(),
				new ExprType[] { ExprTypePlace.instance(), ExprTypeInt.instance() });
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr stackPointer = arguments.getChild(1);
		BPLArrayExpression currentStackpointer = BuiltinFunctions.getCurrentSp(isGlobalInvariant, ((ExprTypeLocalPlace)p.attrType()).getVersion());
		return ExprWellDefinedness.conjunction(
					new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
							new BPLIntLiteral(0), stackPointer.translateExpr()),
					new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
							stackPointer.translateExpr(), 
							currentStackpointer)
					 
				);
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr stackPointer = arguments.getChild(1);
		ExprTypeLocalPlace placeType = (ExprTypeLocalPlace) p.attrType();
		// stack1[ip1][stackPointer][place] == p
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
				BuiltinFunctions.stackProperty(isGlobalInvariant, placeType.getVersion(), stackPointer.translateExpr(), new BPLVariableExpression("place"))
				, p.translateExpr()
				);
	}
}
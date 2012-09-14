package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
/**
 * bool at(Place p) 
 *
 * returns true if the current place at the current stackpointer is p
 * in the current interaction frame
 */
final class BuiltinFuncAt_place extends BuiltinFunction {
	BuiltinFuncAt_place() {
		super("at", 
				ExprTypeBool.instance(),
				new ExprType[] { PlaceType.instance() });
	}

	@Override
	public BPLExpression translateWelldefinedness(List<Expr> arguments) {
		return BPLBoolLiteral.TRUE; 
	}

	@Override
	public BPLExpression translateCall(List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		PlaceType placeType = (PlaceType) p.attrType();
		BPLExpression stackPointer;
		if (placeType.getVersion() == Version.OLD) {
			stackPointer = BuiltinFunctions.FUNC_SP1.translateCall(new List<Expr>());
		} else {
			stackPointer = BuiltinFunctions.FUNC_SP2.translateCall(new List<Expr>());
		}
		// stack1[ip1][stackPointer][place] == p
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
				BuiltinFunctions.stackProperty(placeType.getVersion(), stackPointer, new BPLVariableExpression("place"))
				, p.translateExpr()
				);
	}
}
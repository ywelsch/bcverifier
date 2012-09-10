package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
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
 * bool at(Place p, int stackPointer) 
 *
 * returns true if the current place at the given stackpointer is p
 * in the current interaction frame
 */
final class BuiltinFuncAt extends BuiltinFunction {
	BuiltinFuncAt() {
		super("at", 
				ExprTypeBool.instance(),
				new ExprType[] { PlaceType.instance(), ExprTypeInt.instance() });
	}

	@Override
	public BPLExpression translateWelldefinedness(List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr stackPointer = arguments.getChild(1);
		BPLArrayExpression currentStackpointer;
		if (((PlaceType)p.attrType()).getVersion() == Version.OLD) {
			currentStackpointer= new BPLArrayExpression(new BPLVariableExpression("spmap1"), new BPLVariableExpression("ip1"));
		} else {
			currentStackpointer= new BPLArrayExpression(new BPLVariableExpression("spmap2"), new BPLVariableExpression("ip2"));
		}
		return ExprWellDefinedness.conjunction(
					new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
							new BPLIntLiteral(0), stackPointer.translateExpr()),
					new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
							stackPointer.translateExpr(), 
							currentStackpointer)
					 
				);
	}

	@Override
	public BPLExpression translateCall(List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr stackPointer = arguments.getChild(1);
		PlaceType placeType = (PlaceType) p.attrType();
		// stack1[libip(ip1)][stackPointer][place] == p
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
				BuiltinFunctions.stackProperty(placeType.getVersion(), stackPointer.translateExpr(), new BPLVariableExpression("place"))
				, p.translateExpr()
				);
	}
}
package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
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
				new ExprType[] { ExprTypePlace.instance() });
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		return BPLBoolLiteral.TRUE; 
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		BPLExpression stackPointer;
		if (placeType.getVersion() == Version.OLD) {
			stackPointer = BuiltinFunctions.FUNC_SP1.translateCall(isGlobalInvariant, new List<Expr>());
		} else {
			stackPointer = BuiltinFunctions.FUNC_SP2.translateCall(isGlobalInvariant, new List<Expr>());
		}
		// stack1[ip1][stackPointer][place] == p
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
				BuiltinFunctions.stackProperty(isGlobalInvariant, placeType.getVersion(), p.attrCompilationUnit().getPhase(), stackPointer, new BPLVariableExpression("place"))
				, p.translateExpr()
				);
	}
	
	@Override
	public ExprType exactType(FuncCall call) {
		/*if (call.attrIsInLocalPlaceDef()) {
			call.addError("Function 'at' must not be used in local place definitions.");
		}*/
		return super.exactType(call);
	}
}
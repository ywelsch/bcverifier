package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;

/**
 * int sp1() 
 * 
 * returns the current value of the stackpointer for the old library in the topmost interaction frame
 */
final class BuiltinFuncSp1 extends BuiltinFunction {

	public BuiltinFuncSp1() {
		super("sp1", ExprTypeInt.instance(), new ExprType[] {});
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		String ip = "ip1";
		if (isInGlobalInv) {
			ip = "iframe";
		}
		return new BPLArrayExpression(
				new BPLVariableExpression("spmap1"), 
				new BPLVariableExpression(ip)) ;
	}
}
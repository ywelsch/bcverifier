package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.translation.Translation;

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
		String ip = ITranslationConstants.IP1_VAR;
		if (isInGlobalInv) {
			ip = Translation.IFRAME_VAR;
		}
		return new BPLArrayExpression(
				new BPLVariableExpression(ITranslationConstants.SP_MAP1_VAR), 
				new BPLVariableExpression(ip)) ;
	}
}
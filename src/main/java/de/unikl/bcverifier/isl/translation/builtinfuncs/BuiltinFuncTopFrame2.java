package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;

/**
 * int topFrame(version v, int slice) 
 * 
 * returns the current value of the stackpointer for the library identified by the parameter v
 *  in the given interaction frame
 */
final class BuiltinFuncTopFrame2 extends BuiltinFunction {

	public BuiltinFuncTopFrame2() {
		super("topFrame", ExprTypeInt.instance(), ExprTypeVersion.instance(), ExprTypeInt.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		ExprTypeVersion t = (ExprTypeVersion) arguments.getChild(0).attrType();
		Version v = t.getVersion();
		BPLExpression slice = arguments.getChild(1).translateExpr();
		return new BPLArrayExpression(
				BuiltinFuncTopFrame1.getSpVar(v), 
				slice) ;
	}


}
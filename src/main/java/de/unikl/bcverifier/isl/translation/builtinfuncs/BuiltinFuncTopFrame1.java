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
 * int topFrame(version v) 
 * 
 * returns the current value of the stackpointer for the library identified by the parameter v
 *  in the topmost interaction frame
 */
final class BuiltinFuncTopFrame1 extends BuiltinFunction {

	public BuiltinFuncTopFrame1() {
		super("topFrame", ExprTypeInt.instance(), ExprTypeVersion.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		ExprTypeVersion t = (ExprTypeVersion) arguments.getChild(0).attrType();
		Version v = t.getVersion();
		
		return new BPLArrayExpression(
				getSpVar(v), 
				BuiltinFuncTopSlice2.getIpVar(v)) ;
	}

	public static BPLExpression getSpVar(Version v) {
		if (v == Version.NEW) {
			return new BPLVariableExpression(ITranslationConstants.SP_MAP2_VAR);
		} else if (v == Version.OLD) {
			return new BPLVariableExpression(ITranslationConstants.SP_MAP1_VAR);
		}
		throw new Error();
	}

}
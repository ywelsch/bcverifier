package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;

public class BuiltinFuncTopSlice2  extends BuiltinFunction {

	public BuiltinFuncTopSlice2() {
		super("topSlice", ExprTypeInt.instance(), ExprTypeVersion.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		ExprTypeVersion vers = (ExprTypeVersion) arguments.getChild(0).attrType();
		return getIpVar(vers.getVersion());
	}

	public static BPLExpression getIpVar(Version v) {
		switch (v) {
		case NEW:
			return new BPLVariableExpression(ITranslationConstants.IP2_VAR);
		case OLD:
			return new BPLVariableExpression(ITranslationConstants.IP1_VAR);
		case BOTH:
		default:
			throw new Error();
		}
	}

}

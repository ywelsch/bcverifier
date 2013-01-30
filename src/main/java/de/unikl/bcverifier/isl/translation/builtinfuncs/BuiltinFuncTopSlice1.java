package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;

public class BuiltinFuncTopSlice1  extends BuiltinFunction {

	public BuiltinFuncTopSlice1() {
		super("topSlice", ExprTypeInt.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		return BuiltinFuncTopSlice2.getIpVar(Version.OLD);
	}
}

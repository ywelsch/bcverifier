package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.BijectionType;
import de.unikl.bcverifier.isl.checking.types.ExprType;

public class BuiltinFuncEmpty extends BuiltinFunction {

	public BuiltinFuncEmpty() {
		super("empty", BijectionType.instance(), new ExprType[]{});
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant,
			List<Expr> arguments) {
		// TODO Auto-generated method stub
		return new BPLFunctionApplication("emptyBij()");
	}

}

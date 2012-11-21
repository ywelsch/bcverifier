package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;

public class BuiltinFuncEmpty extends BuiltinFunction {

	public BuiltinFuncEmpty() {
		super("empty", BinRelationType.instance(), new ExprType[]{});
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant,
			List<Expr> arguments) {
		return new BPLFunctionApplication(ITranslationConstants.EMPTY_REL_FUNC);
	}

}

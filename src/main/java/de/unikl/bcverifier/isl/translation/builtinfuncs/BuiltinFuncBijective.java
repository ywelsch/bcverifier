package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;

public class BuiltinFuncBijective extends BuiltinFunction {
	public BuiltinFuncBijective() {
		super("bijective", ExprTypeBool.instance(),
				new ExprType[] { BinRelationType.instance() });
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant,
			List<Expr> arguments) {
		return new BPLFunctionApplication(ITranslationConstants.BIJECTIVE_FUNC, arguments.getChild(0).translateExpr());
	}
}
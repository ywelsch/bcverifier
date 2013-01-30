package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAny;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
import de.unikl.bcverifier.isl.translation.TranslationHelper;


/**
 * T stack(Place p, int stackPointer, T expr)
 * 
 * evaluates the expression expr in the context of the given place and stackpointer
 * (i.e. local variables visible at p can be used in expr)
 */
final class BuiltinFuncEval3 extends BuiltinFunction {
	private final BuiltinFunctions builtinFunctions;


	public BuiltinFuncEval3(BuiltinFunctions builtinFunctions) {
		super(BuiltinFuncEval1.name, ExprTypeAny.instance(), new ExprType[] { ExprTypePlace.instance(),
			ExprTypeInt.instance(), ExprTypeInt.instance(), ExprTypeAny.instance() });
		this.builtinFunctions = builtinFunctions;
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		BPLExpression[] exprs = { this.builtinFunctions.FUNC_AT3.translateWelldefinedness(!placeType.isLocalPlace(), arguments), this.builtinFunctions.FUNC_AT3.translateCall(!placeType.isLocalPlace(), arguments) };
		return TranslationHelper.conjunction(exprs);
	}

	@Override
	public ExprType exactType(FuncCall call) {
		ExprTypePlace place = (ExprTypePlace) call.getArgument(0).attrType();
		if (!place.isCallPlace()) {
			call.addError("Function 'eval' with integer argument can only be used with places that are defined with the call statement.");
		}
		return call.getArgument(3).attrType();
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr exp = arguments.getChild(3);
		return exp.translateExpr();
	}
}
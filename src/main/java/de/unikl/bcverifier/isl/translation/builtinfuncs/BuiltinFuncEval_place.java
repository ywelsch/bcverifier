package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAny;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
import de.unikl.bcverifier.isl.translation.TranslationHelper;


/**
 * T stack(Place p, T expr)
 * 
 * evaluates the expression expr in the context of the given place and the current stackpointer
 * (i.e. local variables visible at p can be used in expr)
 */
final public class BuiltinFuncEval_place extends BuiltinFunction {
	public static final String name = "eval";
	private final BuiltinFunctions builtinFunctions;


	public BuiltinFuncEval_place(BuiltinFunctions builtinFunctions) {
		super(name, ExprTypeAny.instance(), new ExprType[] { ExprTypePlace.instance(),
			ExprTypeAny.instance() });
		this.builtinFunctions = builtinFunctions;
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		BPLExpression[] exprs = { this.builtinFunctions.FUNC_AT_place.translateWelldefinedness(isGlobalInvariant, arguments), this.builtinFunctions.FUNC_AT_place.translateCall(isGlobalInvariant, arguments) };
		return TranslationHelper.conjunction(exprs);
	}

	@Override
	public ExprType exactType(FuncCall call) {
		/*if (call.attrIsInLocalPlaceDef()) {
			call.addError("Function 'stack' must not be used in local place definitions.");
		}*/
		return call.getArgument(1).attrType();
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr exp = arguments.getChild(1);
		return exp.translateExpr();
	}
}
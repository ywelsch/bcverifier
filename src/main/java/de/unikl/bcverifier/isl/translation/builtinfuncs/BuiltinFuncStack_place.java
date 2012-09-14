package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAny;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;


/**
 * T stack(Place p, T expr)
 * 
 * evaluates the expression expr in the context of the given place and the current stackpointer
 * (i.e. local variables visible at p can be used in expr)
 */
final class BuiltinFuncStack_place extends BuiltinFunction {
	private final BuiltinFunctions builtinFunctions;


	public BuiltinFuncStack_place(BuiltinFunctions builtinFunctions) {
		super("stack", ExprTypeAny.instance(), new ExprType[] { PlaceType.instance(),
			ExprTypeAny.instance() });
		this.builtinFunctions = builtinFunctions;
	}

	@Override
	public BPLExpression translateWelldefinedness(List<Expr> arguments) {
		return ExprWellDefinedness.conjunction(
				this.builtinFunctions.FUNC_AT_place.translateWelldefinedness(arguments),
				this.builtinFunctions.FUNC_AT_place.translateCall(arguments));
	}

	@Override
	public ExprType exactType(List<Expr> arguments) {
		return arguments.getChild(1).attrType();
	}

	@Override
	public BPLExpression translateCall(List<Expr> arguments) {
		Expr exp = arguments.getChild(1);
		return exp.translateExpr();
	}
}
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
 * T stack(Place p, int stackPointer, T expr)
 * 
 * evaluates the expression expr in the context of the given place and stackpointer
 * (i.e. local variables visible at p can be used in expr)
 */
final class BuiltinFuncStack extends BuiltinFunction {
	private final BuiltinFunctions builtinFunctions;


	public BuiltinFuncStack(BuiltinFunctions builtinFunctions) {
		super("stack", ExprTypeAny.instance(), new ExprType[] { PlaceType.instance(),
			ExprTypeInt.instance(), ExprTypeAny.instance() });
		this.builtinFunctions = builtinFunctions;
	}

	@Override
	public BPLExpression translateWelldefinedness(List<Expr> arguments) {
		return ExprWellDefinedness.conjunction(
				this.builtinFunctions.funcs.get("at").translateWelldefinedness(arguments),
				this.builtinFunctions.funcs.get("at").translateCall(arguments));
	}

	@Override
	public ExprType exactType(List<Expr> arguments) {
		return arguments.getChild(2).attrType();
	}

	@Override
	public BPLExpression translateCall(List<Expr> arguments) {
		Expr exp = arguments.getChild(2);
		return exp.translateExpr();
	}
}
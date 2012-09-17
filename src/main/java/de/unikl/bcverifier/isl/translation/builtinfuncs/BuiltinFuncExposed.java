package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

/**
 * bool exposed(Object o) 
 * 
 * returns true iff the library could have a reference to o
 * 
 */
final class BuiltinFuncExposed extends BuiltinFunction {

	public BuiltinFuncExposed(TwoLibraryModel twoLibraryModel) {
		super("exposed", ExprTypeBool.instance(), new ExprType[] { JavaType.object(twoLibraryModel) });
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		return BuiltinFunctions.heapProperty(arguments.getChild(0), "exposed");
	}
}
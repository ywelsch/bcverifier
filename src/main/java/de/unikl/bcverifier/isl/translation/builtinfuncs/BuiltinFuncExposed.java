package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLNullLiteral;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

/**
 * bool exposed(Object o) 
 * 
 * returns true iff the library could have a reference to o
 * 
 */
final class BuiltinFuncExposed extends BuiltinFunction {

	public BuiltinFuncExposed(TwoLibraryModel twoLibraryModel) {
		super("exposed", ExprTypeBool.instance(), new ExprType[] { ExprTypeJavaType.object(twoLibraryModel) });
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		return BuiltinFunctions.heapProperty(arguments.getChild(0), ITranslationConstants.EXPOSED_FIELD);
	}
	
	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr arg = arguments.getChild(0);
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.NOT_EQUALS, 
				arg.translateExpr(), BPLNullLiteral.NULL);
	}
}
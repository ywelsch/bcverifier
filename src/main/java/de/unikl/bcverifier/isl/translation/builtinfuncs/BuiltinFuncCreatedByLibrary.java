package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.IBPLVisitor;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLLiteral;
import b2bpl.bpl.ast.BPLLogicalNotExpression;
import b2bpl.bpl.ast.BPLNullLiteral;
import b2bpl.bpl.ast.BPLUnaryExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

/**
 * bool createdByCtxt(Object o) 
 * 
 * returns true if o was created by the context
 * returns false if o was created by the library
 */
final class BuiltinFuncCreatedByLibrary extends BuiltinFunction {

	BuiltinFuncCreatedByLibrary(TwoLibraryModel twoLibraryModel) {
		super("createdByLibrary", ExprTypeBool.instance(),
				new ExprType[] { JavaType.object(twoLibraryModel) });
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		return new BPLLogicalNotExpression(
				BuiltinFunctions.heapProperty(arguments.getChild(0),
				ITranslationConstants.CREATED_BY_CTXT_FIELD));
	}
	
	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr arg = arguments.getChild(0);
		return new BPLEqualityExpression(BPLEqualityExpression.Operator.NOT_EQUALS, 
				arg.translateExpr(), BPLNullLiteral.NULL);
	}
}
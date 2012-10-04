package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.BijectionType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

final class BuiltinFuncRelated extends BuiltinFunction {

	public BuiltinFuncRelated(TwoLibraryModel twoLibraryModel) {
		super("related", ExprTypeBool.instance(), new ExprType[] { BijectionType.instance(), JavaType.object(twoLibraryModel), JavaType.object(twoLibraryModel) });
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		return new BPLArrayExpression(arguments.getChild(0).translateExpr(), arguments.getChild(1).translateExpr(), arguments.getChild(2).translateExpr());
	}
	
	@Override
	public ExprType exactType(FuncCall call) {
		ExprType t1 = call.getArgument(1).attrType();
		ExprType t2 = call.getArgument(2).attrType();
		if ((t1 instanceof JavaType) && !((JavaType)t1).isOld()) {
			call.addError("Second parameter to function 'related' must be of a type of the old implementation.");
		}
		if ((t2 instanceof JavaType) && !((JavaType)t2).isNew()) {
			call.addError("Third parameter to function 'related' must be of a type of the old implementation.");
		}
		return super.exactType(call);
	}
}
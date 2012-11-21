package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayAssignment;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

final class BuiltinFuncTertiaryBij extends BuiltinFunction {
	public enum Name {
		RELATED,
		ADD,
		REMOVE
	}

	public BuiltinFuncTertiaryBij(Name name, TwoLibraryModel twoLibraryModel) {
		super(name.toString().toLowerCase(), name.equals(Name.RELATED) ? ExprTypeBool.instance() : BinRelationType.instance(), new ExprType[] { BinRelationType.instance(), JavaType.object(twoLibraryModel), JavaType.object(twoLibraryModel) });
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		if (name.equals(Name.RELATED.toString().toLowerCase()))
			return new BPLArrayExpression(arguments.getChild(0).translateExpr(), arguments.getChild(1).translateExpr(), arguments.getChild(2).translateExpr());
		if (name.equals(Name.ADD.toString().toLowerCase()) || name.equals(Name.REMOVE.toString().toLowerCase()))
			return new BPLArrayExpression(arguments.getChild(0).translateExpr(), new BPLArrayAssignment(new BPLExpression[]{arguments.getChild(1).translateExpr(), arguments.getChild(2).translateExpr()}, name.equals(Name.ADD.toString().toLowerCase()) ? BPLBoolLiteral.TRUE : BPLBoolLiteral.FALSE));
		throw new RuntimeException("Case not covered");
	}
	
	@Override
	public ExprType exactType(FuncCall call) {
		ExprType t1 = call.getArgument(1).attrType();
		ExprType t2 = call.getArgument(2).attrType();
		if ((t1 instanceof JavaType) && !((JavaType)t1).isOld()) {
			call.addError("Second parameter to function " + name + " must be of a type of the old implementation.");
		}
		if ((t2 instanceof JavaType) && !((JavaType)t2).isNew()) {
			call.addError("Third parameter to function " + name + " must be of a type of the old implementation.");
		}
		return super.exactType(call);
	}
}
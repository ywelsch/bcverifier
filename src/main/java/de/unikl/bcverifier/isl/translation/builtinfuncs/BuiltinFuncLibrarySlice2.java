package de.unikl.bcverifier.isl.translation.builtinfuncs;

import com.google.common.collect.Lists;

import b2bpl.bpl.ast.BPLBinaryArithmeticExpression;
import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLPartialOrderExpression;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;
import de.unikl.bcverifier.isl.translation.TranslationHelper;

public class BuiltinFuncLibrarySlice2  extends BuiltinFunction {

	public BuiltinFuncLibrarySlice2() {
		super("librarySlice", ExprTypeBool.instance(), ExprTypeVersion.instance(), ExprTypeInt.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		// arg1 mod 2 == 1 && arg1 > 0 && arg1 <= ip1
		BPLExpression arg0 = arguments.getChild(1).translateExpr();
		ExprTypeVersion vers = (ExprTypeVersion) arguments.getChild(0).attrType();
		return isLibrarySlice(vers.getVersion(), arg0);
	}

	public static BPLExpression isLibrarySlice(Version version, BPLExpression slice) {
		return TranslationHelper.conjunction(
			// arg0 mod 2 == 1
			new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS,
				new BPLBinaryArithmeticExpression(
					BPLBinaryArithmeticExpression.Operator.REMAINDER_INT, 
					slice, 
					new BPLIntLiteral(2)
					),
					new BPLIntLiteral(1)), 
			// arg0 > 0
			new BPLRelationalExpression(
					BPLRelationalExpression.Operator.GREATER, 
					slice, 
					new BPLIntLiteral(0)),
			// arg0 <= ip
					new BPLRelationalExpression(
							BPLRelationalExpression.Operator.LESS_EQUAL, 
							slice, 
							BuiltinFuncTopSlice2.getIpVar(version))			
		);
	}

}

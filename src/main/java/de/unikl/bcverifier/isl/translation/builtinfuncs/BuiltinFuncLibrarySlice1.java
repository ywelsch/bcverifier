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

public class BuiltinFuncLibrarySlice1  extends BuiltinFunction {

	public BuiltinFuncLibrarySlice1() {
		super("librarySlice", ExprTypeBool.instance(), ExprTypeInt.instance());
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		// arg1 mod 2 == 1 && arg1 > 0 && arg1 <= ip1
		BPLExpression arg0 = arguments.getChild(0).translateExpr();
		return BuiltinFuncLibrarySlice2.isLibrarySlice(Version.OLD, arg0);
	}


}

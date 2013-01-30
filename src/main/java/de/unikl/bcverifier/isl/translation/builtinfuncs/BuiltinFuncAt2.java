package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.ExprWellDefinedness;
import de.unikl.bcverifier.isl.translation.Phase;
import de.unikl.bcverifier.isl.translation.TranslationHelper;
/**
 * bool at(Place p, int slice, int frame) 
 *
 * returns true if the current place at the given stackpointer is p
 * in the current interaction frame
 */
final class BuiltinFuncAt2 extends BuiltinFunction {
	BuiltinFuncAt2() {
		super("at", 
				ExprTypeBool.instance(),
				ExprTypePlace.instance(), 
				ExprTypeInt.instance());
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant,
			List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr slice = arguments.getChild(1);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trSlice = slice.translateExpr();
		BPLExpression trFrame = BuiltinFunctions.getCurrentSp(version, phase, trSlice);
		return BuiltinFuncAt3.translateAtWelldefinedness(version, phase, trPlace, trSlice, trFrame);
	}
	
	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr slice = arguments.getChild(1);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trSlice = slice.translateExpr();
		BPLExpression trFrame = BuiltinFunctions.getCurrentSp(version, phase, trSlice);
		return BuiltinFuncAt3.translateAt(version, phase, trPlace, trSlice, trFrame);
	}

	
	@Override
	public ExprType exactType(FuncCall call) {
		ExprTypePlace place = (ExprTypePlace) call.getArgument(0).attrType();
		if (!place.isCallPlace()) {
			call.addError("Function 'at' with integer argument can only be used with places that are defined using the call statement.");
		}
		return super.exactType(call);
	}
}
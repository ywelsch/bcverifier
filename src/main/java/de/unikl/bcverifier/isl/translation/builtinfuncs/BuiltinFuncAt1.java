package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.VersionConst;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.Phase;
/**
 * bool at(Place p) 
 *
 * returns true if the current place at the current stackpointer is p
 * in the current interaction frame
 */
final class BuiltinFuncAt1 extends BuiltinFunction {
	BuiltinFuncAt1() {
		super("at", 
				ExprTypeBool.instance(),
				ExprTypePlace.instance());
	}

	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant,
			List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trSlice = BuiltinFuncTopSlice2.getIpVar(version);
		BPLExpression trFrame = BuiltinFunctions.getCurrentSp(version, phase, trSlice);
		return BuiltinFuncAt3.translateAtWelldefinedness(version, phase, trPlace, trSlice, trFrame);
	}
	
	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trSlice = BuiltinFuncTopSlice2.getIpVar(version);
		BPLExpression trFrame = BuiltinFunctions.getCurrentSp(version, phase, trSlice);
		return BuiltinFuncAt3.translateAt(version, phase, trPlace, trSlice, trFrame);
	}
	
	@Override
	public ExprType exactType(FuncCall call) {
		return super.exactType(call);
	}
}
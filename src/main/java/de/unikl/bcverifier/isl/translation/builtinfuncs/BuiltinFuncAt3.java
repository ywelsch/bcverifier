package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
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
final class BuiltinFuncAt3 extends BuiltinFunction {
	BuiltinFuncAt3() {
		super("at", 
				ExprTypeBool.instance(),
				ExprTypePlace.instance(), 
				ExprTypeInt.instance(),
				ExprTypeInt.instance());
	}
	
	@Override
	public BPLExpression translateWelldefinedness(boolean isGlobalInvariant,
			List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr slice = arguments.getChild(1);
		Expr frame = arguments.getChild(2);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trFrame = frame.translateExpr();
		BPLExpression trSlice = slice.translateExpr();
		return translateAtWelldefinedness(version, phase, trPlace, trSlice, trFrame);
	}

	@Override
	public BPLExpression translateCall(boolean isGlobalInvariant, List<Expr> arguments) {
		Expr p = arguments.getChild(0);
		Expr slice = arguments.getChild(1);
		Expr frame = arguments.getChild(2);
		ExprTypePlace placeType = (ExprTypePlace) p.attrType();
		Version version = placeType.getVersion();
		Phase phase = p.attrCompilationUnit().getPhase();
		BPLExpression trPlace = p.translateExpr();
		BPLExpression trFrame = frame.translateExpr();
		BPLExpression trSlice = slice.translateExpr();
		return translateAt(version, phase, trPlace, trSlice, trFrame);
	}

	public static BPLExpression translateAt(Version version, Phase phase,
			BPLExpression trPlace, BPLExpression trSlice, BPLExpression trFrame) {
			return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
				BuiltinFunctions.stackProperty(version, 
						phase, 
						trSlice,
						trFrame, 
						new BPLVariableExpression(ITranslationConstants.PLACE_VARIABLE))
				, trPlace);
	}
	
	@Override
	public ExprType exactType(FuncCall call) {
		ExprTypePlace place = (ExprTypePlace) call.getArgument(0).attrType();
		if (!place.isCallPlace()) {
			call.addError("Function 'at' with integer argument can only be used with places that are defined using the call statement.");
		}
		return super.exactType(call);
	}

	public static BPLExpression translateAtWelldefinedness(Version version,
			Phase phase, BPLExpression trPlace, BPLExpression trSlice,
			BPLExpression trFrame) {
		return TranslationHelper.conjunction(
				// isLibrarySlice(version, slice)
				BuiltinFuncLibrarySlice2.isLibrarySlice(version, trSlice),
				// && isValidFrame(version, slice, frame)
				BuiltinFunctions.isValidFrame(version, phase, trSlice, trFrame)
				);
	}
}
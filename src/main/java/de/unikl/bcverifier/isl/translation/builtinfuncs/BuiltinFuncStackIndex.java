package de.unikl.bcverifier.isl.translation.builtinfuncs;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;
import de.unikl.bcverifier.isl.translation.Translation;

/**
 * int stackIndex(version v) 
 * 
 * returns the current value of the stackpointer for the library identified by the parameter v
 *  in the topmost interaction frame
 */
final class BuiltinFuncStackIndex extends BuiltinFunction {

	public BuiltinFuncStackIndex() {
		super("stackIndex", ExprTypeInt.instance(), new ExprType[] {ExprTypeVersion.instance()});
	}

	@Override
	public BPLExpression translateCall(boolean isInGlobalInv, List<Expr> arguments) {
		ExprTypeVersion t = (ExprTypeVersion) arguments.getChild(0).attrType();
		Version v = t.getVersion();
		String ip = getIframeVar(isInGlobalInv, v);
		
		return new BPLArrayExpression(
				new BPLVariableExpression(getSpVar(v)), 
				new BPLVariableExpression(ip)) ;
	}

	private String getSpVar(Version v) {
		if (v == Version.NEW) {
			return ITranslationConstants.SP_MAP2_VAR;
		} else if (v == Version.OLD) {
			return ITranslationConstants.SP_MAP1_VAR;
		}
		throw new Error();
	}

	private String getIframeVar(boolean isInGlobalInv, Version v) {
		if (isInGlobalInv) {
			return Translation.IFRAME_VAR;
		} else if (v == Version.OLD) {
			return ITranslationConstants.IP1_VAR;
		} else if (v == Version.NEW) {
			return ITranslationConstants.IP2_VAR;
		}
		throw new Error();
	}
}
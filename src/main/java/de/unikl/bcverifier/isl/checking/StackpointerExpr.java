package de.unikl.bcverifier.isl.checking;

import java.io.IOException;
import java.util.Collections;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;

public class StackpointerExpr extends Expr {

	private Version version;

	public StackpointerExpr(Version version) {
		this.version = version;
	}

	@Override
	public Void printTo(Appendable r) {
		try {
			r.append("sp " + version);
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public ExprType attrType() {
		return ExprTypeInt.instance();
	}

	@Override
	public BPLExpression translateExpr() {
		if (version == Version.OLD) {
			return BuiltinFunctions.FUNC_SP1.translateCall(new List<Expr>());
		} else {
			return BuiltinFunctions.FUNC_SP2.translateCall(new List<Expr>());
		}
	}

	@Override
	public BPLExpression translateExprWellDefinedness() {
		if (version == Version.OLD) {
			return BuiltinFunctions.FUNC_SP1.translateWelldefinedness(new List<Expr>());
		} else {
			return BuiltinFunctions.FUNC_SP2.translateWelldefinedness(new List<Expr>());
		}
	}

}

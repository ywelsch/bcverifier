package de.unikl.bcverifier.isl.checking;

import java.io.IOException;

import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.VersionConst;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunctions;

public class StackpointerExpr extends Expr {

	private Version version;
	private boolean isInGlobalInv;

	public StackpointerExpr(Version version, boolean isInGlobalInv) {
		this.version = version;
		this.isInGlobalInv = isInGlobalInv;
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
		return BuiltinFunctions.FUNC_STACKINDEX.translateCall(attrIsInGlobalInvariant(), new List<Expr>().add(new VersionConst(version)));
	}

	@Override
	public BPLExpression translateExprWellDefinedness() {
		return BuiltinFunctions.FUNC_STACKINDEX.translateWelldefinedness(attrIsInGlobalInvariant(), new List<Expr>().add(new VersionConst(version)));
	}

	
	@Override
	public boolean attrIsInGlobalInvariant() {
		return isInGlobalInv;
	}
	
}

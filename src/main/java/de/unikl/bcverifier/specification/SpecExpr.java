package de.unikl.bcverifier.specification;

import de.unikl.bcverifier.isl.ast.Expr;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;

public class SpecExpr {
	private final BPLExpression expr;
	private final BPLExpression welldefinednessExpr;
	private final String comment;
	
	
	
	public SpecExpr(BPLExpression expr, BPLExpression welldefinednessExpr) {
		this(expr, welldefinednessExpr, "");
	}
	
	public SpecExpr(BPLExpression invExpr, BPLExpression welldefinednessExpr, String comment) {
		this.expr = invExpr;
		this.welldefinednessExpr = welldefinednessExpr;
		this.comment = comment;
	}
	
	public static SpecExpr withString(String expr) {
		if (expr == null) {
			return null;
		}
		return new SpecExpr(new BPLVariableExpression(expr), BPLBoolLiteral.TRUE);
	}
	
	public BPLExpression getExpr() {
		return expr;
	}
	public BPLExpression getWelldefinednessExpr() {
		return welldefinednessExpr;
	}
	
	public String getComment() {
		return comment;
	}

	public static SpecExpr fromExpr(Expr e) {
		return new SpecExpr(e.translateExpr(), e.translateExprWellDefinedness(), e.print());
	}
	
	
}

package de.unikl.bcverifier.specification;

import b2bpl.bpl.ast.BPLExpression;

public class SpecInvariant {
	private final BPLExpression invExpr;
	private final BPLExpression welldefinednessExpr;
	private final String comment;
	
	public SpecInvariant(BPLExpression invExpr, BPLExpression welldefinednessExpr) {
		this(invExpr, welldefinednessExpr, "");
	}
	
	public SpecInvariant(BPLExpression invExpr, BPLExpression welldefinednessExpr, String comment) {
		this.invExpr = invExpr;
		this.welldefinednessExpr = welldefinednessExpr;
		this.comment = comment;
	}
	
	public BPLExpression getInvExpr() {
		return invExpr;
	}
	public BPLExpression getWelldefinednessExpr() {
		return welldefinednessExpr;
	}
	
	public String getComment() {
		return comment;
	}
	
	
}

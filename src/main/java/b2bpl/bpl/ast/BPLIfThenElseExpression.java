package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLIfThenElseExpression extends BPLExpression {

	private final BPLExpression condition;
	private final BPLExpression thenExpr;
	private final 	BPLExpression elseExpr;

	



	public BPLIfThenElseExpression(
			BPLExpression condition,
			BPLExpression thenExpr,
			BPLExpression elseExpr) {
		super(Precedence.LOWEST);
		this.condition = condition;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
	}

	public BPLExpression getCondition() {
		return condition;
	}

	public BPLExpression getThenExpr() {
		return thenExpr;
	}

	public BPLExpression getElseExpr() {
		return elseExpr;
	}

	public <R> R accept(IBPLVisitor<R> visitor) {
		return visitor.visitIfThenElseExpression(this);
	}

	public String toString() {
		return  "if " + condition + " then " + thenExpr + " else " + elseExpr;
	}
}

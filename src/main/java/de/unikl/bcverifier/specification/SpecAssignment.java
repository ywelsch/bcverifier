package de.unikl.bcverifier.specification;

import b2bpl.bpl.ast.BPLAssignmentCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLExpression;

public class SpecAssignment {
	private final BPLExpression leftexpr;
	private final BPLExpression rightexpr;
	private final BPLExpression welldefinednessExpr;
	private final String comment;
	
	
	
	public SpecAssignment(BPLExpression leftExpr, BPLExpression rightExpr, BPLExpression welldefinednessExpr) {
		this(leftExpr, rightExpr, welldefinednessExpr, "");
	}
	
	public SpecAssignment(BPLExpression leftExpr, BPLExpression rightExpr, BPLExpression welldefinednessExpr, String comment) {
		this.leftexpr = leftExpr;
		this.rightexpr = rightExpr;
		this.welldefinednessExpr = welldefinednessExpr;
		this.comment = comment;
	}
	

	public BPLExpression getWelldefinednessExpr() {
		return welldefinednessExpr;
	}
	
	public String getComment() {
		return comment;
	}

	public BPLExpression getRightexpr() {
		return rightexpr;
	}

	public BPLExpression getLeftexpr() {
		return leftexpr;
	}

	public BPLCommand getAssignCommand() {
		return new BPLAssignmentCommand(leftexpr, rightexpr);
	}
	
	
}

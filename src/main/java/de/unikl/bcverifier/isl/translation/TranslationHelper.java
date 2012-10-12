package de.unikl.bcverifier.isl.translation;

import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLExpression;

public class TranslationHelper {
	
	public static BPLExpression conjunction(java.util.Collection<BPLExpression> exprs) {
		BPLExpression result = BPLBoolLiteral.TRUE;
		for (BPLExpression e : exprs) {
			if (result == BPLBoolLiteral.TRUE) {
				result = e;
			} else {
				result = new BPLBinaryLogicalExpression(
						BPLBinaryLogicalExpression.Operator.AND, result, e);
			}
		}
		return result;
	}

	public static BPLExpression conjunction(BPLExpression... exprs) {
		BPLExpression result = BPLBoolLiteral.TRUE;
		for (BPLExpression e : exprs) {
			if (result == BPLBoolLiteral.TRUE) {
				result = e;
			} else {
				if (e != BPLBoolLiteral.TRUE) {
					result = new BPLBinaryLogicalExpression(
							BPLBinaryLogicalExpression.Operator.AND, result, e);
				}
			}
		}
		return result;
	}
}

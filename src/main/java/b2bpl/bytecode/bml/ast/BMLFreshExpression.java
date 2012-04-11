package b2bpl.bytecode.bml.ast;

import b2bpl.bytecode.bml.IBMLExpressionVisitor;


public class BMLFreshExpression extends BMLExpression {

  private final BMLExpression expression;

  public BMLFreshExpression(BMLExpression expression) {
    this.expression = expression;
  }

  public BMLExpression getExpression() {
    return expression;
  }

  public boolean isPredicate() {
    return true;
  }

  public <R> R accept(IBMLExpressionVisitor<R> visitor) {
    return visitor.visitFreshExpression(this);
  }

  public String toString() {
    return "\\fresh(" + expression + ")";
  }
}

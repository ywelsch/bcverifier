package b2bpl.bytecode.bml.ast;

import b2bpl.bytecode.bml.IBMLExpressionVisitor;


public class BMLRelationalExpression extends BMLBinaryExpression {

  private final Operator operator;

  public BMLRelationalExpression(
      Operator operator,
      BMLExpression left,
      BMLExpression right) {
    super(left, right);
    this.operator = operator;
  }

  public Operator getOperator() {
    return operator;
  }

  public boolean isPredicate() {
    return true;
  }

  public <R> R accept(IBMLExpressionVisitor<R> visitor) {
    return visitor.visitRelationalExpression(this);
  }

  public String toString() {
    return left + " " + operator + " " + right;
  }

  public static enum Operator {

    LESS("<"),

    GREATER(">"),

    LESS_EQUAL("<="),

    GREATER_EQUAL(">=");

    private final String token;

    private Operator(String token) {
      this.token = token;
    }

    public String toString() {
      return token;
    }
  }
}

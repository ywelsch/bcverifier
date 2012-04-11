package b2bpl.bytecode.bml.ast;

import b2bpl.bytecode.bml.IBMLExpressionVisitor;


public class BMLStackCounterExpression extends BMLExpression {

  private int counter = -1;

  public int getCounter() {
    return counter;
  }

  public void setCounter(int counter) {
    this.counter = counter;
  }

  public <R> R accept(IBMLExpressionVisitor<R> visitor) {
    return visitor.visitStackCounterExpression(this);
  }

  public String toString() {
    return "\\st_cntr";
  }
}

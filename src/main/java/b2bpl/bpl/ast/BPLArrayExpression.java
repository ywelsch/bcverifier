package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLArrayExpression extends BPLExpression {

  private final BPLExpression prefix;

  private final BPLExpression[] accessors;

  public BPLArrayExpression(BPLExpression prefix, BPLExpression... accessors) {
    super(Precedence.ATOM);
    this.prefix = prefix;
    this.accessors = accessors;
  }

  public BPLExpression getPrefix() {
    return prefix;
  }

  public BPLExpression[] getAccessors() {
    return accessors;
  }

  public <R> R accept(IBPLVisitor<R> visitor) {
    return visitor.visitArrayExpression(this);
  }
  
  @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix.toString());
        sb.append('[');
        for(BPLExpression exp : accessors){
            sb.append(exp.toString());
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(']');
        return sb.toString();
    }
}

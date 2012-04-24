package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLTypeName extends BPLType {

  private final String name;
  private final BPLType[] params;

  public BPLTypeName(String name, BPLType...params) {
    this.name = name;
    this.params = params;
  }

  public String getName() {
    return name;
  }
  
  public BPLType[] getParameters() {
      return params;
  }

  public boolean isTypeName() {
    return true;
  }

  public <R> R accept(IBPLVisitor<R> visitor) {
    return visitor.visitTypeName(this);
  }

  public String toString() {
    return name;
  }
}

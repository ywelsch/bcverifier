package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLBuiltInType extends BPLType {

  public static final BPLBuiltInType BOOL = new BPLBuiltInType("bool");

  public static final BPLBuiltInType INT = new BPLBuiltInType("int");

  private final String name;

  private BPLBuiltInType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean isBuiltInType() {
    return true;
  }

  public <R> R accept(IBPLVisitor<R> visitor) {
    return visitor.visitBuiltInType(this);
  }

  public String toString() {
    return name;
  }
}

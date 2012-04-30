package b2bpl.bpl.ast;


public abstract class BPLType extends BPLNode {

  public static final BPLType[] EMPTY_ARRAY = new BPLType[0];

public boolean isBuiltInType() {
    return false;
  }

  public boolean isTypeName() {
    return false;
  }

  public boolean isArrayType() {
    return false;
  }

  public boolean isParameterizedType() {
    return false;
  }
}

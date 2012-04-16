package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLArrayType extends BPLType {
  private final BPLType[] parameterTypes;
    
  private final BPLType[] indexTypes;

  private final BPLType elementType;

  public BPLArrayType(BPLType indexType, BPLType elementType) {
    this(new BPLType[]{}, new BPLType[] { indexType }, elementType);
  }

  public BPLArrayType(
      BPLType indexType1,
      BPLType indexType2,
      BPLType elementType) {
    this(new BPLType[]{}, new BPLType[] { indexType1, indexType2 }, elementType);
  }

  public BPLArrayType(BPLType[] parameterTypes, BPLType[] indexTypes, BPLType elementType) {
    this.parameterTypes = parameterTypes;
    this.indexTypes = indexTypes;
    this.elementType = elementType;
  }
  
  public BPLType[] getParameterTypes() {
      return parameterTypes;
  }

  public BPLType[] getIndexTypes() {
    return indexTypes;
  }

  public BPLType getElementType() {
    return elementType;
  }

  public boolean isArrayType() {
    return true;
  }

  public <R> R accept(IBPLVisitor<R> visitor) {
    return visitor.visitArrayType(this);
  }
}

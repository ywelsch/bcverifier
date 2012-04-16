package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;

public class BPLTypeAlias extends BPLDeclaration{
    private final String name;
    private final BPLType type;
    
    public BPLTypeAlias(String name, BPLType type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public BPLType getType() {
        return type;
    }

    @Override
    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitTypeAlias(this);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("type ");
        sb.append(name);
        sb.append(" = ");
        sb.append(type);

        return sb.toString();
      }

}

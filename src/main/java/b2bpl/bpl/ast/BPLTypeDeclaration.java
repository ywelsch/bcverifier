package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;


public class BPLTypeDeclaration extends BPLDeclaration {

    private final String name;
    private final String[] typeParams;

    public BPLTypeDeclaration(String name, String... typeParams) {
        this.name = name;
        this.typeParams = typeParams;
    }
    
    public String getName() {
        return name;
    }

    public String[] getTypeParams() {
        return typeParams;
    }

    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitTypeDeclaration(this);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("type ");
        sb.append(name);
        sb.append(" ");
        for (int i = 0; i < typeParams.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(typeParams[i]);
        }
        sb.append(';');

        return sb.toString();
    }
}

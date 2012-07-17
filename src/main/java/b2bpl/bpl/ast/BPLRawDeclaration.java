package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;

public class BPLRawDeclaration extends BPLDeclaration {
    private String declaration;
    
    public BPLRawDeclaration(String declaration) {
        this.declaration = declaration;
    }

    public String getDeclarationString() {
        return declaration;
    }
    
    @Override
    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitRawDeclaration(this);
    }

}

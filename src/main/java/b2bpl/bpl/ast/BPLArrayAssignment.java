package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;

public class BPLArrayAssignment extends BPLExpression {
    private BPLExpression[] indices;
    private BPLExpression right;
    
    public BPLArrayAssignment(BPLExpression[] indices, BPLExpression right) {
        super(Precedence.ATOM);
        this.indices = indices;
        this.right = right;
    }

    public BPLExpression[] getIndices() {
        return indices;
    }

    public BPLExpression getRight() {
        return right;
    }

    @Override
    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitArrayAssignment(this);
    }

}

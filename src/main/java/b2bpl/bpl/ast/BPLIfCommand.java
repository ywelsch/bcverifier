package b2bpl.bpl.ast;

import java.util.List;

import b2bpl.bpl.IBPLVisitor;

public class BPLIfCommand extends BPLCommand {
    private BPLExpression predicate;
    private List<BPLCommand> thenBranch;
    private List<BPLCommand> elseBranch;
    
    public BPLIfCommand(BPLExpression predicate, List<BPLCommand> thenBranch, List<BPLCommand> elseBranch) {
        this.predicate = predicate;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public List<BPLCommand> getThenBranch() {
        return thenBranch;
    }

    public void setThenBranch(List<BPLCommand> thenBranch) {
        this.thenBranch = thenBranch;
    }

    public List<BPLCommand> getElseBranch() {
        return elseBranch;
    }

    public void setElseBranch(List<BPLCommand> elseBranch) {
        this.elseBranch = elseBranch;
    }

    public BPLExpression getPredicate() {
        return predicate;
    }

    public void setPredicate(BPLExpression predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitIfCommand(this);
    }

}

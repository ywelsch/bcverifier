package b2bpl.bpl.ast;

import b2bpl.bpl.IBPLVisitor;

public class BPLRawCommand extends BPLCommand {
    private String command;
    
    public BPLRawCommand(String commandString) {
        this.command = commandString;
    }

    public String getCommandString() {
        return command;
    }
    
    @Override
    public boolean isPassive() {
        return false;
    }
    
    @Override
    public <R> R accept(IBPLVisitor<R> visitor) {
        return visitor.visitRawCommand(this);
    }

}

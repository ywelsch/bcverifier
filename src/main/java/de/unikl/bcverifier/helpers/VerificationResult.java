package de.unikl.bcverifier.helpers;

import de.unikl.bcverifier.boogie.BoogieRunner;

public class VerificationResult {
    private boolean lastRunSuccess;
    private String lastMessage = "";
    private int lastUnreachableCodeCount = 0;
    private int lastErrorCount = 0;
    private int lastVerified = 0;
    
    public static VerificationResult fromBoogie(BoogieRunner runner) {
        VerificationResult result = new VerificationResult();
        result.lastRunSuccess = runner.getLastReturn();
        result.lastMessage = runner.getLastMessage();
        result.lastUnreachableCodeCount = runner.getLastUnreachalbeCodeCount();
        result.lastErrorCount = runner.getLastErrorCount();
        result.lastVerified = runner.getLastVerifiedCount();
        return result;
    }
    
    public boolean isLastRunSuccess() {
        return lastRunSuccess;
    }
    
    public void setLastRunSuccess(boolean lastRunSuccess) {
        this.lastRunSuccess = lastRunSuccess;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public int getLastUnreachableCodeCount() {
        return lastUnreachableCodeCount;
    }
    
    public void setLastUnreachableCodeCount(int lastUnreachableCodeCount) {
        this.lastUnreachableCodeCount = lastUnreachableCodeCount;
    }
    
    public int getLastErrorCount() {
        return lastErrorCount;
    }
    
    public void setLastErrorCount(int lastErrorCount) {
        this.lastErrorCount = lastErrorCount;
    }
    
    public int getLastVerified() {
        return lastVerified;
    }
    
    public void setLastVerified(int lastVerified) {
        this.lastVerified = lastVerified;
    }
}

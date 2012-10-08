package de.unikl.bcverifier.boogie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class BoogieRunner {
    private static final Logger log = Logger.getLogger(BoogieRunner.class);
    
    public static class BoogieRunException extends Exception{
        private static final long serialVersionUID = 6094316736644132810L;

        public BoogieRunException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
    
    private static String BOOGIE_COMMAND = null;
    
    public static synchronized void setBoogieCommand(String cmd) {
    	BOOGIE_COMMAND = cmd;
    }
    
    public static synchronized String getBoogieCommand() {
    	if (BOOGIE_COMMAND == null)	{
    		try{
    			BOOGIE_COMMAND = System.getenv("BOOGIE_CMD");
    			if(BOOGIE_COMMAND == null || BOOGIE_COMMAND.equals("")){
    				log.debug("Could not get boogie cmd from environment");
    				String os = System.getProperty("os.name");
    				String[] cmdArgs;
    				if (os.toLowerCase().indexOf("win") >= 0) {
    					cmdArgs = new String[]{"where", "boogie"};
    				} else {
    					cmdArgs = new String[]{"/bin/bash", "-l",  "-c", "which boogie"};
    				}
    				Process p = Runtime.getRuntime().exec(cmdArgs);
    				BOOGIE_COMMAND = IOUtils.toString(p.getInputStream()).trim();
    				log.debug("Which returned "+BOOGIE_COMMAND);
    				log.debug(IOUtils.toString(p.getErrorStream()));
    				if(BOOGIE_COMMAND == null || BOOGIE_COMMAND.equals("")){
    					return BOOGIE_COMMAND = "boogie";
    				}
    			} else {
    				log.debug("Got boogie cmd from environment: "+BOOGIE_COMMAND);
    			}
    		} catch (IOException e){
    			e.printStackTrace();
    			return BOOGIE_COMMAND = "boogie";
    		}
    	}
    	return BOOGIE_COMMAND;
    }
    
    private boolean verify = true;
    private boolean smokeTest = false;
    private int loopUnroll = 5;
    private int timelimit = 0;
    private boolean lastRunSuccess;
    private String lastMessage = "";
    private int lastUnreachableCodeCount = 0;
    private int lastErrorCount = 0;
    private int lastVerified = 0;
    
    public void setLoopUnroll(int loopUnroll) {
        this.loopUnroll = loopUnroll;
    }

    public void setVerify(boolean b){
        verify = b;
    }
    
    public void setSmokeTest(boolean b){
        smokeTest = b;
    }
    
	public void setTimelimit(int proverTimelimit) {
		timelimit = proverTimelimit;
	}
    
    public boolean getLastReturn(){
        return lastRunSuccess;
    }
    
    public int getLastErrorCount() {
        return lastErrorCount;
    }
    
    public int getLastVerifiedCount() {
        return lastVerified;
    }
    
    public String getLastMessage(){
    	return lastMessage;
    }
    
    public int getLastUnreachalbeCodeCount(){
        return lastUnreachableCodeCount;
    }
    
    public void runBoogie(File boogieFile) throws BoogieRunException{
        Runtime runtime = Runtime.getRuntime();
        File workingDir = boogieFile.getParentFile();
        InputStream processOutput = null;
        try {
            ArrayList<String> parameters = new ArrayList<String>();
            Collections.addAll(parameters, getBoogieCommand().split(" "));
            parameters.add("/nologo");
            parameters.add("/noinfer");
            parameters.add("/loopUnroll:" + loopUnroll);
            if(smokeTest){
                parameters.add("/smoke");
            }
            if (timelimit > 0) {
            	parameters.add("/timeLimit:" + timelimit);
            }
//            parameters.add("/mv:boogie.model");
//            parameters.add("/errorTrace:2");
            if(!verify){
                parameters.add("/noVerify");
            }
            parameters.add(boogieFile.getAbsolutePath());
            Process p = runtime.exec(parameters.toArray(new String[parameters.size()]), null, workingDir);
            processOutput = p.getInputStream();
            String result = IOUtils.toString(processOutput).trim();
//            String errors = IOUtils.toString(p.getErrorStream());
            lastUnreachableCodeCount = parseUnreachableMessages(result);
            lastRunSuccess = parseLastOutputLine(result);
            lastMessage = result;
        } catch (IOException e) {
        	log.debug("Boogie could not be started " + e.getMessage());
            throw new BoogieRunException("Boogie could not be started.", e);
        } finally {
            if(processOutput != null){
                IOUtils.closeQuietly(processOutput);
            }
        }
    }

    private static int parseUnreachableMessages(String result) {
        int count = 0;
        String[] lines = result.split("\n");
        for(String line : lines){
            if(line.matches("found unreachable code:")){
                count++;
            }
        }
        return count;
    }

    private boolean parseLastOutputLine(String result) {
        String[] lines = result.split("\n");
        String lastLine = lines[lines.length-1];
        Pattern lastLinePattern = Pattern.compile("Boogie program verifier finished with (\\d+) verified, (\\d+) error(s)?");
        Matcher errorMatcher = lastLinePattern.matcher(lastLine);
        if(errorMatcher.matches()){
            lastVerified = Integer.parseInt(errorMatcher.group(1));
            lastErrorCount = Integer.parseInt(errorMatcher.group(2));
            return lastErrorCount == 0;
        } else {
            lastErrorCount = 0;
            lastVerified = 0;
            return false;
        }
    }
}

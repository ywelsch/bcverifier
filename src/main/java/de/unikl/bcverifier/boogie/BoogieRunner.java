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
    
    public static String BOOGIE_COMMAND = "boogie";
    static {
        try{
            BOOGIE_COMMAND = System.getenv("BOOGIE_CMD");
            if(BOOGIE_COMMAND == null || BOOGIE_COMMAND.equals("")){
                log.debug("Could not get boogie cmd from environment");
                Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-l",  "-c", "which boogie"});
                BOOGIE_COMMAND = IOUtils.toString(p.getInputStream()).trim();
                log.debug("Which returned "+BOOGIE_COMMAND);
                log.debug(IOUtils.toString(p.getErrorStream()));   
            } else {
                log.debug("Got boogie cmd from environment: "+BOOGIE_COMMAND);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    private static boolean verify = true;
    private static boolean smokeTest = false;
    private static int loopUnroll = 5;
    private static boolean lastRunSuccess;
    private static String lastMessage = "";
    private static int lastUnreachableCodeCount = 0;
    private static int lastErrorCount = 0;
    private static int lastVerified = 0;
    
    public static void setLoopUnroll(int loopUnroll) {
        BoogieRunner.loopUnroll = loopUnroll;
    }

    public static void setVerify(boolean b){
        verify = b;
    }
    
    public static void setSmokeTest(boolean b){
        smokeTest = b;
    }
    
    public static boolean getLastReturn(){
        return lastRunSuccess;
    }
    
    public static int getLastErrorCount() {
        return lastErrorCount;
    }
    
    public static int getLastVerifiedCount() {
        return lastVerified;
    }
    
    public static String getLastMessage(){
    	return lastMessage;
    }
    
    public static int getLastUnreachalbeCodeCount(){
        return lastUnreachableCodeCount;
    }
    
    public static void runBoogie(File boogieFile) throws BoogieRunException{
        Runtime runtime = Runtime.getRuntime();
        File workingDir = boogieFile.getParentFile();
        InputStream processOutput = null;
        try {
            ArrayList<String> parameters = new ArrayList<String>();
            Collections.addAll(parameters, BOOGIE_COMMAND.split(" "));
            parameters.add("/nologo");
            parameters.add("/loopUnroll:" + loopUnroll);
            if(smokeTest){
                parameters.add("/smoke");
            }
//            parameters.add("/timeLimit:30"); //limit time spent to verify each procedure to 30 seconds
//            parameters.add("/mv:boogie.model");
//            parameters.add("/errorTrace:2");
            if(!verify){
                parameters.add("/noVerify");
            }
            parameters.add(boogieFile.getName());
            Process p = runtime.exec(parameters.toArray(new String[parameters.size()]), null, workingDir);
            processOutput = p.getInputStream();
            String result = IOUtils.toString(processOutput).trim();
//            String errors = IOUtils.toString(p.getErrorStream());
            lastUnreachableCodeCount = parseUnreachableMessages(result);
            lastRunSuccess = parseLastOutputLine(result);
            lastMessage = result;
        } catch (IOException e) {
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

    private static boolean parseLastOutputLine(String result) {
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

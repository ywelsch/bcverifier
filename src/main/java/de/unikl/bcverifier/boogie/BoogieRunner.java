package de.unikl.bcverifier.boogie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

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
    private static boolean lastRunSuccess;
    
    public static void setVerify(boolean b){
        verify = b;
    }
    
    public static boolean getLastReturn(){
        return lastRunSuccess;
    }
    
    public static String runBoogie(File boogieFile) throws BoogieRunException{
        Runtime runtime = Runtime.getRuntime();
        File workingDir = boogieFile.getParentFile();
        InputStream processOutput = null;
        try {
            ArrayList<String> parameters = new ArrayList<String>();
            Collections.addAll(parameters, BOOGIE_COMMAND.split(" "));
            parameters.add("/nologo");
//            parameters.add("/smoke");
//            parameters.add("/timeLimit:30"); //limit time spent to verify each procedure to 30 seconds
//            parameters.add("/mv:boogie.model");
            if(!verify){
                parameters.add("/noVerify");
            }
            parameters.add(boogieFile.getName());
            Process p = runtime.exec(parameters.toArray(new String[parameters.size()]), null, workingDir);
            processOutput = p.getInputStream();
            String result = IOUtils.toString(processOutput).trim();
//            String errors = IOUtils.toString(p.getErrorStream());
            lastRunSuccess = parseLastOutputLine(result);
            return result;
        } catch (IOException e) {
            throw new BoogieRunException("Boogie could not be started.", e);
        } finally {
            if(processOutput != null){
                IOUtils.closeQuietly(processOutput);
            }
        }
    }

    private static boolean parseLastOutputLine(String result) {
        String[] lines = result.split("\n");
        String lastLine = lines[lines.length-1];
        return lastLine.matches("Boogie program verifier finished with \\d* verified, \\d* errors");
    }
}

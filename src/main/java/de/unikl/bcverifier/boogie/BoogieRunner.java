package de.unikl.bcverifier.boogie;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

public class BoogieRunner {
    public static class BoogieRunException extends Exception{
        private static final long serialVersionUID = 6094316736644132810L;

        public BoogieRunException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
    
    public static String BOOGIE_COMMAND = "boogie";
    static {
        try{
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-l",  "-c", "which boogie"});
            BOOGIE_COMMAND = IOUtils.toString(p.getInputStream()).trim();
            System.out.println(BOOGIE_COMMAND);
            System.out.println(IOUtils.toString(p.getErrorStream()));
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
            parameters.add(BOOGIE_COMMAND);
            parameters.add("/nologo");
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

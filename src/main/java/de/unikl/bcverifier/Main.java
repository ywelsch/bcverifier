package de.unikl.bcverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class Main {
    public enum VerifyAction {
    	NONE, TYPECHECK, VERIFY;
    	public static final String allValues = Arrays.toString(VerifyAction.values()); 
    }
    
    public static class Configuration {
    	@Parameter(names = {"-d", "--debug"}, description = "Debug mode")
        private boolean debug = false;
    	@Parameter(names = {"-c", "--compile"}, description = "Compile .java files in library directory before generating Boogie specification")
        private boolean compileFirst = false;
        @Parameter(names = {"-a", "--action"}, description = "Specifies action after generation (one of [NONE, TYPECHECK, VERIFY])")
        private VerifyAction action = VerifyAction.VERIFY;
        @Parameter(names = {"-i" , "--invariant"}, description = "Path to the file containing the coupling invariant", required = true)
        private File invariant;
        @Parameter(names = {"-o" , "--output"}, description = "Path to generated Boogie file")
        private File output;
        @Parameter(names = {"-l", "--libs"}, description = "Path to the libraries to compare", arity = 2, required = true, validateWith = DirectoryValidator.class)
        private List<File> dirs = new ArrayList<File>();
        
        public static class DirectoryValidator implements IParameterValidator {
    		public void validate(String name, String value) throws ParameterException {
    			if (value.equals(new ArrayList<File>().toString())) {
    				return;
    			}
    			File f = new File(value);
    			if (!f.isDirectory()) {
    				throw new ParameterException("Value " + value + " for parameter " + name + " must be a valid directory");
    			}
    		}
        }
        
        public boolean isDebug() {
            return debug;
        }
        public boolean isCompileFirst() {
            return compileFirst;
        }
        public boolean isCheck() {
            return !action.equals(VerifyAction.NONE);
        }
        public boolean isVerify() {
            return action.equals(VerifyAction.VERIFY);
        }
        public File library1() {
        	return dirs.get(0);
        }
        public File library2() {
        	return dirs.get(1);
        }
        public File invariant() {
        	return invariant;
        }
        public File output() {
        	return output;
        }
    }
    
   public static void main(String... args) {
        PropertyConfigurator.configure("log4j.properties");
        
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
    	parser.setProgramName("Main");
        try {
        	parser.parse(args);
        } catch (ParameterException e) {
        	System.err.println("Error parsing command line parameters " + e.getMessage());
        	parser.usage();
        	return;
        }
        Logger.getRootLogger().setLevel(config.isDebug() ? Level.DEBUG : Level.INFO);
        try {
        	Library library = new Library(config.invariant(), config.library1(), config.library2(), config.output() != null ? config.output() : new File(config.invariant().getParentFile(), "specification.bpl"));
            if(config.isCompileFirst()){
                library.compile();
            }
            library.translate();
            if(config.isCheck()){
                library.check(config.isVerify());
                System.out.println(BoogieRunner.getLastMessage());
                System.out.println("Found unreachable code points: "+BoogieRunner.getLastUnreachalbeCodeCount());
            }
        } catch (TranslationException e) {
            System.out.println("Error while translating to Boogie:");
            e.printStackTrace();
        }
   }
}

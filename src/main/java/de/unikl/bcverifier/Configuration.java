package de.unikl.bcverifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Configuration {
	public enum VerifyAction {
    	NONE, TYPECHECK, VERIFY;
    	public static final String allValues = Arrays.toString(VerifyAction.values()); 
    }
	@Parameter(names = {"-h", "--help"}, description = "Show this help screen and return")
    private boolean help = false;
	@Parameter(names = {"-v", "--version"}, description = "Show version String")
	private boolean showVersion = false;
	@Parameter(names = {"-d", "--debug"}, description = "Debug mode")
    private boolean debug = false;
	@Parameter(names = {"-c", "--compile"}, description = "Compile .java files in library directory before generating Boogie specification")
    private boolean compileFirst = false;
	@Parameter(names = {"-H", "--heapassumes"}, description = "Add assume WellformedHeap after every heap assignment")
	private boolean assumeWellformedHeap = false;
	@Parameter(names = {"-X", "--extensionality"}, description = "Add extensionality axioms")
	private boolean extensionality = false;
	@Parameter(names = {"-S", "--smoketest"}, description = "Perform smoke test during verification")
    private boolean smoke = false;
	@Parameter(names = {"-L", "--loopUnroll"}, description = "The cap for loop unrolling")
	private int loopUnrollCap = 5;
	@Parameter(names = {"-N", "--nullchecks"}, description = "Disable null checks to field accesses and method calls as well as !=0 checks to division/modulo")
	private boolean disableNullChecks = false;
	@Parameter(names = {"-a", "--action"}, description = "Specifies action after generation (one of [NONE, TYPECHECK, VERIFY])")
    private VerifyAction action = VerifyAction.VERIFY;
    @Parameter(names = {"-i" , "--invariant"}, description = "Path to the file containing the coupling invariant", required = true, validateWith = Configuration.FileValidator.class)
    private File invariant;
    @Parameter(names = {"-o" , "--output"}, description = "Path to generated Boogie file")
    private File output;
    @Parameter(names = {"-p" , "--places"}, description = "Path to places configuration file", validateWith = Configuration.FileValidator.class)
    private File places;
    @Parameter(names = {"-l", "--libs"}, description = "Path to the libraries to compare", arity = 2, required = true, validateWith = Configuration.DirectoryValidator.class)
    private List<File> dirs = new ArrayList<File>();
	private String versionString;
    
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
    
    public static class FileValidator implements IParameterValidator {
		public void validate(String name, String value) throws ParameterException {
			if (value == null) {
				return;
			}
			File f = new File(value);
			if (!f.isFile()) {
				throw new ParameterException("Value " + value + " for parameter " + name + " must be a valid file");
			}
		}
    }
    
    public boolean isDebug() {
        return debug;
    }
    public boolean isCompileFirst() {
        return compileFirst;
    }
    public boolean isAssumeWellformedHeap() {
        return assumeWellformedHeap;
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
    	if (output == null) {
    		output = new File(invariant().getParentFile(), "output.bpl");
    	}
    	return output;
    }
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public void setCompileFirst(boolean compileFirst) {
		this.compileFirst = compileFirst;
	}
	public void setAssumeWellformedHeap(boolean assumeWellformedHeap) {
		this.assumeWellformedHeap = assumeWellformedHeap;
	}
	public void setAction(VerifyAction action) {
		this.action = action;
	}
	public void setInvariant(File invariant) {
		this.invariant = invariant;
	}
	public void setLibraries(File lib1, File lib2) {
		this.dirs = new ArrayList<File>();
		dirs.add(lib1);
		dirs.add(lib2);
	}
	public void setOutput(File file) {
		output = file;
	}
	public boolean extensionalityEnabled() {
		return extensionality;
	}
	public void setExtensionality(boolean b) {
	    extensionality = b;
	}
    public boolean isSmokeTestOn() {
        return smoke;
    }
    public void setSmokeTestOn(boolean smoke) {
        this.smoke = smoke;
    }
    public boolean isNullChecks() {
        return !disableNullChecks;
    }
    public void setNullChecks(boolean nullChecks) {
        this.disableNullChecks = !nullChecks;
    }
    public boolean isHelp() {
		return help;
	}
    public boolean showVersion() {
		return showVersion;
	}
    public int getLoopUnrollCap() {
        return loopUnrollCap;
    }
    public void setLoopUnrollCap(int loopUnroll) {
        this.loopUnrollCap = loopUnroll;
    }
    public File configFile() {
    	return places;
    }
    public String getVersionString() {
    	if (versionString == null) {
    		Properties prop = new Properties();
    		InputStream in = Configuration.class.getResourceAsStream("/project.properties");
    		if (in != null) {
    			try {
    				prop.load(in);
    			} catch (IOException e) {
    				e.printStackTrace();
    			} finally {
    				IOUtils.closeQuietly(in);
    			}
    		}
    		versionString = prop.getProperty("version", "unknown");
    	}
		return versionString;
	}
}
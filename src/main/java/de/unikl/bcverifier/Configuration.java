package de.unikl.bcverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Configuration {
	public enum VerifyAction {
    	NONE, TYPECHECK, VERIFY;
    	public static final String allValues = Arrays.toString(VerifyAction.values()); 
    }
    
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
	@Parameter(names = {"-N", "--nullchecks"}, description = "Disable null checks to field accesses and method calls as well as !=0 checks to division/modulo")
	private boolean disableNullChecks = false;
	@Parameter(names = {"-a", "--action"}, description = "Specifies action after generation (one of [NONE, TYPECHECK, VERIFY])")
    private VerifyAction action = VerifyAction.VERIFY;
    @Parameter(names = {"-i" , "--invariant"}, description = "Path to the file containing the coupling invariant", required = true)
    private File invariant;
    @Parameter(names = {"-o" , "--output"}, description = "Path to generated Boogie file")
    private File output;
    @Parameter(names = {"-l", "--libs"}, description = "Path to the libraries to compare", arity = 2, required = true, validateWith = Configuration.DirectoryValidator.class)
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
    		output = new File(invariant().getParentFile(), "specification.bpl");
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
}
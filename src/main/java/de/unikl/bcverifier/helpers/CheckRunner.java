package de.unikl.bcverifier.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Configuration;
import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.sourcecomp.SourceInCompatibilityException;
import de.unikl.bcverifier.specification.GenerationException;
import de.unikl.bcverifier.specification.GeneratorFactory;

public class CheckRunner {
    public static class CheckRunException extends Exception {
        private static final long serialVersionUID = 5370488906805720234L;

        public CheckRunException(String msg){
            super(msg);
        }
        
        public CheckRunException(String msg, Throwable ex) {
            super(msg, ex);
        }
    }
    
    private static VerificationResult run(BCCheckDefinition def, File outputDir, boolean doCheck, boolean doSmoke) throws CheckRunException{
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(def.getFlags());
        } catch (ParameterException e) {
            throw new CheckRunException("Error parsing arguments", e);
        }
        
        File specificationFile = new File(outputDir, "output"+def.getCheckIndex()+".bpl");
        File lib1 = new File(def.getLibDir(), "old");
        File lib2 = new File(def.getLibDir(), "new");
        config.setSpecification(def.getSpecification());
        config.setLibraries(lib1, lib2);
        config.setOutput(specificationFile);
        if(doCheck){
            config.setAction(VerifyAction.VERIFY);
            if(doSmoke){
                config.setSmokeTestOn(true);
            }
        } else {
            config.setAction(VerifyAction.TYPECHECK);
        }
        config.setLoopUnrollCap(def.getLoopUnrollCap());
        try{
            Library library = new Library(config);
            return library.runLifecycle();
        } catch (TranslationException ex){
            throw new CheckRunException("Error translating bytecode", ex);
        } catch (GenerationException e) {
            throw new CheckRunException("Error generating specification", e);
        } catch (SourceInCompatibilityException e) {
            throw new CheckRunException("Source incompatibility", e);
        } catch (CompileException e) {
        	throw new CheckRunException("Compilation error", e);
		}
    }
    
    public static void generate(BCCheckDefinition def, File outputDir) throws CheckRunException {
        run(def, outputDir, false, false);
    }
    
    public static VerificationResult runCheck(BCCheckDefinition def) throws CheckRunException {
        VerificationResult result = run(def, new File(def.getLibDir(), "bpl"), true, false);
        result.setLastRunSuccess(result.getLastErrorCount() == def.getExpectedErrors());
        return result;
    }
    
    public static VerificationResult runSmokeTest(BCCheckDefinition def) throws CheckRunException {
        VerificationResult result = run(def, new File(def.getLibDir(), "bpl"), true, true);
        
        if(result.getLastErrorCount() != def.getExpectedErrors()){
            Logger.getLogger(CheckRunner.class).debug(result.getLastMessage());
            throw new CheckRunException("Expected "+def.getExpectedErrors()+" errors, but got "+result.getLastErrorCount());
        }
        
        File boogieOutput = new File(def.getLibDir(), "bpl/boogie_output.txt");
        try{
            FileUtils.write(boogieOutput, result.getLastMessage());
        } catch(IOException ex){
            throw new CheckRunException("Error writing smoke test log", ex);
        }
        result.setLastRunSuccess(result.getLastUnreachableCodeCount() == def.getExpectedDeadCodePoints());
        return result;
    }
}

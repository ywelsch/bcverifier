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
import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.boogie.BoogieRunner;

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
    
    public static boolean runCheck(BCCheckDefinition def) throws CheckRunException{
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(def.getFlags());
        } catch (ParameterException e) {
            throw new CheckRunException("Error parsing arguments", e);
        }
        
        File specificationFile = new File(def.getLibDir(), "bpl/output.bpl");
        File lib1 = new File(def.getLibDir(), "old");
        File lib2 = new File(def.getLibDir(), "new");
        config.setInvariant(def.getInvariant());
        config.setConfigFile(def.getPreconditions());
        config.setLocalInvariant(def.getLocalInv());
        config.setLibraries(lib1, lib2);
        config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(def.getLoopUnrollCap());
        TranslationController tc = new TranslationController();
        Library library = new Library(config);
        library.setTranslationController(tc);
        library.compile();
        try{
            library.translate();
        } catch (TranslationException ex){
            throw new CheckRunException("Error translating bytecode", ex);
        }
        library.check();
        return BoogieRunner.getLastErrorCount() == def.getExpectedErrors();
    }
    
    public static boolean runSmokeTest(BCCheckDefinition def) throws CheckRunException {
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(def.getFlags());
        } catch (ParameterException e) {
            Logger.getLogger(CheckRunner.class).warn(e);
        }
        
        File specificationFile = new File(def.getLibDir(), "bpl/output.bpl");
        File lib1 = new File(def.getLibDir(), "old");
        File lib2 = new File(def.getLibDir(), "new");
        config.setInvariant(def.getInvariant());
        config.setConfigFile(def.getPreconditions());
        config.setLocalInvariant(def.getLocalInv());
        config.setLibraries(lib1, lib2);
        config.setNullChecks(false);
        config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(def.getLoopUnrollCap());
        config.setSmokeTestOn(true);
        TranslationController tc = new TranslationController();
        Library library = new Library(config);
        library.setTranslationController(tc);
        library.compile();
        try{
            library.translate();
        } catch(TranslationException ex){
            throw new CheckRunException("Error translating bytecode", ex);
        }
        library.check();
        
        if(BoogieRunner.getLastErrorCount() != def.getExpectedErrors()){
            Logger.getLogger(CheckRunner.class).debug(BoogieRunner.getLastMessage());
            throw new CheckRunException("Expected "+def.getExpectedErrors()+" errors, but got "+BoogieRunner.getLastErrorCount());
        }
        
        File boogieOutput = new File(def.getLibDir(), "bpl/boogie_output.txt");
        try{
            FileUtils.write(boogieOutput, BoogieRunner.getLastMessage());
        } catch(IOException ex){
            throw new CheckRunException("Error writing smoke test log", ex);
        }
        
        return BoogieRunner.getLastUnreachalbeCodeCount() == def.getExpectedDeadCodePoints();
    }
}

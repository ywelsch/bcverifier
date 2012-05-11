package de.unikl.bcverifier;

import java.io.File;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);
    
    private static class Configuration {
        private boolean debug = false;
        private boolean compileFirst = false;
        private boolean check = false;
        private boolean workOnAll = false;
        private boolean verify = true;
        public boolean isDebug() {
            return debug;
        }
        public void setDebug(boolean debug) {
            this.debug = debug;
        }
        public boolean isCompileFirst() {
            return compileFirst;
        }
        public void setCompileFirst(boolean compileFirst) {
            this.compileFirst = compileFirst;
        }
        public boolean isCheck() {
            return check;
        }
        public void setCheck(boolean check) {
            this.check = check;
        }
        public boolean isWorkOnAll() {
            return workOnAll;
        }
        public void setWorkOnAll(boolean workOnAll) {
            this.workOnAll = workOnAll;
        }
        public boolean isVerify() {
            return verify;
        }
        public void setVerify(boolean verify) {
            this.verify = verify;
        }
    }
    
    public static class ConfigurationException extends Exception {
        private static final long serialVersionUID = 6582774819912175668L;

        public ConfigurationException(String msg) {
            super(msg);
        }
    }
    
    public static void main(String... args) {
        PropertyConfigurator.configure("log4j.properties");
        
        Configuration config = new Configuration();
        try {
            File givenPath = parseParames(args, config);
            if(givenPath == null){
                printUsage();
                return;
            }
            if(config.isDebug()){
                Logger.getRootLogger().setLevel(Level.DEBUG);
            } else {
                Logger.getRootLogger().setLevel(Level.INFO);
            }
            
            if(config.isWorkOnAll()){
                log.debug("Parsing all libraries in "+givenPath);
                File libraryPath;
                for(String path : givenPath.list(new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NotFileFilter(HiddenFileFilter.HIDDEN)))){
                    log.debug("Parsing library in "+path);
                    libraryPath = new File(givenPath, path);
                    workOnLibrary(config, libraryPath);
                }
            } else {
                log.debug("Working Directory = " + givenPath);
                workOnLibrary(config, givenPath);
            }
        } catch (ConfigurationException e) {
            System.out.println(e.getMessage());
            System.out.println();
            printUsage();
        } catch (TranslationException e) {
            System.out.println("Error while translating to Boogie:");
            e.printStackTrace();
        }
    }

    private static void workOnLibrary(Configuration config, File libraryPath)
            throws TranslationException {
        Library library = new Library(libraryPath);
        if(config.isCompileFirst()){
            library.compile();
        }
        library.translate();
        if(config.isCheck()){
            library.check(config.isVerify());
        }
        System.out.println(BoogieRunner.getLastMessage());
    }

    private static File parseParames(String[] args, Configuration config) throws ConfigurationException {
        File givenPath = null;
        for(String arg : args){
            if(arg.equals("-a")){
                config.setWorkOnAll(true);
            } else if(arg.equals("-d")){
                config.setDebug(true);
            } else if(arg.equals("-c")){
                config.setCheck(true);
            } else if(arg.equals("-nv")){
                config.setCheck(true);
                config.setVerify(false);
            } else if(arg.equals("-cf")){
                config.setCompileFirst(true);
            } else {
                if(givenPath != null){
                    throw new ConfigurationException("Wrong usage: Specify only one library path");
                }
                givenPath = new File(arg);
                if(!givenPath.exists()){
                    throw new ConfigurationException("Given directory does not exist.");
                }
            }
        }
        return givenPath;
    }
    
    private static void printUsage(){
        System.out.println(
                "Usage: Main [options] libraryPath\n" +
                "  -a   given path is the root of a collection of libraries (work on all of them)\n" +
                "  -d   debug\n" +
                "  -c   check generated Boogie files after generation\n" +
                "  -nv  do not verify while checking generated Boogie files\n" +
                "  -cf  compile .java files in library directory before generating Boogie specification"
        );
    }
}

package de.unikl.bcverifier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class Main {
    
    public static void main(String... args) {
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
    	parser.setProgramName("bcv");
        try {
        	parser.parse(args);
        } catch (ParameterException e) {
        	if (config.showVersion()) System.out.println("BCVerifier version " + config.getVersionString());
        	if (!config.isHelp()) {
        		System.err.println("Error parsing command line parameters: " + e.getMessage());
        		System.err.println("Use --help to get a list of all available options.");
        		return;
        	}
        }
        if (config.showVersion()) System.out.println("BCVerifier version " + config.getVersionString());
    	if (config.isHelp()) {
    		parser.usage();
    		return;
    	}
        Logger.getRootLogger().setLevel(config.isDebug() ? Level.DEBUG : Level.INFO);
        try {
        	Library library = new Library(config);
            if(config.isCompileFirst()){
                library.compile();
            }
            library.translate();
            if(config.isCheck()){
                library.check();
                System.out.println(BoogieRunner.getLastMessage());
                if (config.isSmokeTestOn()) {
                	System.out.println("Found unreachable code points: "+BoogieRunner.getLastUnreachalbeCodeCount());
                }
            }
        } catch (TranslationException e) {
            System.err.println("Error while translating to Boogie:");
            e.printStackTrace();
        }
   }
}

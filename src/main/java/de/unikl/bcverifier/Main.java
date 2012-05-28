package de.unikl.bcverifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
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
        	if (config.showVersion()) printVersion();
        	if (!config.isHelp()) {
        		System.err.println("Error parsing command line parameters: " + e.getMessage());
        		System.err.println("Use --help to get a list of all available options.");
        		return;
        	}
        }
        if (config.showVersion()) printVersion();
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
    private static void printVersion() {
    	Properties prop = new Properties();
		InputStream in = Main.class.getResourceAsStream("/project.properties");
		if (in != null) {
			try {
				prop.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		String version = prop.getProperty("version", "unknown");
		System.out.println("BCVerifier version " + version);
    }
}

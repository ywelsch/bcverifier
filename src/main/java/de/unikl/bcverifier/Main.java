package de.unikl.bcverifier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;

public class Main {
    
    public static void main(String... args) {
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
        	Library library = new Library(config);
            if(config.isCompileFirst()){
                library.compile();
            }
            library.translate();
            if(config.isCheck()){
                library.check();
                System.out.println(BoogieRunner.getLastMessage());
                System.out.println("Found unreachable code points: "+BoogieRunner.getLastUnreachalbeCodeCount());
            }
        } catch (TranslationException e) {
            System.out.println("Error while translating to Boogie:");
            e.printStackTrace();
        }
   }
}

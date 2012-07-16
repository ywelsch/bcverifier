package de.unikl.bcverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.unikl.bcverifier.Configuration.VerifyAction;
import de.unikl.bcverifier.Library.TranslationException;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.tests.LibraryTests;

public class SingleTestRunner {
    
    public static void main(String[] args) throws NumberFormatException, TranslationException {
        if(args.length != 1){
            JOptionPane.showMessageDialog(null, "Please pass the csv file containing the checking profiles as parameter!");
        }
        File csvFile = new File(args[0]);
        if(!csvFile.isFile() || !args[0].endsWith(".csv")){
            JOptionPane.showMessageDialog(null, "File is not a csv file: "+csvFile.getPath());
        }
        File libDir = csvFile.getParentFile();
        List<BCCheckDefinition> tests = BCCheckDefinition.parseDefinitions(libDir, csvFile);
        List<String> choiceStrings = buildTestcaseEntries(tests);
        String s = (String)JOptionPane.showInputDialog(
                null,
                "Choose test to run:",
                "Choose test",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choiceStrings.toArray(),
                choiceStrings.get(0));

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            int index = Integer.parseInt(s.substring(0, s.indexOf(':')));
            BCCheckDefinition test = tests.get(index);
            runTest(test);
            return;
        }
    }
    
    public static void runTest(BCCheckDefinition test) throws TranslationException {
        Configuration config = new Configuration();
        JCommander parser = new JCommander();
        parser.addObject(config);
        parser.setProgramName("Main");
        
        try {
            parser.parseWithoutValidation(test.getFlags());
        } catch (ParameterException e) {
            Logger.getLogger(LibraryTests.class).warn(e);
        }
        
        File specificationFile = new File(test.getLibDir(), "bpl/output.bpl");
        File lib1 = new File(test.getLibDir(), "old");
        File lib2 = new File(test.getLibDir(), "new");
        config.setInvariant(test.getInvariant());
        config.setConfigFile(test.getPreconditions());
        config.setLibraries(lib1, lib2);
        config.setOutput(specificationFile);
        config.setAction(VerifyAction.VERIFY);
        config.setLoopUnrollCap(test.getLoopUnrollCap());
        TranslationController tc = new TranslationController();
        Library library = new Library(config);
        library.setTranslationController(tc);
        library.compile();
        library.translate();
        library.check();
        if(BoogieRunner.getLastErrorCount() != test.getExpectedErrors()){
            System.out.println(BoogieRunner.getLastMessage());
        }
    }
    
    private static List<String> buildTestcaseEntries(List<BCCheckDefinition> testCases){
        List<String> testCaseStrings = new ArrayList<String>();
        BCCheckDefinition currentCase;
        StringBuilder builder;
        for(int i=0; i<testCases.size(); i++){
            currentCase = testCases.get(i);
            builder = new StringBuilder();
            builder.append(i);
            builder.append(": ");
            builder.append(currentCase.toString());
            testCaseStrings.add(builder.toString());
        }
        return testCaseStrings;
    }
}

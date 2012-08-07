package de.unikl.bcverifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.helpers.BCCheckDefinition;
import de.unikl.bcverifier.helpers.CheckRunner;
import de.unikl.bcverifier.helpers.CheckRunner.CheckRunException;

public class SingleTestRunner {
    
    public static void main(String[] args) throws NumberFormatException, CheckRunException {
        if(args.length != 1 && args.length != 2){
            JOptionPane.showMessageDialog(null, "Please pass the csv file containing the checking profiles as parameter!");
        }
        boolean doCheck = args.length == 1 || "yes".equals(args[1]);
        
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

        if ((s != null) && (s.length() > 0)) {
            int index = Integer.parseInt(s.substring(0, s.indexOf(':')));
            BCCheckDefinition test = tests.get(index);
            if(doCheck){
                if(!CheckRunner.runCheck(test)){
                    Logger.getLogger(SingleTestRunner.class).error("Check did not succeed!");
                    Logger.getLogger(SingleTestRunner.class).error(BoogieRunner.getLastMessage());
                } else {
                    Logger.getLogger(SingleTestRunner.class).info("Test completed successfully.");
                }
            } else {
                CheckRunner.generate(test, new File(test.getLibDir(), "bpl"));
                Logger.getLogger(SingleTestRunner.class).info("Test generated.");
            }
            return;
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

package de.unikl.bcverifier.tests;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;

import org.testng.annotations.Test;

import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;
import de.unikl.bcverifier.exceptionhandling.ErrorTrace;
import de.unikl.bcverifier.exceptionhandling.ErrorTraceParser;
import de.unikl.bcverifier.exceptionhandling.ErrorTraceParser.TraceParseException;
import de.unikl.bcverifier.exceptionhandling.ErrorTracePrinter;
import de.unikl.bcverifier.helpers.BCCheckDefinition;

public class LibraryTestsNG extends AbstractLibraryTestsNG {
	
	@Test(dataProvider = "boogieFiles")
	public void verifyBoogieFiles(BCCheckDefinition test, File boogieFile) throws BoogieRunException{
	    BoogieRunner runner = new BoogieRunner();
	    runner.setVerify(true);
	    runner.runBoogie(boogieFile);
	    if(test.getExpectedErrors()>0){
	        if(runner.getLastReturn() || runner.getLastErrorCount() != test.getExpectedErrors()){ //not expected
	            ErrorTraceParser parser = new ErrorTraceParser();
	            try{
	                ErrorTrace errorTrace = parser.parse(runner.getLastMessage());
	                ErrorTracePrinter printer = new ErrorTracePrinter();
	                printer.print(errorTrace, true);
	                fail(printer.getOutput());
	            } catch(TraceParseException ex) {
	                fail(runner.getLastMessage(), ex);
	            }
	        }
	        assertTrue(!runner.getLastReturn() && runner.getLastErrorCount() == test.getExpectedErrors(), runner.getLastMessage());
	    } else {
	        if(!runner.getLastReturn()){
	            ErrorTraceParser parser = new ErrorTraceParser();
                try{
                    ErrorTrace errorTrace = parser.parse(runner.getLastMessage());
                    ErrorTracePrinter printer = new ErrorTracePrinter();
                    printer.print(errorTrace, true);
                    fail(printer.getOutput());
                } catch(TraceParseException ex) {
                    fail(runner.getLastMessage(), ex);
                }
	        }
	        assertTrue(runner.getLastReturn(), runner.getLastMessage());
	    }
	}
}

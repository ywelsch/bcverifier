package de.unikl.bcverifier.tests;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import de.unikl.bcverifier.helpers.BCCheckDefinition;

/**
 * same as LibraryTests but only checks .isl specifications 
 */
public class LibraryTestsIslOnly extends LibraryTests {
	@Override Object[] librariesToCheck() {
	    // filter out all specifications that are not .isl
		Object[] sup = super.librariesToCheck();
	    List<Object> result = Lists.newArrayList();
	    for (Object o : sup) {
	    	BCCheckDefinition spec = (BCCheckDefinition) o;
	    	if (spec.getSpecification().getName().endsWith(".isl")) {
	    		result.add(spec);
	    	}
	    }
	    return result.toArray();
	}
	
	@Override Object[] boogieFilesToCheck() {
		return new Object[0];
	}
	
}

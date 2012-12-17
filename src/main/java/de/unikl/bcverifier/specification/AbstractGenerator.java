package de.unikl.bcverifier.specification;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import b2bpl.bpl.ast.BPLVariableDeclaration;

import de.unikl.bcverifier.Configuration;

public class AbstractGenerator implements Generator {
    private Configuration config;
    
    public AbstractGenerator(Configuration config) {
        this.config = config;
    }
    
    protected Reader getReader() {
        try {
            return new FileReader(config.specification());
        } catch(FileNotFoundException e){
            Logger.getLogger(AbstractGenerator.class).error("Opening specificaiton file failed (should have been checked by configuration)", e);
            throw new RuntimeException("Internal error in configuration: Existance check of specification file not working.", e);
        }
    }
    
    protected Configuration getConfig() {
    	return config;
    }
    
    @Override
	public List<SpecExpr> generateInvariant() throws GenerationException {
    	return Collections.emptyList();
	}
	
    @Override
    public List<SpecExpr> generateLocalInvariant() throws GenerationException {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> generatePreconditions() throws GenerationException {
        return Collections.emptyList();
    }
    
    @Override
    public LocalPlaceDefinitions generateLocalPlaces() throws GenerationException {
        return new LocalPlaceDefinitions(Collections.<Integer,List<Place>>emptyMap(), Collections.<Integer,List<Place>>emptyMap());
    }
    
    @Override
    public List<String> generatePreludeAddition() throws GenerationException {
        return Collections.emptyList();
    }

	@Override
	public List<VariableDef> generateVars() throws GenerationException {
		return Collections.emptyList();
	}

	@Override
	public List<String> generateGlobalAssignments() throws GenerationException {
		return Collections.emptyList();
	}

	@Override
	public List<String> generateInitialAssignments() throws GenerationException {
		return Collections.emptyList();
	}

	@Override
	public List<BPLVariableDeclaration> generateGlobalVariables() throws GenerationException {
		return Collections.emptyList();
	}
}

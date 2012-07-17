package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import de.unikl.bcverifier.Configuration;

public abstract class AbstractGenerator implements Generator {
    private Configuration config;
    
    public AbstractGenerator(Configuration config) {
        this.config = config;
    }
    
    protected Configuration config() {
        return config;
    }
    
    @Override
    public List<String> generateInvariant() throws GenerationException {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> generateLocalInvariant() throws GenerationException {
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
}

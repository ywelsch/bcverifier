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

public abstract class AbstractGenerator implements Generator {
    private Reader reader;
    
    public AbstractGenerator(Reader reader) {
        this.reader = reader;
    }
    
    public AbstractGenerator(File file) throws FileNotFoundException {
        this.reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
    }
    
    public AbstractGenerator(String s) {
        this.reader = new StringReader(s);
    }
    
    protected Reader getReader() {
        return reader;
    }
    
    @Override
    public List<String> generateInvariant() {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> generateLocalInvariant() {
        return Collections.emptyList();
    }
    
    @Override
    public List<String> generatePreconditions() {
        return Collections.emptyList();
    }
    
    @Override
    public LocalPlaceDefinitions generateLocalPlaces() {
        return new LocalPlaceDefinitions(Collections.<Integer,List<Place>>emptyMap(), Collections.<Integer,List<Place>>emptyMap());
    }
}

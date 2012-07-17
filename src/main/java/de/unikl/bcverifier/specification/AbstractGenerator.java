package de.unikl.bcverifier.specification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

public abstract class AbstractGenerator {
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
}

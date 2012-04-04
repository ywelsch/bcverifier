package de.unikl.bcverifier.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Library {
    private Map<String, ClassNode> classes;
    
    public Library(){
        classes = new HashMap<String, ClassNode>();
    }
    
    /**
     * Load all classes from given directory
     * @param directory the base directory the package of the library is in
     * @throws IOException something went wrong when loading the classes of the library
     */
    public void load(File directory) throws IOException{
        classes.clear();
        
        Collection<File> classFiles = FileUtils.listFiles(directory, new String[]{"class"}, true);
        for(File file : classFiles){
            ClassReader creader = new ClassReader(new FileInputStream(file));
            ClassNode cn = new ClassNode();
            creader.accept(cn, 0);
            classes.put(cn.name, cn);
        }
    }
    
    /**
     * Get a classes for a name.
     * @param name the name of the classes (for example as returned by class.name
     * @return the class of the given name or <b>null</b> if the class is not in the library
     */
    public ClassNode getClass(String name){
        return classes.get(name);
    }
    
    /**
     * Get all classes of this library
     * @return all classes of this library
     */
    public Collection<ClassNode> getAllClasses(){
        return classes.values();
    }
}

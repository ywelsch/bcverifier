package de.unikl.bcverifier;

import java.io.File;
import java.util.Collection;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.commons.io.FileUtils;

public class LibraryCompiler {
    public static class CompileException extends Exception{
        private static final long serialVersionUID = 4817956211311407538L;

        public CompileException(String msg) {
            super(msg);
        }
    }
    
    public static void compile(File library) throws CompileException{
        File oldLibraryVersion = new File(library, "old");
        File newLibraryVersion = new File(library, "new");

        compileLibraryVersion(oldLibraryVersion);
        compileLibraryVersion(newLibraryVersion);
    }

    @SuppressWarnings("restriction")
    private static void compileLibraryVersion(File libraryVersion)
            throws CompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        Collection<File> javaFiles = FileUtils.listFiles(libraryVersion, new String[]{"java"}, true);
        Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = standardFileManager.getJavaFileObjectsFromFiles(javaFiles);
        CompilationTask compilationTask = compiler.getTask(null, standardFileManager, null, null, null, javaFileObjectsFromFiles);
        if(!compilationTask.call()){
            throw new CompileException("Files could not be compiled.");
        }
    }
}

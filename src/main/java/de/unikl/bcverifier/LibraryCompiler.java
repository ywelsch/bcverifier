package de.unikl.bcverifier;

import java.io.File;
import java.util.Arrays;
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

    @SuppressWarnings("restriction")
    public static void compile(File libraryPath) throws CompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        Collection<File> javaFiles = FileUtils.listFiles(libraryPath, new String[]{"java"}, true);
        Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = standardFileManager.getJavaFileObjectsFromFiles(javaFiles);
        CompilationTask compilationTask = compiler.getTask(null, standardFileManager, null, Arrays.asList("-g"), null, javaFileObjectsFromFiles);
        if(!compilationTask.call()){
            throw new CompileException("Files could not be compiled.");
        }
    }
}

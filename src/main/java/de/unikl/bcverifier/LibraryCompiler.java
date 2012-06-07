package de.unikl.bcverifier;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class LibraryCompiler {
    public static class CompileException extends Exception{
        private static final long serialVersionUID = 4817956211311407538L;

        public CompileException(String msg) {
            super(msg);
        }
    }

    private static final String DEFAULT_PREFIX = "-source 5 -target 5 -g -nowarn -noExit ";
    
    public static void compile(File libraryPath) throws CompileException {
    	StringWriter outWriter = new StringWriter();
    	StringWriter errWriter = new StringWriter();
        boolean res = BatchCompiler.compile(DEFAULT_PREFIX + libraryPath.getAbsolutePath(), new PrintWriter(outWriter), new PrintWriter(errWriter), null);
        if (!res) {
        	String errorString = errWriter.toString();
            throw new CompileException("Files could not be compiled\n" + errorString);
        }
    }
}

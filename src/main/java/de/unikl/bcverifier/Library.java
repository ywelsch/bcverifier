package de.unikl.bcverifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import b2bpl.CompilationAbortedException;
import b2bpl.Project;
import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bytecode.ITroubleReporter;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.TroubleDescription;
import b2bpl.bytecode.TroubleMessage;
import b2bpl.bytecode.TroublePosition;
import b2bpl.bytecode.TypeLoader;
import b2bpl.bytecode.analysis.SemanticAnalyzer;
import b2bpl.translation.CodeGenerator;
import b2bpl.translation.Translator;

import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;

public class Library implements ITroubleReporter{
    public static class TranslationException extends Exception {
        private static final long serialVersionUID = 1501139187899875010L;

        public TranslationException(String msg) {
            super(msg);
        }
        
        public TranslationException(String msg, Throwable t) {
            super(msg, t);
        }
    }
    
    private static final Logger log = Logger.getLogger(Library.class);
    
    private File libraryPath;
    private File oldVersionPath;
    private File newVersionPath;
    
    public Library(File libPath) {
        this.libraryPath = libPath;
        this.oldVersionPath = new File(libPath, "old");
        this.newVersionPath = new File(libPath, "new");
    }
    
    public void compile(){
        try {
            LibraryCompiler.compile(oldVersionPath);
            LibraryCompiler.compile(newVersionPath);
        } catch (CompileException e) {
            e.printStackTrace();
        }
    }
    
    public void translate() throws TranslationException{
        File bplPath = new File(libraryPath, "bpl");
        bplPath.mkdir();
        
        File oldSpecification = new File(bplPath, "old.bpl");
        File newSpecification = new File(bplPath, "new.bpl");
        
        Collection<File> oldClassFiles = FileUtils.listFiles(oldVersionPath, new String[]{"class"}, true);
        String[] oldFileNames = new String[oldClassFiles.size()+2];
        oldFileNames[0] = "-basedir";
        oldFileNames[1] = oldVersionPath.getAbsolutePath();
        int i = 2;
        for(File file : oldClassFiles){
            oldFileNames[i] = oldVersionPath.toURI().relativize(file.toURI()).getPath();
            i++;
        }
        
        Collection<File> newClassFiles = FileUtils.listFiles(newVersionPath, new String[]{"class"}, true);
        String[] newFileNames = new String[newClassFiles.size()+2];
        newFileNames[0] = "-basedir";
        newFileNames[1] = newVersionPath.getAbsolutePath();
        i = 2;
        for(File file : newClassFiles){
            newFileNames[i] = newVersionPath.toURI().relativize(file.toURI()).getPath();
            i++;
        }
        
        try {
            compileSpecification(oldFileNames, oldSpecification);
            compileSpecification(newFileNames, newSpecification);
        } catch (FileNotFoundException e) {
            throw new TranslationException("Could not write boogie specification to file.", e);
        }
    }
    
    private void compileSpecification(String[] fileNames, File outFile) throws FileNotFoundException {
        Project project = Project.fromCommandLine(fileNames, new PrintWriter(System.out));
        CodeGenerator.setProject(project);
        
        TypeLoader.setProject(project);
        TypeLoader.setProjectTypes(project.getProjectTypes());
        TypeLoader.setSpecificationProvider(project.getSpecificationProvider());
        TypeLoader.setSemanticAnalyzer(new SemanticAnalyzer(project, this));
        TypeLoader.setTroubleReporter(this);
        
        String[] projectTypeNames = project.getProjectTypes();
        JClassType[] projectTypes = new JClassType[projectTypeNames.length];
        for (int j = 0; j < projectTypes.length; j++) { 
          projectTypes[j] = TypeLoader.getClassType(projectTypeNames[j]);
        }
        
        BPLProgram program = new Translator(project).translate(projectTypes);
        
//        System.out.println(program);
        log.debug("Writing specification to file "+outFile);
        PrintWriter writer = new PrintWriter(outFile);
        program.accept(new BPLPrinter(writer));
        writer.close();
    }
    
    public void check(boolean verify){
        File bplPath = new File(libraryPath, "bpl");
        File oldSpecification = new File(bplPath, "old.bpl");
        File newSpecification = new File(bplPath, "new.bpl");
        
        BoogieRunner.setVerify(verify);
        try {
            System.out.println(BoogieRunner.runBoogie(oldSpecification));
            if(BoogieRunner.getLastReturn()){
                log.debug("Success");
            } else {
                log.debug("Error");
            }
        } catch (BoogieRunException e) {
            e.printStackTrace();
        }
        
        try {
            System.out.println(BoogieRunner.runBoogie(newSpecification));
            if(BoogieRunner.getLastReturn()){
                log.debug("Success");
            } else {
                log.debug("Error");
            }
        } catch (BoogieRunException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * copied from b2bpl.Main.reportTrouble()
     * @see b2bpl.bytecode.ITroubleReporter#reportTrouble(b2bpl.bytecode.TroubleMessage)
     */
    public void reportTrouble(TroubleMessage message) {
        String msg = "";

        TroublePosition position = message.getPosition();
        if (position != null) {
          if (position.getClassType() != null) {
            msg += position.getClassType().getName() + ":";
          }
          if (position.getMethod() != null) {
            msg += position.getMethod().getName() + ":";
          }
          if (position.getInstruction() != null) {
            msg += position.getInstruction().getIndex() + ":";
          }
          if (msg.length() > 0) {
            msg += " ";
          }
        }
        
        msg += message.getDescriptionString();
        
        switch (message.getDescription().getKind()) {
          case ERROR:
            log.error(msg);
            break;
          case WARNING:
            log.warn(msg);
            break;
        }

        if (message.getDescription().getKind() == TroubleDescription.Kind.ERROR) {
          throw new CompilationAbortedException();
        }
      }
}

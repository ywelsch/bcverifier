package de.unikl.bcverifier;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import b2bpl.CompilationAbortedException;
import b2bpl.Project;
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

public class B2BPLTest implements ITroubleReporter{
    private File libraryDir;
    
    public B2BPLTest(File libraryDir) {
        this.libraryDir = libraryDir;
    }
    
    public void parse(){
        File oldLibraryDir = new File(libraryDir, "old");
        assert oldLibraryDir.exists() && oldLibraryDir.isDirectory();
        File newLibraryDir = new File(libraryDir, "new");
        assert newLibraryDir.exists() && newLibraryDir.isDirectory();

        Collection<File> oldClassFiles = FileUtils.listFiles(oldLibraryDir, new String[]{"class"}, true);
        String[] oldFileNames = new String[oldClassFiles.size()+2];
        oldFileNames[0] = "-basedir";
        oldFileNames[1] = oldLibraryDir.getAbsolutePath();
        int i = 2;
        for(File file : oldClassFiles){
            oldFileNames[i] = oldLibraryDir.toURI().relativize(file.toURI()).getPath();
//            oldFileNames[i] = file.getPath();
            i++;
        }
        
        Collection<File> newClassFiles = FileUtils.listFiles(newLibraryDir, new String[]{"class"}, true);
        String[] newFileNames = new String[newClassFiles.size()+2];
        newFileNames[0] = "-basedir";
        newFileNames[1] = newLibraryDir.getAbsolutePath();
        i = 2;
        for(File file : newClassFiles){
            newFileNames[i] = file.getPath();
            i++;
        }
        
        Project project = Project.fromCommandLine(oldFileNames, new PrintWriter(System.out));
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
        
        System.out.println(program);
        
//        program.accept(new BPLPrinter(new PrintWriter(System.out)));
    }
    
    public static void main(String[] args) throws IOException {
        
        String libraryPath = args[0];
        System.out.println("Working Directory = " + libraryPath);
        
        File libraryDir = new File(libraryPath);
        B2BPLTest test = new B2BPLTest(libraryDir);
        test.parse();
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

        switch (message.getDescription().getKind()) {
          case ERROR:
            msg += "[Error]";
            break;
          case WARNING:
            msg += "[Warning]";
            break;
        }
        msg += " ";

        msg += message.getDescriptionString();

        System.err.println(msg);

        if (message.getDescription().getKind() == TroubleDescription.Kind.ERROR) {
          throw new CompilationAbortedException();
        }
      }
}

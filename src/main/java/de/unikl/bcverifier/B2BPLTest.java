package de.unikl.bcverifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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

public class B2BPLTest implements ITroubleReporter{
    private static final Logger log = Logger.getLogger(B2BPLTest.class);
    
    private File libraryDir;
    
    public B2BPLTest(File libraryDir) {
        this.libraryDir = libraryDir;
    }
    
    public void parse() throws FileNotFoundException{
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
            newFileNames[i] = newLibraryDir.toURI().relativize(file.toURI()).getPath();
//            newFileNames[i] = file.getPath();
            i++;
        }
        

        File oldSpecification = new File(libraryDir, "old.bpl");
        File newSpecification = new File(libraryDir, "new.bpl");
        compileSpecification(oldFileNames, oldSpecification);
        compileSpecification(newFileNames, newSpecification);
    }

    private void compileSpecification(String[] fileNames, File outFile)
            throws FileNotFoundException {
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
    
    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("log4j.properties");
        if(args.length == 0 || args.length > 2){
            System.out.println("Wrong usage. 'Library-dir' or '-a root-dir' as parameter.");
        } else {
            if(args[0].equals("-a")){
                File rootDir = new File(args[1]);
                log.debug("Parsing all libraries in "+rootDir);
                for(String path : rootDir.list(DirectoryFileFilter.DIRECTORY)){
                    log.debug("Parsing library in "+path);
                    B2BPLTest test = new B2BPLTest(new File(rootDir, path));
                    test.parse();
                }
            } else {
                String libraryPath = args[0];
                log.debug("Working Directory = " + libraryPath);

                File libraryDir = new File(libraryPath);
                B2BPLTest test = new B2BPLTest(libraryDir);
                test.parse();
            }
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

        log.error(msg);

        if (message.getDescription().getKind() == TroubleDescription.Kind.ERROR) {
          throw new CompilationAbortedException();
        }
      }
}

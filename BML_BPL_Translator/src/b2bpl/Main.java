package b2bpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.transformation.IBPLTransformator;
import b2bpl.bytecode.ITroubleReporter;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.TroubleDescription;
import b2bpl.bytecode.TroubleException;
import b2bpl.bytecode.TroubleMessage;
import b2bpl.bytecode.TroublePosition;
import b2bpl.bytecode.TypeLoader;
import b2bpl.bytecode.analysis.SemanticAnalyzer;
import b2bpl.translation.Translator;


public class Main implements ITroubleReporter {

  private static Project project = null;

  public Main(String[] args) {
    this(Project.fromCommandLine(args, new PrintWriter(System.out)));
  }

  public Main(Project project) {
    Main.project = project;
  }

  public static void main(String[] args) {
    new Main(args).compile();
    System.exit(0);
  }

  public void compile() {
    if (project == null) {
      return;
    }

    try {
      try {
        TypeLoader.setProject(project);
        TypeLoader.setProjectTypes(project.getProjectTypes());
        TypeLoader.setSpecificationProvider(project.getSpecificationProvider());
        TypeLoader.setSemanticAnalyzer(new SemanticAnalyzer(project, this));
        TypeLoader.setTroubleReporter(this);

        String[] projectTypeNames = project.getProjectTypes();
        JClassType[] projectTypes = new JClassType[projectTypeNames.length];
        for (int i = 0; i < projectTypes.length; i++) { 
          projectTypes[i] = TypeLoader.getClassType(projectTypeNames[i]);
        }

        if (project.isTranslateSeparately()) {
          for (int i = 0; i < projectTypes.length; i++) {
            translate(project.getSeparateOutFile(i), projectTypes[i]);
          }
        } else {
          translate(project.getOutFile(), projectTypes);
        }
      } catch (TroubleException te) {
        reportTrouble(te.getTroubleMessage());
      } catch (CompilationAbortedException cae) {
        // do nothing
      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (CompilationAbortedException cae) {
      // do nothing
    }
  }

  private void translate(String outFile, JClassType... types) {
    BPLProgram program = new Translator(project).translate(types);

    for (IBPLTransformator transformator : project.getTransformators()) {
      program = transformator.transform(program);
    }

    try {
      PrintWriter writer;
      if ("-".equals(outFile)) {
        writer = new PrintWriter(System.out);
      } else {
        writer = new PrintWriter(new FileOutputStream(outFile));
      }
      program.accept(new BPLPrinter(writer));
      writer.flush();
      writer.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

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
  
  public static Project getProject() {
    return Main.project;
  }
}

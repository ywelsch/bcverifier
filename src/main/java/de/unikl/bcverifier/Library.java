package de.unikl.bcverifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.KeyStore.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import b2bpl.CompilationAbortedException;
import b2bpl.Project;
import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLBuiltInType;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLConstantDeclaration;
import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLGotoCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLModifiesClause;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLReturnCommand;
import b2bpl.bpl.ast.BPLSpecification;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableDeclaration;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.ITroubleReporter;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.TroubleDescription;
import b2bpl.bytecode.TroubleMessage;
import b2bpl.bytecode.TroublePosition;
import b2bpl.bytecode.TypeLoader;
import b2bpl.bytecode.analysis.SemanticAnalyzer;
import b2bpl.translation.CodeGenerator;
import b2bpl.translation.MethodTranslator;
import b2bpl.translation.Translator;
import static b2bpl.translation.ITranslationConstants.*;
import static b2bpl.translation.CodeGenerator.*;

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
    
    private BPLExpression stack1(BPLExpression exp){
        return new BPLArrayExpression(new BPLArrayExpression(var("stack1"), var("sp1")), exp);
    }
    
    private BPLExpression stack2(BPLExpression exp){
        return new BPLArrayExpression(new BPLArrayExpression(var("stack2"), var("sp2")), exp);
    }
    
    private BPLExpression related(BPLExpression exp1, BPLExpression exp2){
        return new BPLArrayExpression(var("related"), exp1, exp2);
    }
    
    public void translate() throws TranslationException{
        File bplPath = new File(libraryPath, "bpl");
        bplPath.mkdir();
        
        File specificationFile = new File(bplPath, "specification.bpl");
        
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
            List<BPLDeclaration> programDecls = new ArrayList<BPLDeclaration>();
            List<BPLBasicBlock> methodBlocks = new ArrayList<BPLBasicBlock>();
            
            
            
            Project project = Project.fromCommandLine(oldFileNames, new PrintWriter(System.out));
            CodeGenerator.setProject(project);
            
            TypeLoader.setProject(project);
            TypeLoader.setProjectTypes(project.getProjectTypes());
            TypeLoader.setSpecificationProvider(project.getSpecificationProvider());
            TypeLoader.setSemanticAnalyzer(new SemanticAnalyzer(project, this));
            TypeLoader.setTroubleReporter(this);
            
            programDecls.addAll(new Translator(project).getPrelude()); //TODO workaround to generate Prelude
            
            
            
            TranslationController.activate();
            
            TranslationController.enterRound1();
            LibraryDefinition libraryDefinition1 = compileSpecification(oldFileNames);
            programDecls.addAll(libraryDefinition1.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition1.getMethodBlocks());
            
            TranslationController.enterRound2();
            LibraryDefinition libraryDefinition2 = compileSpecification(newFileNames);
            programDecls.addAll(libraryDefinition2.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition2.getMethodBlocks());
            

            methodBlocks.add(new BPLBasicBlock(TranslationController.getCheckLabel(), new BPLCommand[0], new BPLReturnCommand()));
            
            
            for(BPLVariable var : TranslationController.stackVariables().values()){
                programDecls.add(new BPLConstantDeclaration(var));
            }
            
            for(String place : TranslationController.places()){
                programDecls.add(new BPLConstantDeclaration(new BPLVariable(place, new BPLTypeName(ADDRESS_TYPE))));
            }
            
            List<BPLVariableDeclaration> localVariables = new ArrayList<BPLVariableDeclaration>();
            BPLVariable[] inParams = new BPLVariable[0];
            BPLVariable[] outParams = new BPLVariable[0];
            
            for(BPLVariable var : TranslationController.usedVariables().values()){
                localVariables.add(new BPLVariableDeclaration(var));
            }
            
            List<BPLCommand> procAssumes = new ArrayList<BPLCommand>();
            procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var("isCall")), stack2(var("isCall")))));
            procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var("meth")), stack2(var("meth")))));
            procAssumes.add(new BPLAssumeCommand(implies(stack1(var("isCall")), related(stack1(var("receiver")), stack2(var("receiver"))))));
            procAssumes.add(new BPLAssumeCommand(implies(stack1(var("isCall")), logicalAnd(isEqual(var("sp1"), new BPLIntLiteral(0)), isEqual(var("sp2"), new BPLIntLiteral(0))))));
            procAssumes.add(new BPLAssumeCommand(implies(logicalNot(stack1(var("isCall"))), logicalAnd(greaterEqual(var("sp1"), new BPLIntLiteral(0)), greaterEqual(var("sp2"), new BPLIntLiteral(0))))));
            //TODO add parameters
            
            methodBlocks.add(0, new BPLBasicBlock("preconditions", procAssumes.toArray(new BPLCommand[procAssumes.size()]), new BPLGotoCommand(methodBlocks.get(0).getLabel())));
            
            String methodName = "checkLibraries";
            BPLImplementationBody methodBody = new BPLImplementationBody(localVariables.toArray(new BPLVariableDeclaration[localVariables.size()]), methodBlocks.toArray(new BPLBasicBlock[methodBlocks.size()]));
            BPLImplementation methodImpl = new BPLImplementation(methodName, inParams, outParams, methodBody);
            programDecls.add(new BPLProcedure(methodName, inParams, outParams, new BPLSpecification(
                    new BPLModifiesClause(
                            new BPLVariableExpression("heap1"),
                            new BPLVariableExpression("heap2"),
                            new BPLVariableExpression("stack1"),
                            new BPLVariableExpression("stack2"),
                            new BPLVariableExpression("sp1"),
                            new BPLVariableExpression("sp2"),
                            new BPLVariableExpression("related")
                            )
                    ), methodImpl));
            BPLProgram program = new BPLProgram(programDecls.toArray(new BPLDeclaration[programDecls.size()]));
            
            log.debug("Writing specification to file "+specificationFile);
            PrintWriter writer = new PrintWriter(specificationFile);
            program.accept(new BPLPrinter(writer));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new TranslationException("Could not write boogie specification to file.", e);
        }
    }
    
    private LibraryDefinition compileSpecification(String[] fileNames) throws FileNotFoundException {
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
        
        Translator translator = new Translator(project);
        Map<String, BPLProcedure> procedures = translator.translateMethods(projectTypes);
        List<BPLDeclaration> programDecls = new ArrayList<BPLDeclaration>();
        programDecls.addAll(translator.getNeededDeclarations());
        
        int maxLocals = 0, maxStack = 0;
        List<BPLBasicBlock> methodBlocks = new ArrayList<BPLBasicBlock>();
        BPLProcedure proc;
        List<String> methodLabels = new ArrayList<String>();
        String methodLabel;
        for(JClassType classType : projectTypes){
            for(BCMethod method : classType.getMethods()){
                if (!method.isAbstract()
                        && !method.isNative()
                        && !method.isSynthetic()) {
                    log.debug("Adding "+method.getQualifiedBoogiePLName());
                    proc = procedures.get(method.getQualifiedBoogiePLName());
                    maxLocals = Math.max(maxLocals, method.getMaxLocals());
                    maxStack = Math.max(maxStack, method.getMaxStack());
                    
                    for(BPLVariableDeclaration varDecl : proc.getImplementation().getBody().getVariableDeclarations()){
                        for(BPLVariable var : varDecl.getVariables()){
                            if(var.getType().isTypeName() && ((BPLTypeName)var.getType()).getName().equals(VAR_TYPE)){
                                TranslationController.stackVariables().put(var.getName(), var);
                            } else {
                                TranslationController.usedVariables().put(var.getName(), var);
                            }
                        }
                    }
                    for(BPLVariable outParam : proc.getOutParameters()){
                        TranslationController.usedVariables().put(outParam.getName(), outParam);
                    }
                    
                    methodLabel = TranslationController.prefix(proc.getName());
                    methodLabels.add(methodLabel);
                    
                    List<BPLCommand> preMethodCommands = new ArrayList<BPLCommand>();
                    preMethodCommands.add(new BPLAssumeCommand(
                                        logicalAnd(
                                        isEqual(stack(var("place")), var(TranslationController.buildPlace(proc.getName(), true))),
                                        isEqual(
                                                stack(var("meth")) ,
                                                var(GLOBAL_VAR_PREFIX+MethodTranslator.getMethodName(method))
                                                ),
                                        //the method is callable from the type of the "this" variable on the stack
                                        isCallable(typ(stack(var(PARAM_VAR_PREFIX + "0" + REF_TYPE_ABBREV))), var(GLOBAL_VAR_PREFIX+MethodTranslator.getMethodName(method)))
                                        )
                                    ));
                    
                    methodBlocks.add(new BPLBasicBlock(methodLabel,
                            preMethodCommands.toArray(new BPLCommand[preMethodCommands.size()]),
                            new BPLGotoCommand(proc.getImplementation().getBody().getBasicBlocks()[0].getLabel())));
                    Collections.addAll(methodBlocks, proc.getImplementation().getBody().getBasicBlocks());
                }
            }
        }
        
        methodBlocks.add(0, new BPLBasicBlock(TranslationController.getDispatchLabel(), new BPLCommand[0], new BPLGotoCommand(methodLabels.toArray(new String[methodLabels.size()]))));
        
        return new LibraryDefinition(programDecls, methodBlocks);
    }
    
    public void check(boolean verify){
        File bplPath = new File(libraryPath, "bpl");
        File specificationFile = new File(bplPath, "specification.bpl");
        
        BoogieRunner.setVerify(verify);
        try {
            log.info("Checking "+specificationFile);
            System.out.println(BoogieRunner.runBoogie(specificationFile));
            if(BoogieRunner.getLastReturn()){
                log.debug("Success");
            } else {
                log.debug("Error");
            }
        } catch (BoogieRunException e) {
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

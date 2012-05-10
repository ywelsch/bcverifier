package de.unikl.bcverifier;

import static b2bpl.translation.CodeGenerator.exists;
import static b2bpl.translation.CodeGenerator.forall;
import static b2bpl.translation.CodeGenerator.greater;
import static b2bpl.translation.CodeGenerator.greaterEqual;
import static b2bpl.translation.CodeGenerator.heap1;
import static b2bpl.translation.CodeGenerator.heap2;
import static b2bpl.translation.CodeGenerator.ifThenElse;
import static b2bpl.translation.CodeGenerator.implies;
import static b2bpl.translation.CodeGenerator.isCallable;
import static b2bpl.translation.CodeGenerator.isEqual;
import static b2bpl.translation.CodeGenerator.isEquiv;
import static b2bpl.translation.CodeGenerator.logicalAnd;
import static b2bpl.translation.CodeGenerator.logicalNot;
import static b2bpl.translation.CodeGenerator.logicalOr;
import static b2bpl.translation.CodeGenerator.memberOf;
import static b2bpl.translation.CodeGenerator.nonNull;
import static b2bpl.translation.CodeGenerator.receiver;
import static b2bpl.translation.CodeGenerator.relNull;
import static b2bpl.translation.CodeGenerator.related;
import static b2bpl.translation.CodeGenerator.stack;
import static b2bpl.translation.CodeGenerator.stack1;
import static b2bpl.translation.CodeGenerator.stack1old;
import static b2bpl.translation.CodeGenerator.stack2;
import static b2bpl.translation.CodeGenerator.stack2old;
import static b2bpl.translation.CodeGenerator.typ;
import static b2bpl.translation.CodeGenerator.var;
import static b2bpl.translation.ITranslationConstants.ADDRESS_TYPE;
import static b2bpl.translation.ITranslationConstants.GLOBAL_VAR_PREFIX;
import static b2bpl.translation.ITranslationConstants.HAS_RETURN_VALUE_FUNC;
import static b2bpl.translation.ITranslationConstants.INT_TYPE_ABBREV;
import static b2bpl.translation.ITranslationConstants.IS_PUBLIC_FUNC;
import static b2bpl.translation.ITranslationConstants.METHOD_TYPE;
import static b2bpl.translation.ITranslationConstants.PARAM_VAR_PREFIX;
import static b2bpl.translation.ITranslationConstants.REF_TYPE;
import static b2bpl.translation.ITranslationConstants.REF_TYPE_ABBREV;
import static b2bpl.translation.ITranslationConstants.RESULT_VAR;
import static b2bpl.translation.ITranslationConstants.STACK_PTR_TYPE;
import static b2bpl.translation.ITranslationConstants.VAR_TYPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import b2bpl.CompilationAbortedException;
import b2bpl.Project;
import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssignmentCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLAxiom;
import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLConstantDeclaration;
import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLGotoCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLModifiesClause;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLReturnCommand;
import b2bpl.bpl.ast.BPLSpecification;
import b2bpl.bpl.ast.BPLTransferCommand;
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
import b2bpl.translation.ITranslationConstants;
import b2bpl.translation.MethodTranslator;
import b2bpl.translation.Translator;
import de.unikl.bcverifier.LibraryCompiler.CompileException;
import de.unikl.bcverifier.boogie.BoogieRunner;
import de.unikl.bcverifier.boogie.BoogieRunner.BoogieRunException;

public class Library implements ITroubleReporter {
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

    public void compile() {
        try {
            LibraryCompiler.compile(oldVersionPath);
            LibraryCompiler.compile(newVersionPath);
        } catch (CompileException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws TranslationException
     */
    /**
     * @throws TranslationException
     */
    /**
     * @throws TranslationException
     */
    public void translate() throws TranslationException {
        File bplPath = new File(libraryPath, "bpl");
        bplPath.mkdir();

        File invFile = new File(bplPath, "inv.bpl");
        List<String> invariants;
        try {
            invariants = FileUtils.readLines(invFile, "UTF-8");
        } catch (IOException ex) {
            invariants = new ArrayList<String>();
        }
        ArrayList<BPLCommand> invAssertions = new ArrayList<BPLCommand>();
        ArrayList<BPLCommand> invAssumes = new ArrayList<BPLCommand>();
        BPLCommand cmd;
        for (String inv : invariants) {
            if (inv.length() > 0 && !inv.matches("\\/\\/.*")) {
                cmd = new BPLAssertCommand(var(inv));
                cmd.addComment("invariant");
                invAssertions.add(cmd);
                cmd = new BPLAssumeCommand(var(inv));
                cmd.addComment("invariant");
                invAssumes.add(cmd);
            }
        }

        File specificationFile = new File(bplPath, "specification.bpl");

        Collection<File> oldClassFiles = FileUtils.listFiles(oldVersionPath,
                new String[] { "class" }, true);
        String[] oldFileNames = new String[oldClassFiles.size() + 2];
        oldFileNames[0] = "-basedir";
        oldFileNames[1] = oldVersionPath.getAbsolutePath();
        int i = 2;
        for (File file : oldClassFiles) {
            oldFileNames[i] = oldVersionPath.toURI().relativize(file.toURI())
                    .getPath();
            i++;
        }

        Collection<File> newClassFiles = FileUtils.listFiles(newVersionPath,
                new String[] { "class" }, true);
        String[] newFileNames = new String[newClassFiles.size() + 2];
        newFileNames[0] = "-basedir";
        newFileNames[1] = newVersionPath.getAbsolutePath();
        i = 2;
        for (File file : newClassFiles) {
            newFileNames[i] = newVersionPath.toURI().relativize(file.toURI())
                    .getPath();
            i++;
        }

        try {
            List<BPLDeclaration> programDecls = new ArrayList<BPLDeclaration>();
            List<BPLBasicBlock> methodBlocks = new ArrayList<BPLBasicBlock>();

            Project project = Project.fromCommandLine(oldFileNames,
                    new PrintWriter(System.out));
            CodeGenerator.setProject(project);

            TypeLoader.setProject(project);
            TypeLoader.setProjectTypes(project.getProjectTypes());
            TypeLoader.setSpecificationProvider(project
                    .getSpecificationProvider());
            TypeLoader.setSemanticAnalyzer(new SemanticAnalyzer(project, this));
            TypeLoader.setTroubleReporter(this);

            programDecls.addAll(new Translator(project).getPrelude()); // TODO
                                                                       // workaround
                                                                       // to
                                                                       // generate
                                                                       // Prelude

            TranslationController.activate();

            TranslationController.enterRound1();
            LibraryDefinition libraryDefinition1 = compileSpecification(oldFileNames);
            programDecls.addAll(libraryDefinition1.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition1.getMethodBlocks());

            TranslationController.enterRound2();
            LibraryDefinition libraryDefinition2 = compileSpecification(newFileNames);
            programDecls.addAll(libraryDefinition2.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition2.getMethodBlocks());

            // insert all method definition axioms
            for (String className : TranslationController.methodDefinitions()
                    .keySet()) {
                Set<String> methodNames = TranslationController
                        .methodDefinitions().get(className);
                List<BPLExpression> methodExprs = new ArrayList<BPLExpression>();

                String m = "m";
                BPLVariable mVar = new BPLVariable(m, new BPLTypeName(
                        METHOD_TYPE));
                for (String methodName : methodNames) {
                    methodExprs.add(isEqual(var(m), var(methodName)));
                }
                programDecls.add(new BPLAxiom(forall(
                        mVar,
                        isEquiv(new BPLFunctionApplication("definesMethod",
                                var(className), var(m)),
                                logicalOr(methodExprs
                                        .toArray(new BPLExpression[methodExprs
                                                .size()]))))));
            }

            // ///////////////////////////////////
            // checking blocks (intern and extern)
            // ///////////////////////////////////
            methodBlocks.add(new BPLBasicBlock(TranslationController
                    .getCheckLabel(), new BPLCommand[0], new BPLGotoCommand(
                    "check_intern", "check_extern")));

            // ////////////////////////////////
            // assertions of the intern block
            // ///////////////////////////////
            List<BPLCommand> checkingCommand = new ArrayList<BPLCommand>();
            checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                    isEqual(var("sp1"), new BPLIntLiteral(-1)),
                    isEqual(var("sp2"), new BPLIntLiteral(-1)))));

            checkingCommand.add(new BPLAssertCommand(isEqual(
                    stack1old(var("meth")), stack2old(var("meth")))));
            checkingCommand.add(new BPLAssertCommand(logicalAnd(
                    nonNull(stack1old(receiver())),
                    nonNull(stack2old(receiver())))));

            // //TODO only do this if it is a call (sp1 == -1?)
            // checkingCommand.add(new BPLAssertCommand(logicalOr(
            // relNull(stack1old(receiver()), stack2old(receiver()),
            // var("related")),
            // logicalAnd(
            // logicalNot(exists(new BPLVariable("r", new
            // BPLTypeName(REF_TYPE)), related(stack1old(receiver()),
            // var("r")))),
            // logicalNot(exists(new BPLVariable("r", new
            // BPLTypeName(REF_TYPE)), related(var("r"),
            // stack2old(receiver()))))
            // )
            // )));
            // checkingCommand.add(new
            // BPLAssignmentCommand(heap1(stack1old(receiver()),
            // var("exposed")), BPLBoolLiteral.TRUE));
            // checkingCommand.add(new
            // BPLAssignmentCommand(heap2(stack2old(receiver()),
            // var("exposed")), BPLBoolLiteral.TRUE));
            // checkingCommand.add(new
            // BPLAssignmentCommand(related(stack1old(receiver()),
            // stack2old(receiver())), BPLBoolLiteral.TRUE));

            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    checkingCommand
                            .add(new BPLAssertCommand(
                                    logicalOr(
                                            relNull(stack1old(var(var.getName())),
                                                    stack2old(var(var.getName())),
                                                    var("related")),
                                            logicalAnd(
                                                    logicalNot(exists(
                                                            new BPLVariable(
                                                                    "r",
                                                                    new BPLTypeName(
                                                                            REF_TYPE)),
                                                            related(stack1old(var(var
                                                                    .getName())),
                                                                    var("r")))),
                                                    logicalNot(exists(
                                                            new BPLVariable(
                                                                    "r",
                                                                    new BPLTypeName(
                                                                            REF_TYPE)),
                                                            related(var("r"),
                                                                    stack2old(var(var
                                                                            .getName())))))))));
                    // if var != null ...
                    checkingCommand
                            .add(new BPLAssignmentCommand(
                                    heap1(stack1old(var(var.getName())),
                                            var("exposed")),
                                    ifThenElse(
                                            logicalAnd(
                                                    nonNull(stack1old(var(var
                                                            .getName()))),
                                                    nonNull(stack2old(var(var
                                                            .getName())))),
                                            BPLBoolLiteral.TRUE,
                                            heap1(stack1old(var(var.getName())),
                                                    var("exposed")))));
                    checkingCommand
                            .add(new BPLAssignmentCommand(
                                    heap2(stack2old(var(var.getName())),
                                            var("exposed")),
                                    ifThenElse(
                                            logicalAnd(
                                                    nonNull(stack1old(var(var
                                                            .getName()))),
                                                    nonNull(stack2old(var(var
                                                            .getName())))),
                                            BPLBoolLiteral.TRUE,
                                            heap2(stack2old(var(var.getName())),
                                                    var("exposed")))));
                    checkingCommand.add(new BPLAssignmentCommand(related(
                            stack1old(var(var.getName())),
                            stack2old(var(var.getName()))), ifThenElse(
                            logicalAnd(nonNull(stack1old(var(var.getName()))),
                                    nonNull(stack2old(var(var.getName())))),
                            BPLBoolLiteral.TRUE,
                            related(stack1old(var(var.getName())),
                                    stack2old(var(var.getName()))))));
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    checkingCommand.add(new BPLAssertCommand(isEqual(
                            stack1old(var(var.getName())),
                            stack2old(var(var.getName())))));
                }
            }

            checkingCommand.add(new BPLAssertCommand(
                    implies(new BPLFunctionApplication(HAS_RETURN_VALUE_FUNC,
                            stack1old(var("meth"))),
                            logicalOr(
                                    relNull(stack1old(var(RESULT_VAR
                                            + REF_TYPE_ABBREV)),
                                            stack2old(var(RESULT_VAR
                                                    + REF_TYPE_ABBREV)),
                                            var("related")),
                                    isEqual(stack1old(var(RESULT_VAR
                                            + INT_TYPE_ABBREV)),
                                            stack2old(var(RESULT_VAR
                                                    + INT_TYPE_ABBREV)))))));

            checkingCommand.addAll(invAssertions);
            methodBlocks.add(new BPLBasicBlock("check_intern", checkingCommand
                    .toArray(new BPLCommand[checkingCommand.size()]),
                    new BPLReturnCommand()));

            // ////////////////////////////////
            // assertions of the extern block
            // ////////////////////////////////
            checkingCommand.clear();
            checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                    greater(var("sp1"), new BPLIntLiteral(0)),
                    greater(var("sp2"), new BPLIntLiteral(0)))));

            checkingCommand.add(new BPLAssertCommand(isEqual(
                    stack1(var("meth")), stack2(var("meth")))));

            // //TODO only do this if it is a call (sp1 == -1?)
            // checkingCommand.add(new BPLAssertCommand(logicalOr(
            // relNull(stack1old(receiver()), stack2old(receiver()),
            // var("related")),
            // logicalAnd(
            // logicalNot(exists(new BPLVariable("r", new
            // BPLTypeName(REF_TYPE)), related(stack1old(receiver()),
            // var("r")))),
            // logicalNot(exists(new BPLVariable("r", new
            // BPLTypeName(REF_TYPE)), related(var("r"),
            // stack2old(receiver()))))
            // )
            // )));
            // checkingCommand.add(new
            // BPLAssignmentCommand(heap1(stack1old(receiver()),
            // var("exposed")), BPLBoolLiteral.TRUE));
            // checkingCommand.add(new
            // BPLAssignmentCommand(heap2(stack2old(receiver()),
            // var("exposed")), BPLBoolLiteral.TRUE));
            // checkingCommand.add(new
            // BPLAssignmentCommand(related(stack1old(receiver()),
            // stack2old(receiver())), BPLBoolLiteral.TRUE));

            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    checkingCommand
                            .add(new BPLAssertCommand(
                                    logicalOr(
                                            relNull(stack1(var(var.getName())),
                                                    stack2(var(var.getName())),
                                                    var("related")),
                                            logicalAnd(
                                                    logicalNot(exists(
                                                            new BPLVariable(
                                                                    "r",
                                                                    new BPLTypeName(
                                                                            REF_TYPE)),
                                                            related(stack1(var(var
                                                                    .getName())),
                                                                    var("r")))),
                                                    logicalNot(exists(
                                                            new BPLVariable(
                                                                    "r",
                                                                    new BPLTypeName(
                                                                            REF_TYPE)),
                                                            related(var("r"),
                                                                    stack2(var(var
                                                                            .getName())))))))));
                    // if var != null ...
                    checkingCommand
                            .add(new BPLAssignmentCommand(
                                    heap1(stack1(var(var.getName())),
                                            var("exposed")), ifThenElse(
                                            logicalAnd(nonNull(stack1(var(var
                                                    .getName()))),
                                                    nonNull(stack2(var(var
                                                            .getName())))),
                                            BPLBoolLiteral.TRUE,
                                            heap1(stack1(var(var.getName())),
                                                    var("exposed")))));
                    checkingCommand
                            .add(new BPLAssignmentCommand(
                                    heap2(stack2(var(var.getName())),
                                            var("exposed")), ifThenElse(
                                            logicalAnd(nonNull(stack1(var(var
                                                    .getName()))),
                                                    nonNull(stack2(var(var
                                                            .getName())))),
                                            BPLBoolLiteral.TRUE,
                                            heap2(stack2(var(var.getName())),
                                                    var("exposed")))));
                    checkingCommand.add(new BPLAssignmentCommand(related(
                            stack1(var(var.getName())),
                            stack2(var(var.getName()))), ifThenElse(
                            logicalAnd(nonNull(stack1(var(var.getName()))),
                                    nonNull(stack2(var(var.getName())))),
                            BPLBoolLiteral.TRUE,
                            related(stack1(var(var.getName())),
                                    stack2(var(var.getName()))))));
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    checkingCommand.add(new BPLAssertCommand(isEqual(
                            stack1(var(var.getName())),
                            stack2(var(var.getName())))));
                }
            }
            checkingCommand.addAll(invAssertions);
            // checkingCommand.add(new
            // BPLAssertCommand(BPLBoolLiteral.FALSE));//TODO implement checking
            // of method calls to extern

            methodBlocks.add(new BPLBasicBlock("check_extern", checkingCommand
                    .toArray(new BPLCommand[checkingCommand.size()]),
                    new BPLReturnCommand()));

            
            
            /////////////////////////////////////
            // preconditions of before checking
            ////////////////////////////////////
            List<BPLCommand> procAssumes = new ArrayList<BPLCommand>();
            procAssumes.addAll(invAssumes);
            procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var("meth")),
                    stack2(var("meth")))));
            // procAssumes.add(new
            // BPLAssumeCommand(related(stack1(var("param0_r")),
            // stack2(var("param0_r")))));
            // procAssumes.add(new
            // BPLAssumeCommand(logicalAnd(isEqual(var("sp1"), new
            // BPLIntLiteral(0)), isEqual(var("sp2"), new BPLIntLiteral(0)))));
            // procAssumes.add(new
            // BPLAssumeCommand(implies(logicalNot(stack1(var("isCall"))),
            // logicalAnd(greaterEqual(var("sp1"), new BPLIntLiteral(0)),
            // greaterEqual(var("sp2"), new BPLIntLiteral(0))))));

            // ///////////////////////////////////////
            // relate all parameters from the outside
            // ///////////////////////////////////////
            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                programDecls.add(new BPLConstantDeclaration(var));
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    // procAssumes.add(new
                    // BPLAssumeCommand(relNull(stack1(var(var.getName())),
                    // stack2(var(var.getName())), var("related"))));
                    BPLCommand assumeCmd = new BPLAssumeCommand(forall(
                            new BPLVariable("sp", new BPLTypeName(
                                    STACK_PTR_TYPE)),
                            relNull(stack1(var("sp"), var(var.getName())),
                                    stack2(var("sp"), var(var.getName())),
                                    var("related"))));
                    assumeCmd.addComment("initially, all parameters to method calls can be assumed to be related");
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(heap1(stack1(var(var.getName())), var("exposed")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(heap2(stack2(var(var.getName())), var("exposed")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(heap1(stack1(var(var.getName())), var("createdByCtxt")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(heap2(stack2(var(var.getName())), var("createdByCtxt")));
                    procAssumes.add(assumeCmd);
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    BPLCommand assumeCmd = new BPLAssumeCommand(forall(
                            new BPLVariable("sp", new BPLTypeName(
                                    STACK_PTR_TYPE)),
                            isEqual(stack1(var("sp"), var(var.getName())),
                                    stack2(var("sp"), var(var.getName())))));
                    assumeCmd
                            .addComment("initially, all parameters to method calls can be assumed to be related");
                    procAssumes.add(assumeCmd);
                }
            }

            for (String place : TranslationController.places()) {
                programDecls.add(new BPLConstantDeclaration(new BPLVariable(
                        place, new BPLTypeName(ADDRESS_TYPE))));
            }

            List<BPLVariableDeclaration> localVariables = new ArrayList<BPLVariableDeclaration>();
            BPLVariable[] inParams = new BPLVariable[0];
            BPLVariable[] outParams = new BPLVariable[0];

            for (BPLVariable var : TranslationController.usedVariables()
                    .values()) {
                localVariables.add(new BPLVariableDeclaration(var));
            }

            methodBlocks
                    .add(0,
                            new BPLBasicBlock("preconditions",
                                    procAssumes
                                            .toArray(new BPLCommand[procAssumes
                                                    .size()]),
                                    new BPLGotoCommand(methodBlocks.get(0)
                                            .getLabel())));

            String methodName = "checkLibraries";
            BPLImplementationBody methodBody = new BPLImplementationBody(
                    localVariables.toArray(new BPLVariableDeclaration[localVariables
                            .size()]),
                    methodBlocks.toArray(new BPLBasicBlock[methodBlocks.size()]));
            BPLImplementation methodImpl = new BPLImplementation(methodName,
                    inParams, outParams, methodBody);
            programDecls
                    .add(new BPLProcedure(methodName, inParams, outParams,
                            new BPLSpecification(new BPLModifiesClause(
                                    new BPLVariableExpression("heap1"),
                                    new BPLVariableExpression("heap2"),
                                    new BPLVariableExpression("stack1"),
                                    new BPLVariableExpression("stack2"),
                                    new BPLVariableExpression("sp1"),
                                    new BPLVariableExpression("sp2"),
                                    new BPLVariableExpression("related"))),
                            methodImpl));
            BPLProgram program = new BPLProgram(
                    programDecls.toArray(new BPLDeclaration[programDecls.size()]));

            log.debug("Writing specification to file " + specificationFile);
            PrintWriter writer = new PrintWriter(specificationFile);
            program.accept(new BPLPrinter(writer));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new TranslationException(
                    "Could not write boogie specification to file.", e);
        }
    }

    private LibraryDefinition compileSpecification(String[] fileNames)
            throws FileNotFoundException {
        TranslationController.resetReturnLabels();
        
        Project project = Project.fromCommandLine(fileNames, new PrintWriter(
                System.out));
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
        Map<String, BPLProcedure> procedures = translator
                .translateMethods(projectTypes);
        List<BPLDeclaration> programDecls = new ArrayList<BPLDeclaration>();
        programDecls.addAll(translator.getNeededDeclarations());

        int maxLocals = 0, maxStack = 0;
        List<BPLBasicBlock> methodBlocks = new ArrayList<BPLBasicBlock>();
        BPLProcedure proc;
        List<String> methodLabels = new ArrayList<String>();
        String methodLabel;
        for (JClassType classType : projectTypes) {
            for (BCMethod method : classType.getMethods()) {
                if (!method.isAbstract() && !method.isNative()
                        && !method.isSynthetic()) {
                    log.debug("Adding " + method.getQualifiedBoogiePLName());
                    proc = procedures.get(method.getQualifiedBoogiePLName());
                    maxLocals = Math.max(maxLocals, method.getMaxLocals());
                    maxStack = Math.max(maxStack, method.getMaxStack());

                    for (BPLVariableDeclaration varDecl : proc
                            .getImplementation().getBody()
                            .getVariableDeclarations()) {
                        for (BPLVariable var : varDecl.getVariables()) {
                            if (var.getType().isTypeName()
                                    && ((BPLTypeName) var.getType()).getName()
                                            .equals(VAR_TYPE)) {
                                TranslationController.stackVariables().put(
                                        var.getName(), var);
                            } else {
                                TranslationController.usedVariables().put(
                                        var.getName(), var);
                            }
                        }
                    }
                    for (BPLVariable outParam : proc.getOutParameters()) {
                        TranslationController.usedVariables().put(
                                outParam.getName(), outParam);
                    }

                    methodLabel = TranslationController.prefix(proc.getName());
                    methodLabels.add(methodLabel);

                    ///////////////////////////////
                    // commands before method block
                    ///////////////////////////////
                    List<BPLCommand> preMethodCommands = new ArrayList<BPLCommand>();
                    preMethodCommands.add(new BPLAssumeCommand(isEqual(
                            stack(var("place")), var(TranslationController
                                    .buildPlace(proc.getName(), true)))));
                    preMethodCommands
                            .add(new BPLAssumeCommand(isEqual(
                                    stack(var("meth")),
                                    var(GLOBAL_VAR_PREFIX
                                            + MethodTranslator
                                                    .getMethodName(method)))));
                    preMethodCommands.add(new BPLAssumeCommand(
                            memberOf(var(GLOBAL_VAR_PREFIX
                                    + MethodTranslator
                                    .getMethodName(method)), var(GLOBAL_VAR_PREFIX + classType.getName()), typ(stack(receiver()), var(TranslationController.getHeap())))
                            ));
                    
                    // preMethodCommands.add(new
                    // BPLAssumeCommand(isCallable(typ(stack(var(PARAM_VAR_PREFIX
                    // + "0" + REF_TYPE_ABBREV))),
                    // var(GLOBAL_VAR_PREFIX+MethodTranslator.getMethodName(method)))));

                    methodBlocks.add(new BPLBasicBlock(methodLabel,
                            preMethodCommands
                                    .toArray(new BPLCommand[preMethodCommands
                                            .size()]), new BPLGotoCommand(proc
                                    .getImplementation().getBody()
                                    .getBasicBlocks()[0].getLabel())));
                    Collections.addAll(methodBlocks, proc.getImplementation()
                            .getBody().getBasicBlocks());
                }
            }
        }

        // //////////////////////////////////////
        // callTable and returnTable
        // //////////////////////////////////////
        String callTableLabel = TranslationController.prefix(ITranslationConstants.CALLTABLE_LABEL);
        String callTableInitLabel = callTableLabel + "_init";
        String retTableLabel = TranslationController.prefix(ITranslationConstants.RETTABLE_LABEL);
        String retTableInitLabel = retTableLabel + "_init";
        String[] returnLabels = TranslationController.returnLabels().toArray(
                new String[TranslationController.returnLabels().size()]);

        // //////////////////////////////////////
        // commands before callTable
        // /////////////////////////////////////
        List<BPLCommand> dispatchCommands;
        dispatchCommands = new ArrayList<BPLCommand>();
        methodBlocks.add(
                0,
                new BPLBasicBlock(callTableLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        new BPLGotoCommand(methodLabels
                                .toArray(new String[methodLabels.size()]))));

        BPLTransferCommand dispatchTransferCmd;

        if (returnLabels.length > 0) {
            dispatchTransferCmd = new BPLGotoCommand(returnLabels);
        } else {
            dispatchTransferCmd = new BPLReturnCommand();
        }
        
        ///////////////////////////////////////////
        // commands before callTableInit (preconditions of the calltable)
        ///////////////////////////////////////////
        
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(isEqual(
                var(TranslationController.getStackPointer()),
                new BPLIntLiteral(0))));
        methodBlocks.add(
                0,
                new BPLBasicBlock(callTableInitLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        new BPLGotoCommand(callTableLabel)));
        
        

        // ///////////////////////////////////////
        // commands before returnTable
        // ///////////////////////////////////////
        dispatchCommands = new ArrayList<BPLCommand>();
        methodBlocks.add(
                0,
                new BPLBasicBlock(retTableLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        dispatchTransferCmd));
        
        ///////////////////////////////////////////
        // commands before returnTableInit (preconditions of the calltable)
        ///////////////////////////////////////////
        
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(greaterEqual(
                var(TranslationController.getStackPointer()),
                new BPLIntLiteral(0))));
        methodBlocks.add(
                0,
                new BPLBasicBlock(retTableInitLabel, dispatchCommands
                          .toArray(new BPLCommand[dispatchCommands.size()]),
                          new BPLGotoCommand(retTableLabel)));
        

        // //////////////////////////////////////
        // commands before dispatch
        // //////////////////////////////////////
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(new BPLFunctionApplication(
                IS_PUBLIC_FUNC, typ(stack(receiver()), var(TranslationController.getHeap())))));
        dispatchCommands.add(new BPLAssumeCommand(isCallable(
                typ(stack(receiver()), var(TranslationController.getHeap())), stack(var("meth")))));
        methodBlocks
                .add(0,
                        new BPLBasicBlock(
                                TranslationController.getDispatchLabel(),
                                dispatchCommands
                                        .toArray(new BPLCommand[dispatchCommands
                                                .size()]), new BPLGotoCommand(
                                        callTableInitLabel, retTableInitLabel)));

        return new LibraryDefinition(programDecls, methodBlocks);
    }

    public void check(boolean verify) {
        File bplPath = new File(libraryPath, "bpl");
        File specificationFile = new File(bplPath, "specification.bpl");

        BoogieRunner.setVerify(verify);
        try {
            log.info("Checking " + specificationFile);
            System.out.println(BoogieRunner.runBoogie(specificationFile));
            if (BoogieRunner.getLastReturn()) {
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

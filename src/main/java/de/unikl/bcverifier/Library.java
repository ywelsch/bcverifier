package de.unikl.bcverifier;

import static b2bpl.translation.CodeGenerator.exists;
import static b2bpl.translation.CodeGenerator.forall;
import static b2bpl.translation.CodeGenerator.greater;
import static b2bpl.translation.CodeGenerator.hasReturnValue;
import static b2bpl.translation.CodeGenerator.heap1;
import static b2bpl.translation.CodeGenerator.heap2;
import static b2bpl.translation.CodeGenerator.ifThenElse;
import static b2bpl.translation.CodeGenerator.implies;
import static b2bpl.translation.CodeGenerator.isCallable;
import static b2bpl.translation.CodeGenerator.isEqual;
import static b2bpl.translation.CodeGenerator.isEquiv;
import static b2bpl.translation.CodeGenerator.isNull;
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
import static b2bpl.translation.CodeGenerator.stack2;
import static b2bpl.translation.CodeGenerator.typ;
import static b2bpl.translation.CodeGenerator.var;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import b2bpl.bpl.ast.BPLBuiltInType;
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

public class Library implements ITroubleReporter, ITranslationConstants {
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

            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                programDecls.add(new BPLConstantDeclaration(var));
            }

            // ///////////////////////////////////
            // checking blocks (intern and extern)
            // ///////////////////////////////////
            methodBlocks.add(new BPLBasicBlock(TranslationController
                    .getCheckLabel(), new BPLCommand[0], new BPLGotoCommand(
                    "check_boundary_return", "check_boundary_call")));

            // ////////////////////////////////
            // assertions of the check return block
            // ///////////////////////////////
            List<BPLCommand> checkingCommand = new ArrayList<BPLCommand>();
            checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                    isEqual(var("sp1"), new BPLIntLiteral(0)),
                    isEqual(var("sp2"), new BPLIntLiteral(0)))));

            checkingCommand.add(new BPLAssertCommand(isEqual(
                    stack1(var("meth")), stack2(var("meth")))));
//            checkingCommand.add(new BPLAssertCommand(logicalAnd(
//                    nonNull(stack1(receiver())), nonNull(stack2(receiver())))));
//
//            for (BPLVariable var : TranslationController.stackVariables()
//                    .values()) {
//                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
//                    checkingCommand
//                            .add(new BPLAssertCommand(
//                                    logicalOr(
//                                            relNull(stack1(var(var.getName())),
//                                                    stack2(var(var.getName())),
//                                                    var("related")),
//                                            logicalAnd(
//                                                    logicalNot(exists(
//                                                            new BPLVariable(
//                                                                    "r",
//                                                                    new BPLTypeName(
//                                                                            REF_TYPE)),
//                                                            related(stack1(var(var
//                                                                    .getName())),
//                                                                    var("r")))),
//                                                    logicalNot(exists(
//                                                            new BPLVariable(
//                                                                    "r",
//                                                                    new BPLTypeName(
//                                                                            REF_TYPE)),
//                                                            related(var("r"),
//                                                                    stack2(var(var
//                                                                            .getName())))))))));
//                    // if var != null ...
//                    checkingCommand
//                            .add(new BPLAssignmentCommand(
//                                    heap1(stack1(var(var.getName())),
//                                            var("exposed")), ifThenElse(
//                                            logicalAnd(nonNull(stack1(var(var
//                                                    .getName()))),
//                                                    nonNull(stack2(var(var
//                                                            .getName())))),
//                                            BPLBoolLiteral.TRUE,
//                                            heap1(stack1(var(var.getName())),
//                                                    var("exposed")))));
//                    checkingCommand
//                            .add(new BPLAssignmentCommand(
//                                    heap2(stack2(var(var.getName())),
//                                            var("exposed")), ifThenElse(
//                                            logicalAnd(nonNull(stack1(var(var
//                                                    .getName()))),
//                                                    nonNull(stack2(var(var
//                                                            .getName())))),
//                                            BPLBoolLiteral.TRUE,
//                                            heap2(stack2(var(var.getName())),
//                                                    var("exposed")))));
//                    checkingCommand.add(new BPLAssignmentCommand(related(
//                            stack1(var(var.getName())),
//                            stack2(var(var.getName()))), ifThenElse(
//                            logicalAnd(nonNull(stack1(var(var.getName()))),
//                                    nonNull(stack2(var(var.getName())))),
//                            BPLBoolLiteral.TRUE,
//                            related(stack1(var(var.getName())),
//                                    stack2(var(var.getName()))))));
//                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
//                    checkingCommand.add(new BPLAssertCommand(isEqual(
//                            stack1(var(var.getName())),
//                            stack2(var(var.getName())))));
//                }
//            }
            
          checkingCommand.add(new BPLAssignmentCommand(
                  heap1(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                          var("exposed")), ifThenElse(
                          logicalAnd(hasReturnValue(stack1(var("meth"))),
                                  nonNull(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV))),
                                  nonNull(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)))),
                          BPLBoolLiteral.TRUE,
                          heap1(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                                  var("exposed")))));
          checkingCommand.add(new BPLAssignmentCommand(
                  heap2(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                          var("exposed")), ifThenElse(
                          logicalAnd(hasReturnValue(stack2(var("meth"))),
                                  nonNull(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV))),
                                  nonNull(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)))),
                          BPLBoolLiteral.TRUE,
                          heap2(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                                  var("exposed")))));

          checkingCommand.add(new BPLAssumeCommand(CodeGenerator.wellfomredCoupling(var(TranslationController.HEAP1), var(TranslationController.HEAP2), var("related"))));
          
            checkingCommand.add(new BPLAssertCommand(
                    implies(hasReturnValue(stack1(var("meth"))),
                            logicalOr(
                                    relNull(stack1(var(RESULT_VAR
                                            + REF_TYPE_ABBREV)),
                                            stack2(var(RESULT_VAR
                                                    + REF_TYPE_ABBREV)),
                                            var("related")),
                                    isEqual(stack1(var(RESULT_VAR
                                            + INT_TYPE_ABBREV)),
                                            stack2(var(RESULT_VAR
                                                    + INT_TYPE_ABBREV)))))));

            checkingCommand.addAll(invAssertions);
            methodBlocks.add(new BPLBasicBlock("check_boundary_return", checkingCommand
                    .toArray(new BPLCommand[checkingCommand.size()]),
                    new BPLReturnCommand()));

            // ////////////////////////////////
            // assertions of the check call block
            // ////////////////////////////////
            checkingCommand.clear();
            checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                    greater(var("sp1"), new BPLIntLiteral(0)),
                    greater(var("sp2"), new BPLIntLiteral(0)))));

            checkingCommand.add(new BPLAssertCommand(isEqual(
                    stack1(var("meth")), stack2(var("meth")))));

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

            methodBlocks.add(new BPLBasicBlock("check_boundary_call", checkingCommand
                    .toArray(new BPLCommand[checkingCommand.size()]),
                    new BPLReturnCommand()));

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

            List<BPLCommand> procAssumes;

            // ///////////////////////////////////
            // preconditions of before checking
            // //////////////////////////////////
            procAssumes = new ArrayList<BPLCommand>();
            methodBlocks.add(
                    0,
                    new BPLBasicBlock("preconditions", procAssumes
                            .toArray(new BPLCommand[procAssumes.size()]),
                            new BPLGotoCommand("preconditions_call",
                                    "preconditions_return", "preconditions_constructor")));

            BPLCommand assumeCmd;

            // //////////////////////////////////
            // preconditions of a call
            // /////////////////////////////////
            procAssumes = new ArrayList<BPLCommand>();
            procAssumes.add(new BPLAssumeCommand(isEqual(var("sp1"),
                    new BPLIntLiteral(0))));
            procAssumes.add(new BPLAssumeCommand(isEqual(var("sp2"),
                    new BPLIntLiteral(0))));

            // invariant
            procAssumes.addAll(invAssumes);

            // ///////////////////////////////////////////
            // relation between lib1 and lib2
            // ///////////////////////////////////////////
            procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var("meth")),
                    stack2(var("meth")))));

            // ///////////////////////////////////////
            // relate all parameters from the outside
            // ///////////////////////////////////////
            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    assumeCmd = new BPLAssumeCommand(relNull(
                            stack1(var(var.getName())),
                            stack2(var(var.getName())), var("related")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack1(var(var.getName()))),
                            heap1(stack1(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack2(var(var.getName()))),
                            heap2(stack2(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    assumeCmd = new BPLAssumeCommand(isEqual(
                            stack1(var(var.getName())),
                            stack2(var(var.getName()))));
                    procAssumes.add(assumeCmd);
                }
            }

            methodBlocks.add(1, new BPLBasicBlock("preconditions_call",
                    procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                    new BPLGotoCommand(TranslationController.DISPATCH_LABEL1)));

            
            // //////////////////////////////////
            // preconditions of a constructor call
            // /////////////////////////////////
            procAssumes = new ArrayList<BPLCommand>();
            procAssumes.add(new BPLAssumeCommand(isEqual(var("sp1"),
                    new BPLIntLiteral(0))));
            procAssumes.add(new BPLAssumeCommand(isEqual(var("sp2"),
                    new BPLIntLiteral(0))));

            // ///////////////////////////////////////////
            // relation between lib1 and lib2
            // ///////////////////////////////////////////
            procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var("meth")),
                    stack2(var("meth")))));

//            // the object is not yet initialized (so the fields have their default value)
//            procAssumes.add(
//                    new BPLAssumeCommand(logicalAnd(
//                            forall(new BPLVariable("f", new BPLTypeName(FIELD_TYPE, BPLBuiltInType.INT)),
//                                    logicalAnd(
//                                            isEqual(heap1(stack1(receiver()), var("f")), new BPLIntLiteral(0)),
//                                            isEqual(heap2(stack2(receiver()), var("f")), new BPLIntLiteral(0))
//                                            )
//                                    ),
//                            forall(new BPLVariable("f", new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE))),
//                                    logicalAnd(
//                                            isNull(heap1(stack1(receiver()), var("f"))),
//                                            isNull(heap2(stack2(receiver()), var("f")))
//                                            )
//                                    )
//                            )
//                    ));
            
            // invariant
            procAssumes.addAll(invAssumes);
            
            // "this" is created by context and not yet exposed
            // the two "this" objects are related
            procAssumes.add(
                    new BPLAssumeCommand(
                                logicalAnd(heap1(stack1(receiver()), var("createdByCtxt")),
                                        logicalNot(heap1(stack1(receiver()), var("exposed"))))
                            )
                    );
            procAssumes.add(
                    new BPLAssumeCommand(
                                logicalAnd(heap2(stack2(receiver()), var("createdByCtxt")),
                                        logicalNot(heap2(stack2(receiver()), var("exposed"))))
                            )
                    );
            procAssumes.add(
                    new BPLAssumeCommand(relNull(
                            stack1(receiver()),
                            stack2(receiver()), var("related")))
                    );
            
            // ///////////////////////////////////////
            // relate all parameters from the outside
            // ///////////////////////////////////////
            Pattern paramRefPattern = Pattern.compile(PARAM_VAR_PREFIX + "(\\d+)_r");
            Matcher matcher;
            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                matcher = paramRefPattern.matcher(var.getName());
                if (matcher.matches() && !matcher.group(1).equals("0")) { //special assumes for receiver (param0_r)
                    assumeCmd = new BPLAssumeCommand(relNull(
                            stack1(var(var.getName())),
                            stack2(var(var.getName())), var("related")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack1(var(var.getName()))),
                            heap1(stack1(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack2(var(var.getName()))),
                            heap2(stack2(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    assumeCmd = new BPLAssumeCommand(isEqual(
                            stack1(var(var.getName())),
                            stack2(var(var.getName()))));
                    procAssumes.add(assumeCmd);
                }
            }

            methodBlocks.add(1, new BPLBasicBlock("preconditions_constructor",
                    procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                    new BPLGotoCommand(TranslationController.LABEL_PREFIX1 + CONSTRUCTOR_TABLE_LABEL)));
            
            
            
            // //////////////////////////////////
            // preconditions of a return
            // /////////////////////////////////
            procAssumes = new ArrayList<BPLCommand>();
            procAssumes.add(new BPLAssumeCommand(greater(var("sp1"),
                    new BPLIntLiteral(0))));
            procAssumes.add(new BPLAssumeCommand(greater(var("sp2"),
                    new BPLIntLiteral(0))));

            BPLExpression zero = new BPLIntLiteral(0);
            // ///////////////////////////////////////////
            // relation of the called methods (context)
            // ///////////////////////////////////////////
            assumeCmd = new BPLAssumeCommand(isEqual(stack1(var("meth")),
                    stack2(var("meth"))));
            assumeCmd
                    .addComment("The methods called on the context have to be the same.");
            procAssumes.add(assumeCmd);
            assumeCmd = new BPLAssumeCommand(isEqual(stack1(zero, var("meth")),
                    stack2(zero, var("meth"))));
            assumeCmd
                    .addComment("Relate the methods that where originally called on the library.");
            procAssumes.add(assumeCmd);

            assumeCmd = new BPLAssumeCommand(related(stack1(zero, receiver()),
                    stack2(zero, receiver())));
            assumeCmd
                    .addComment("The receiver and all parameters where initially related.");
            // ///////////////////////////////////////
            // relate all parameters from the outside
            // ///////////////////////////////////////
            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    assumeCmd = new BPLAssumeCommand(relNull(
                            stack1(zero, var(var.getName())),
                            stack2(zero, var(var.getName())), var("related")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack1(zero, var(var.getName()))),
                            heap1(stack1(zero, var(var.getName())),
                                    var("exposed"))));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack2(zero, var(var.getName()))),
                            heap2(stack2(zero, var(var.getName())),
                                    var("exposed"))));
                    procAssumes.add(assumeCmd);
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    assumeCmd = new BPLAssumeCommand(isEqual(
                            stack1(zero, var(var.getName())),
                            stack2(zero, var(var.getName()))));
                    procAssumes.add(assumeCmd);
                }
            }

            // invariant
            procAssumes.addAll(invAssumes);

            // TODO add information about the initial state of the stack in
            // caller

            // ///////////////////////////////////////
            // relate all parameters from the outside
            // ///////////////////////////////////////
            for (BPLVariable var : TranslationController.stackVariables()
                    .values()) {
                if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                    assumeCmd = new BPLAssumeCommand(relNull(
                            stack1(var(var.getName())),
                            stack2(var(var.getName())), var("related")));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack1(var(var.getName()))),
                            heap1(stack1(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                    assumeCmd = new BPLAssumeCommand(implies(
                            nonNull(stack2(var(var.getName()))),
                            heap2(stack2(var(var.getName())), var("exposed"))));
                    procAssumes.add(assumeCmd);
                } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                    assumeCmd = new BPLAssumeCommand(isEqual(
                            stack1(var(var.getName())),
                            stack2(var(var.getName()))));
                    procAssumes.add(assumeCmd);
                }
            }

            // the method has to be overridden -> receiver was created by
            // context
            assumeCmd = new BPLAssumeCommand(heap1(stack1(receiver()),
                    var("createdByCtxt")));
            procAssumes.add(assumeCmd);
            assumeCmd = new BPLAssumeCommand(heap2(stack2(receiver()),
                    var("createdByCtxt")));
            procAssumes.add(assumeCmd);

            assumeCmd = new BPLAssumeCommand(implies(
                    hasReturnValue(stack1(var("meth"))),
                    logicalAnd(
                            heap1(stack1(var(RESULT_PARAM + REF_TYPE_ABBREV)),
                                    var("exposed")),
                            heap2(stack2(var(RESULT_PARAM + REF_TYPE_ABBREV)),
                                    var("exposed")))));
            procAssumes.add(assumeCmd);

            methodBlocks.add(2, new BPLBasicBlock("preconditions_return",
                    procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                    new BPLGotoCommand(TranslationController.DISPATCH_LABEL1)));

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
        List<String> constructorLabels = new ArrayList<String>();
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
                    
                    // add label of the method to the method label list
                    if(!method.isConstructor()){
                        methodLabels.add(methodLabel);
                    } else {
                        constructorLabels.add(methodLabel);
                    }

                    // /////////////////////////////
                    // commands before method block
                    // /////////////////////////////
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
                    preMethodCommands.add(new BPLAssumeCommand(memberOf(
                            var(GLOBAL_VAR_PREFIX
                                    + MethodTranslator.getMethodName(method)),
                            var(GLOBAL_VAR_PREFIX + classType.getName()),
                            typ(stack(receiver()),
                                    var(TranslationController.getHeap())))));

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
        String callTableLabel = TranslationController.prefix(CALLTABLE_LABEL);
        String callTableInitLabel = callTableLabel + "_init";
        
        String retTableLabel = TranslationController.prefix(RETTABLE_LABEL);
        String retTableInitLabel = retTableLabel + "_init";
        String[] returnLabels = TranslationController.returnLabels().toArray(
                new String[TranslationController.returnLabels().size()]);
        
        String constTableLabel = TranslationController.prefix(CONSTRUCTOR_TABLE_LABEL);

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

        // /////////////////////////////////////////
        // commands before callTableInit (preconditions of the calltable)
        // /////////////////////////////////////////
        BPLExpression sp = var(TranslationController.getStackPointer());

        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(isEqual(sp,
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

        // /////////////////////////////////////////
        // commands before returnTableInit (preconditions of the calltable)
        // /////////////////////////////////////////

        dispatchCommands = new ArrayList<BPLCommand>();
        // sp > 0
        dispatchCommands.add(new BPLAssumeCommand(greater(sp,
                new BPLIntLiteral(0))));

        methodBlocks.add(
                0,
                new BPLBasicBlock(retTableInitLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        new BPLGotoCommand(retTableLabel)));
        
        // ///////////////////////////////////////////
        // commands before constructor table
        // //////////////////////////////////////////
        
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(new BPLFunctionApplication(
                IS_PUBLIC_FUNC, typ(stack(receiver()),
                        var(TranslationController.getHeap())))));
        dispatchCommands.add(new BPLAssumeCommand(isCallable(
                typ(stack(receiver()), var(TranslationController.getHeap())),
                stack(var("meth")))));
        methodBlocks.add(
                0,
                new BPLBasicBlock(constTableLabel, dispatchCommands.toArray(new BPLCommand[dispatchCommands.size()]),
                        new BPLGotoCommand(constructorLabels.toArray(new String[constructorLabels.size()])))
                );
        

        // //////////////////////////////////////
        // commands before dispatch
        // //////////////////////////////////////
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(new BPLFunctionApplication(
                IS_PUBLIC_FUNC, typ(stack(receiver()),
                        var(TranslationController.getHeap())))));
        dispatchCommands.add(new BPLAssumeCommand(isCallable(
                typ(stack(receiver()), var(TranslationController.getHeap())),
                stack(var("meth")))));
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
            log.debug("Checking " + specificationFile);
            BoogieRunner.runBoogie(specificationFile);
            log.debug(BoogieRunner.getLastMessage());
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

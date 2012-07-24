package de.unikl.bcverifier;

import static b2bpl.translation.CodeGenerator.add;
import static b2bpl.translation.CodeGenerator.definesMethod;
import static b2bpl.translation.CodeGenerator.exists;
import static b2bpl.translation.CodeGenerator.forall;
import static b2bpl.translation.CodeGenerator.greater;
import static b2bpl.translation.CodeGenerator.hasReturnValue;
import static b2bpl.translation.CodeGenerator.heap;
import static b2bpl.translation.CodeGenerator.heap1;
import static b2bpl.translation.CodeGenerator.heap2;
import static b2bpl.translation.CodeGenerator.ifThenElse;
import static b2bpl.translation.CodeGenerator.implies;
import static b2bpl.translation.CodeGenerator.isCallable;
import static b2bpl.translation.CodeGenerator.isEqual;
import static b2bpl.translation.CodeGenerator.isEquiv;
import static b2bpl.translation.CodeGenerator.isLocalPlace;
import static b2bpl.translation.CodeGenerator.isNull;
import static b2bpl.translation.CodeGenerator.isPublic;
import static b2bpl.translation.CodeGenerator.less;
import static b2bpl.translation.CodeGenerator.lessEqual;
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
import static b2bpl.translation.CodeGenerator.stall1;
import static b2bpl.translation.CodeGenerator.stall2;
import static b2bpl.translation.CodeGenerator.sub;
import static b2bpl.translation.CodeGenerator.typ;
import static b2bpl.translation.CodeGenerator.useHavoc;
import static b2bpl.translation.CodeGenerator.validHeapSucc;
import static b2bpl.translation.CodeGenerator.var;
import static b2bpl.translation.CodeGenerator.wellformedHeap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus.Code;

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
import b2bpl.bpl.ast.BPLHavocCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLModifiesClause;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLRawCommand;
import b2bpl.bpl.ast.BPLRawDeclaration;
import b2bpl.bpl.ast.BPLReturnCommand;
import b2bpl.bpl.ast.BPLSpecification;
import b2bpl.bpl.ast.BPLTransferCommand;
import b2bpl.bpl.ast.BPLType;
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
import de.unikl.bcverifier.bpl.UsedVariableFinder;
import de.unikl.bcverifier.specification.GenerationException;
import de.unikl.bcverifier.specification.Generator;
import de.unikl.bcverifier.specification.SpecInvariant;
import de.unikl.bcverifier.specification.LocalPlaceDefinitions;
import de.unikl.bcverifier.specification.Place;

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
    
    private final Configuration config;
    private final Generator specGen;

    private TranslationController tc;
    
    public void setTranslationController(TranslationController controller){
        this.tc = controller;
        try{
            tc.setLocalPlaces(specGen.generateLocalPlaces());
        } catch(GenerationException e){
            Logger.getLogger(Library.class).warn("Error generating local places.", e);
            tc.setLocalPlaces(new LocalPlaceDefinitions(Collections.<Integer,List<Place>>emptyMap(), Collections.<Integer,List<Place>>emptyMap()));
        }
    }
    
    public Library(Configuration config, Generator generator) {
        this.config = config;
        this.specGen = generator;
    }
    
    

    public void compile() {
        try {
            LibraryCompiler.compile(config.library1());
            LibraryCompiler.compile(config.library2());
        } catch (CompileException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws TranslationException
     */
    public void translate() throws TranslationException {
        
        ArrayList<BPLCommand> invAssertions = new ArrayList<BPLCommand>();
        ArrayList<BPLCommand> invAssumes = new ArrayList<BPLCommand>();
        ArrayList<BPLCommand> localInvAssertions = new ArrayList<BPLCommand>();
        ArrayList<BPLCommand> localInvAssumes = new ArrayList<BPLCommand>();
        
        readInvariants(invAssertions, invAssumes, localInvAssertions,
                localInvAssumes);

        String[] oldFileNames = listLibraryClassFiles(config.library1());
        String[] newFileNames = listLibraryClassFiles(config.library2());
        

        try {
            List<BPLDeclaration> programDecls = new ArrayList<BPLDeclaration>();
            List<BPLBasicBlock> methodBlocks = new ArrayList<BPLBasicBlock>();

            Project project = Project.fromCommandLine(oldFileNames,
                    new PrintWriter(System.out));
            
            configureCodeGenerator(project);
            
            Translator trans = new Translator(project);
            trans.setTranslationController(tc);
            programDecls.addAll(trans.getPrelude()); // TODO
                                                                       // workaround
                                                                       // to
                                                                       // generate
                                                                       // Prelude
            List<BPLDeclaration> preludeAdditions = new ArrayList<BPLDeclaration>();
            try {
                for(String decl : specGen.generatePreludeAddition()){
                    preludeAdditions.add(new BPLRawDeclaration(decl));
                }
                programDecls.addAll(preludeAdditions);
            } catch(GenerationException e){
                Logger.getLogger(Library.class).warn("Error generating prelude addition.", e);
            }

            tc.activate();

            tc.enterRound1();
            LibraryDefinition libraryDefinition1 = compileSpecification(oldFileNames);
            programDecls.addAll(libraryDefinition1.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition1.getMethodBlocks());

            tc.enterRound2();
            LibraryDefinition libraryDefinition2 = compileSpecification(newFileNames);
            programDecls.addAll(libraryDefinition2.getNeededDeclarations());
            methodBlocks.addAll(libraryDefinition2.getMethodBlocks());

            addDefinesMethodAxioms(programDecls);
            
            /////////////////////////////////////
            // add method definition for java.lang.Object default constructor
            /////////////////////////////////////
            String objectConstructorName = GLOBAL_VAR_PREFIX+"."+CONSTRUCTOR_NAME+"#"+Object.class.getName();
            programDecls.add(new BPLConstantDeclaration(new BPLVariable(objectConstructorName, new BPLTypeName(METHOD_TYPE))));

            
            /////////////////////////////////////
            // add maxLoopUnroll constant to the prelude
            /////////////////////////////////////
            BPLVariable maxLoopUnrollVar = new BPLVariable(ITranslationConstants.MAX_LOOP_UNROLL, BPLBuiltInType.INT);
            programDecls.add(new BPLConstantDeclaration(maxLoopUnrollVar));
            programDecls.add(new BPLAxiom(isEqual(var(ITranslationConstants.MAX_LOOP_UNROLL), new BPLIntLiteral(config.getLoopUnrollCap()))));

            
            addCheckingBlocks(invAssertions, invAssumes, localInvAssertions, localInvAssumes, methodBlocks);
            
            
            
            for (String place : tc.places()) {
                programDecls.add(new BPLConstantDeclaration(new BPLVariable(
                        place, new BPLTypeName(ADDRESS_TYPE))));
                programDecls.add(new BPLAxiom(logicalNot(isLocalPlace(var(place)))));
            }

            List<BPLVariableDeclaration> localVariables = new ArrayList<BPLVariableDeclaration>();
            BPLVariable[] inParams = new BPLVariable[0];
            BPLVariable[] outParams = new BPLVariable[0];

            
            addLocalVariables(localVariables);

            addPreconditionBlocks(invAssumes, localInvAssumes, methodBlocks);
            
            
            addVariableConstants(programDecls, methodBlocks);
            
            buildBoogieProcedure(programDecls, methodBlocks, localVariables,
                    inParams, outParams);
            
            BPLProgram program = new BPLProgram(
                    programDecls.toArray(new BPLDeclaration[programDecls.size()]));

            log.debug("Writing specification to file " + config.output());
            // create output dir
            config.output().getParentFile().mkdirs();
            PrintWriter writer = new PrintWriter(config.output());
            program.accept(new BPLPrinter(writer));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new TranslationException(
                    "Could not write boogie specification to file.", e);
        }
    }

    private void configureCodeGenerator(Project project) {
        CodeGenerator.setProject(project);
        CodeGenerator.setTranslationController(tc);

        TypeLoader.setProject(project);
        TypeLoader.setProjectTypes(project.getProjectTypes());
        TypeLoader.setSpecificationProvider(project
                .getSpecificationProvider());
        TypeLoader.setSemanticAnalyzer(new SemanticAnalyzer(project, this));
        TypeLoader.setTroubleReporter(this);

        tc.setConfig(config);
    }

    private String[] listLibraryClassFiles(File libraryDir) {
        Collection<File> oldClassFiles = FileUtils.listFiles(libraryDir,
                new String[] { "class" }, true);
        String[] oldFileNames = new String[oldClassFiles.size() + 2];
        oldFileNames[0] = "-basedir";
        oldFileNames[1] = libraryDir.getAbsolutePath();
        int i = 2;
        for (File file : oldClassFiles) {
            oldFileNames[i] = libraryDir.toURI().relativize(file.toURI())
                    .getPath();
            i++;
        }
        return oldFileNames;
    }

    private void buildBoogieProcedure(List<BPLDeclaration> programDecls,
            List<BPLBasicBlock> methodBlocks,
            List<BPLVariableDeclaration> localVariables,
            BPLVariable[] inParams, BPLVariable[] outParams) {
        String methodName = CHECK_LIBRARIES_PROCEDURE_NAME;
        BPLImplementationBody methodBody = new BPLImplementationBody(
                localVariables.toArray(new BPLVariableDeclaration[localVariables
                        .size()]),
                methodBlocks.toArray(new BPLBasicBlock[methodBlocks.size()]));
        BPLImplementation methodImpl = new BPLImplementation(methodName,
                inParams, outParams, methodBody);
        programDecls
                .add(new BPLProcedure(methodName, inParams, outParams,
                        new BPLSpecification(new BPLModifiesClause(
                                new BPLVariableExpression(TranslationController.HEAP1),
                                new BPLVariableExpression(TranslationController.HEAP2),
                                new BPLVariableExpression(TranslationController.STACK1),
                                new BPLVariableExpression(TranslationController.STACK2),
                                new BPLVariableExpression(TranslationController.SP1),
                                new BPLVariableExpression(TranslationController.SP2),
                                new BPLVariableExpression(RELATED_RELATION),
                                new BPLVariableExpression(USE_HAVOC),
                                new BPLVariableExpression(TranslationController.STALL1),
                                new BPLVariableExpression(TranslationController.STALL2)
                                )),
                        methodImpl));
    }

    private void addVariableConstants(List<BPLDeclaration> programDecls,
            List<BPLBasicBlock> methodBlocks) {
        // add constant declarations for stack variables
        ////////////////////////////////////////////////
//            for (BPLVariable var : TranslationController.stackVariables()
//                    .values()) {
//                programDecls.add(new BPLConstantDeclaration(var));
//            }
        
        Map<String, BPLVariable> possibleStackVariables = new HashMap<String, BPLVariable>();
        String refVarName;
        String intVarName;
        refVarName = PARAM_VAR_PREFIX+0+REF_TYPE_ABBREV;
        possibleStackVariables.put(refVarName, new BPLVariable(refVarName, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE))));
        refVarName = RESULT_PARAM+REF_TYPE_ABBREV;
        possibleStackVariables.put(refVarName, new BPLVariable(refVarName, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE))));
        intVarName = RESULT_PARAM+INT_TYPE_ABBREV;
        possibleStackVariables.put(intVarName, new BPLVariable(intVarName, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT)));
        // reg0_r, reg0_i, ...
        for(int j=0; j<tc.maxLocals; j++){
            refVarName = LOCAL_VAR_PREFIX+j+REF_TYPE_ABBREV;
            intVarName = LOCAL_VAR_PREFIX+j+INT_TYPE_ABBREV;
            possibleStackVariables.put(refVarName, new BPLVariable(refVarName, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE))));
            possibleStackVariables.put(intVarName, new BPLVariable(intVarName, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT)));
        }
        // param0_r, param0_i, ...
        for(int j=0; j<tc.maxLocals+1; j++){
            refVarName = PARAM_VAR_PREFIX+j+REF_TYPE_ABBREV;
            intVarName = PARAM_VAR_PREFIX+j+INT_TYPE_ABBREV;
            possibleStackVariables.put(refVarName, new BPLVariable(refVarName, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE))));
            possibleStackVariables.put(intVarName, new BPLVariable(intVarName, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT)));
        }
        // stack0_r, stack0_i, ...
        for(int j=0; j<tc.maxStack; j++){
            refVarName = STACK_VAR_PREFIX+j+REF_TYPE_ABBREV;
            intVarName = STACK_VAR_PREFIX+j+INT_TYPE_ABBREV;
            possibleStackVariables.put(refVarName, new BPLVariable(refVarName, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE))));
            possibleStackVariables.put(intVarName, new BPLVariable(intVarName, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT)));
        }
        // filter out unused variables
        UsedVariableFinder finder = new UsedVariableFinder(possibleStackVariables);
        Map<String, BPLVariable> usedVariables = finder.findUsedVariables(methodBlocks, programDecls);
        for (BPLVariable var : usedVariables.values()) {
            programDecls.add(new BPLConstantDeclaration(var));
        }
    }

    private void readInvariants(ArrayList<BPLCommand> invAssertions,
            ArrayList<BPLCommand> invAssumes,
            ArrayList<BPLCommand> localInvAssertions,
            ArrayList<BPLCommand> localInvAssumes) {
        BPLCommand cmd;
        try{
            for (SpecInvariant inv : specGen.generateInvariant()) {
            	if (inv.getWelldefinednessExpr() != null) {
            		cmd = new BPLAssertCommand(inv.getWelldefinednessExpr());
            		cmd.addComment(inv.getComment());
            		cmd.addComment("check welldefinedness:");
            		invAssumes.add(cmd);
            		invAssertions.add(cmd);
            	}
                cmd = new BPLAssertCommand(inv.getInvExpr());
                cmd.addComment("invariant");
                invAssertions.add(cmd);
                cmd = new BPLAssumeCommand(inv.getInvExpr());
                cmd.addComment("invariant");
                invAssumes.add(cmd);
            }
        } catch(GenerationException e){
            Logger.getLogger(Library.class).warn("Error generating invariant.", e);
        }
        try{
            for (String inv : specGen.generateLocalInvariant()) {
                cmd = new BPLAssertCommand(var(inv));
                cmd.addComment("local invariant");
                localInvAssertions.add(cmd);
                cmd = new BPLAssumeCommand(var(inv));
                cmd.addComment("local invariant");
                localInvAssumes.add(cmd);
            }
        } catch(GenerationException e){
            Logger.getLogger(Library.class).warn("Error generating local invariant.", e);
        }
    }

    private void addPreconditionBlocks(ArrayList<BPLCommand> invAssumes,
            ArrayList<BPLCommand> localInvAssumes,
            List<BPLBasicBlock> methodBlocks) {
        String sp = "sp";
        BPLVariable spVar = new BPLVariable(sp, new BPLTypeName(STACK_PTR_TYPE));
        
        String unrollCount1 = TranslationController.LABEL_PREFIX1+ITranslationConstants.UNROLL_COUNT;
        String unrollCount2 = TranslationController.LABEL_PREFIX2+ITranslationConstants.UNROLL_COUNT;
        
        BPLExpression sp1MinusOne = sub(var(TranslationController.SP1), new BPLIntLiteral(1));
        BPLExpression sp2MinusOne = sub(var(TranslationController.SP2), new BPLIntLiteral(1));
        List<BPLCommand> procAssumes;

        // ///////////////////////////////////
        // preconditions of before checking
        // //////////////////////////////////
        procAssumes = new ArrayList<BPLCommand>();
        procAssumes.add(new BPLAssumeCommand(isEqual(var(unrollCount1), new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(isEqual(var(unrollCount2), new BPLIntLiteral(0))));
        
        String address = "address";
        BPLVariable addressVar = new BPLVariable(address, new BPLTypeName(ADDRESS_TYPE));
        procAssumes.add(new BPLAssumeCommand(forall(
                addressVar,
                    isEqual(useHavoc(var(address)), BPLBoolLiteral.TRUE)
                )));
        
        String a1 = "a1";
        String a2 = "a2";
        BPLVariable a1Var = new BPLVariable(a1, new BPLTypeName(ADDRESS_TYPE));
        BPLVariable a2Var = new BPLVariable(a2, new BPLTypeName(ADDRESS_TYPE));
        procAssumes.add(new BPLAssumeCommand(forall(
                a1Var, a2Var,
                logicalNot(stall1(var(a1), var(a2)))
                )));
        procAssumes.add(new BPLAssumeCommand(forall(
                a1Var, a2Var,
                logicalNot(stall2(var(a1), var(a2)))
                )));

        try{
            for(String line : specGen.generatePreconditions()){
                procAssumes.add(new BPLRawCommand(line));
            }
        } catch(GenerationException e){
            Logger.getLogger(Library.class).warn("Error generating precondition", e);
        }
        
        methodBlocks.add(
                0,
                new BPLBasicBlock(PRECONDITIONS_LABEL, procAssumes
                        .toArray(new BPLCommand[procAssumes.size()]),
                        new BPLGotoCommand(PRECONDITIONS_CALL_LABEL,
                                PRECONDITIONS_RETURN_LABEL, PRECONDITIONS_CONSTRUCTOR_LABEL, PRECONDITIONS_LOCAL_LABEL)));

        BPLCommand assumeCmd;

        // //////////////////////////////////
        // preconditions of a call
        // /////////////////////////////////
        procAssumes = new ArrayList<BPLCommand>();
        procAssumes.add(new BPLAssumeCommand(isEqual(var(TranslationController.SP1),
                new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(isEqual(var(TranslationController.SP2),
                new BPLIntLiteral(0))));
        

        // assume the result of the method is not yet set
        procAssumes.add(new BPLAssumeCommand(isNull(stack1(var(RESULT_PARAM + REF_TYPE_ABBREV)))));
        procAssumes.add(new BPLAssumeCommand(isNull(stack2(var(RESULT_PARAM + REF_TYPE_ABBREV)))));
        procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(isEqual(stack2(var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))));

        // invariant
        procAssumes.addAll(invAssumes);

        // relation between lib1 and lib2
        // ///////////////////////////////////////////
        procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var(METH_FIELD)),
                stack2(var(METH_FIELD)))));

        // relate all parameters from the outside
        // ///////////////////////////////////////
        for (BPLVariable var : tc.stackVariables()
                .values()) {
            if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                assumeCmd = new BPLAssumeCommand(relNull(
                        stack1(var(var.getName())),
                        stack2(var(var.getName())), var(RELATED_RELATION)));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack1(var(var.getName()))),
                        heap1(stack1(var(var.getName())), var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack2(var(var.getName()))),
                        heap2(stack2(var(var.getName())), var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
            } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                assumeCmd = new BPLAssumeCommand(isEqual(
                        stack1(var(var.getName())),
                        stack2(var(var.getName()))));
                procAssumes.add(assumeCmd);
            }
        }

        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack1(var(PLACE_VARIABLE))))));
        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack2(var(PLACE_VARIABLE))))));
        
        methodBlocks.add(1, new BPLBasicBlock(PRECONDITIONS_CALL_LABEL,
                procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                new BPLGotoCommand(TranslationController.DISPATCH_LABEL1)));

        
        // //////////////////////////////////
        // preconditions of a constructor call
        // /////////////////////////////////
        procAssumes = new ArrayList<BPLCommand>();
        procAssumes.add(new BPLAssumeCommand(isEqual(var(TranslationController.SP1),
                new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(isEqual(var(TranslationController.SP2),
                new BPLIntLiteral(0))));

        // initialize int return values to be zero, so the relation check of the check_boundary_return block only checks the ref-result
        procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var(RESULT_PARAM+INT_TYPE_ABBREV)), new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(isEqual(stack2(var(RESULT_PARAM+INT_TYPE_ABBREV)), new BPLIntLiteral(0))));
        
        procAssumes.add(new BPLAssumeCommand(nonNull(stack1(receiver()))));
        procAssumes.add(new BPLAssumeCommand(nonNull(stack2(receiver()))));
        
        // relation between lib1 and lib2
        // ///////////////////////////////////////////
        procAssumes.add(new BPLAssumeCommand(isEqual(stack1(var(METH_FIELD)),
                stack2(var(METH_FIELD)))));

//            // the object is not yet initialized (so the fields have their default value)
        procAssumes.add(
                new BPLAssumeCommand(logicalAnd(
                        forall(new BPLVariable("f", new BPLTypeName(FIELD_TYPE, BPLBuiltInType.INT)),
                                logicalAnd(
                                        isEqual(heap1(stack1(receiver()), var("f")), new BPLIntLiteral(0)),
                                        isEqual(heap2(stack2(receiver()), var("f")), new BPLIntLiteral(0))
                                        )
                                ),
                        forall(new BPLVariable("f", new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE))),
                                logicalAnd(
                                        isNull(heap1(stack1(receiver()), var("f"))),
                                        isNull(heap2(stack2(receiver()), var("f")))
                                        )
                                )
                        )
                ));
        
        // "this" is created by context and not yet exposed
        // the two "this" objects are related
        procAssumes.add(
                new BPLAssumeCommand(
                        logicalAnd(heap1(stack1(receiver()), var(CREATED_BY_CTXT_FIELD)),
                                logicalNot(heap1(stack1(receiver()), var(EXPOSED_FIELD))))
                        )
                );
        procAssumes.add(
                new BPLAssumeCommand(
                        logicalAnd(heap2(stack2(receiver()), var(CREATED_BY_CTXT_FIELD)),
                                logicalNot(heap2(stack2(receiver()), var(EXPOSED_FIELD))))
                        )
                );
        
        // invariant
        procAssumes.addAll(invAssumes);
        
        
        // now pass the receiver over the boundary
        procAssumes.add(new BPLAssignmentCommand(heap1(stack1(receiver()), var(EXPOSED_FIELD)), BPLBoolLiteral.TRUE));
        procAssumes.add(new BPLAssignmentCommand(heap2(stack2(receiver()), var(EXPOSED_FIELD)), BPLBoolLiteral.TRUE));
        procAssumes.add(new BPLAssignmentCommand(related(stack1(receiver()), stack2(receiver())), BPLBoolLiteral.TRUE));
        if(config.isAssumeWellformedHeap()){
            procAssumes.add(new BPLAssumeCommand(CodeGenerator.wellformedHeap(var(TranslationController.HEAP1))));
            procAssumes.add(new BPLAssumeCommand(CodeGenerator.wellformedHeap(var(TranslationController.HEAP2))));
        }
        
        // relate all parameters from the outside
        // ///////////////////////////////////////
        Pattern paramRefPattern = Pattern.compile(PARAM_VAR_PREFIX + "(\\d+)_r");
        Matcher matcher;
        for (BPLVariable var : tc.stackVariables()
                .values()) {
            matcher = paramRefPattern.matcher(var.getName());
            if (matcher.matches() && !matcher.group(1).equals("0")) { //special assumes for receiver (param0_r)
                assumeCmd = new BPLAssumeCommand(relNull(
                        stack1(var(var.getName())),
                        stack2(var(var.getName())), var(RELATED_RELATION)));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack1(var(var.getName()))),
                        heap1(stack1(var(var.getName())), var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack2(var(var.getName()))),
                        heap2(stack2(var(var.getName())), var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
            } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                assumeCmd = new BPLAssumeCommand(isEqual(
                        stack1(var(var.getName())),
                        stack2(var(var.getName()))));
                procAssumes.add(assumeCmd);
            }
        }

        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack1(var(PLACE_VARIABLE))))));
        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack2(var(PLACE_VARIABLE))))));
        
        methodBlocks.add(1, new BPLBasicBlock(PRECONDITIONS_CONSTRUCTOR_LABEL,
                procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                new BPLGotoCommand(TranslationController.LABEL_PREFIX1 + CONSTRUCTOR_TABLE_LABEL)));
        
        
        
        // //////////////////////////////////
        // preconditions of a return
        // /////////////////////////////////
        procAssumes = new ArrayList<BPLCommand>();
        procAssumes.add(new BPLAssumeCommand(greater(var(TranslationController.SP1),
                new BPLIntLiteral(0))));
        procAssumes.add(new BPLAssumeCommand(greater(var(TranslationController.SP2),
                new BPLIntLiteral(0))));
        
        // this return path may not be taken if havoc is used to handle it
        /////////////////////////////////////////////////////////////////
        //TODO maybe add consistency check useHavoc[stack1[sp1][place]] <=> useHavoc[stack2[sp2][place]] 
        procAssumes.add(new BPLAssumeCommand(logicalNot(useHavoc(stack1(sp1MinusOne, var(PLACE_VARIABLE))))));
        procAssumes.add(new BPLAssumeCommand(logicalNot(useHavoc(stack2(sp2MinusOne, var(PLACE_VARIABLE))))));
        
        // can not return to a static method call site
        //////////////////////////////////////////////
        procAssumes.add(new BPLAssumeCommand(logicalNot(CodeGenerator.isStaticMethod(stack1(var(METH_FIELD))))));

        BPLExpression zero = new BPLIntLiteral(0);
        
        // relation of the called methods (context)
        // ///////////////////////////////////////////
        assumeCmd = new BPLAssumeCommand(isEqual(stack1(var(METH_FIELD)),
                stack2(var(METH_FIELD))));
        assumeCmd
                .addComment("The methods called on the context have to be the same.");
        procAssumes.add(assumeCmd);
        assumeCmd = new BPLAssumeCommand(isEqual(stack1(zero, var(METH_FIELD)),
                stack2(zero, var(METH_FIELD))));
        assumeCmd
                .addComment("Relate the methods that where originally called on the library.");
        procAssumes.add(assumeCmd);

        assumeCmd = new BPLAssumeCommand(related(stack1(zero, receiver()),
                stack2(zero, receiver())));
        assumeCmd
                .addComment("The receiver and all parameters where initially related.");
        
        // relate all parameters from the outside
        // ///////////////////////////////////////
        for (BPLVariable var : tc.stackVariables()
                .values()) {
            if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                assumeCmd = new BPLAssumeCommand(relNull(
                        stack1(zero, var(var.getName())),
                        stack2(zero, var(var.getName())), var(RELATED_RELATION)));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack1(zero, var(var.getName()))),
                        heap1(stack1(zero, var(var.getName())),
                                var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack2(zero, var(var.getName()))),
                        heap2(stack2(zero, var(var.getName())),
                                var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
            } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                assumeCmd = new BPLAssumeCommand(isEqual(
                        stack1(zero, var(var.getName())),
                        stack2(zero, var(var.getName()))));
                procAssumes.add(assumeCmd);
            }
        }
        
        // assume the result of the method is not yet set
        procAssumes.add(new BPLAssumeCommand(
                forall(spVar, 
                        implies(less(var(sp), var(TranslationController.SP1)), logicalAnd(isNull(stack1(var(sp), var(RESULT_PARAM + REF_TYPE_ABBREV))), isEqual(stack1(var(sp), var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))) )
                )));
        procAssumes.add(new BPLAssumeCommand(
                forall(spVar, 
                        implies(less(var(sp), var(TranslationController.SP2)), logicalAnd(isNull(stack2(var(sp), var(RESULT_PARAM + REF_TYPE_ABBREV))), isEqual(stack2(var(sp), var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))) )
                )));

        // invariant
        procAssumes.addAll(invAssumes);
        procAssumes.addAll(localInvAssumes);

        // relate all parameters from the outside
        // ///////////////////////////////////////
        for (BPLVariable var : tc.stackVariables()
                .values()) {
            if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                assumeCmd = new BPLAssumeCommand(relNull(
                        stack1(var(var.getName())),
                        stack2(var(var.getName())), var(RELATED_RELATION)));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack1(var(var.getName()))),
                        heap1(stack1(var(var.getName())), var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack2(var(var.getName()))),
                        heap2(stack2(var(var.getName())), var(EXPOSED_FIELD))));
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
                var(CREATED_BY_CTXT_FIELD)));
        procAssumes.add(assumeCmd);
        assumeCmd = new BPLAssumeCommand(heap2(stack2(receiver()),
                var(CREATED_BY_CTXT_FIELD)));
        procAssumes.add(assumeCmd);

        assumeCmd = new BPLAssumeCommand(implies(
                hasReturnValue(stack1(var(METH_FIELD))),
                logicalAnd(
                        heap1(stack1(var(RESULT_PARAM + REF_TYPE_ABBREV)),
                                var(EXPOSED_FIELD)),
                        heap2(stack2(var(RESULT_PARAM + REF_TYPE_ABBREV)),
                                var(EXPOSED_FIELD)))));
        procAssumes.add(assumeCmd);

        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack1(var(PLACE_VARIABLE))))));
        procAssumes.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack2(var(PLACE_VARIABLE))))));
        
        methodBlocks.add(2, new BPLBasicBlock(PRECONDITIONS_RETURN_LABEL,
                procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                new BPLGotoCommand(TranslationController.DISPATCH_LABEL1)));
        
        
        // //////////////////////////////////
        // preconditions of a return to a local place
        // /////////////////////////////////
        procAssumes = new ArrayList<BPLCommand>();
        procAssumes.add(new BPLAssumeCommand(isLocalPlace(stack1(var(PLACE_VARIABLE)))));
        procAssumes.add(new BPLAssumeCommand(isLocalPlace(stack2(var(PLACE_VARIABLE)))));
        
        // relation of the methods initially called on the library
        // ///////////////////////////////////////////
        assumeCmd = new BPLAssumeCommand(isEqual(stack1(zero, var(METH_FIELD)),
                stack2(zero, var(METH_FIELD))));
        assumeCmd.addComment("Relate the methods that where originally called on the library.");
        procAssumes.add(assumeCmd);

        assumeCmd = new BPLAssumeCommand(related(stack1(zero, receiver()),
                stack2(zero, receiver())));
        assumeCmd.addComment("The receiver and all parameters where initially related.");
        
        // relate all parameters from the outside
        // ///////////////////////////////////////
        for (BPLVariable var : tc.stackVariables()
                .values()) {
            if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                assumeCmd = new BPLAssumeCommand(relNull(
                        stack1(zero, var(var.getName())),
                        stack2(zero, var(var.getName())), var(RELATED_RELATION)));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack1(zero, var(var.getName()))),
                        heap1(stack1(zero, var(var.getName())),
                                var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
                assumeCmd = new BPLAssumeCommand(implies(
                        nonNull(stack2(zero, var(var.getName()))),
                        heap2(stack2(zero, var(var.getName())),
                                var(EXPOSED_FIELD))));
                procAssumes.add(assumeCmd);
            } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                assumeCmd = new BPLAssumeCommand(isEqual(
                        stack1(zero, var(var.getName())),
                        stack2(zero, var(var.getName()))));
                procAssumes.add(assumeCmd);
            }
        }
        
        // assume the result of the method is not yet set
        procAssumes.add(new BPLAssumeCommand(
                forall(spVar, 
                        implies(lessEqual(var(sp), var(TranslationController.SP1)), logicalAnd(isNull(stack1(var(sp), var(RESULT_PARAM + REF_TYPE_ABBREV))), isEqual(stack1(var(sp), var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))) )
                )));
        procAssumes.add(new BPLAssumeCommand(
                forall(spVar, 
                        implies(lessEqual(var(sp), var(TranslationController.SP2)), logicalAnd(isNull(stack2(var(sp), var(RESULT_PARAM + REF_TYPE_ABBREV))), isEqual(stack2(var(sp), var(RESULT_PARAM + INT_TYPE_ABBREV)), new BPLIntLiteral(0))) )
                )));

        // invariant
        procAssumes.addAll(localInvAssumes);

        
        methodBlocks.add(2, new BPLBasicBlock(PRECONDITIONS_LOCAL_LABEL,
                procAssumes.toArray(new BPLCommand[procAssumes.size()]),
                new BPLGotoCommand(TranslationController.DISPATCH_LABEL1)));
    }

    private void addLocalVariables(List<BPLVariableDeclaration> localVariables) {
        String unrollCount1 = TranslationController.LABEL_PREFIX1+ITranslationConstants.UNROLL_COUNT;
        BPLVariable unrollCount1Var = new BPLVariable(unrollCount1, BPLBuiltInType.INT);
        String unrollCount2 = TranslationController.LABEL_PREFIX2+ITranslationConstants.UNROLL_COUNT;
        BPLVariable unrollCount2Var = new BPLVariable(unrollCount2, BPLBuiltInType.INT);
        BPLVariable maxLoopUnrollVar = new BPLVariable(ITranslationConstants.MAX_LOOP_UNROLL, BPLBuiltInType.INT);
        
        for (BPLVariable var : tc.usedVariables()
                .values()) {
            localVariables.add(new BPLVariableDeclaration(var));
        }
        
        // add variables for loop unroll checking
        //////////////////////////////////////
        localVariables.add(new BPLVariableDeclaration(unrollCount1Var));
        localVariables.add(new BPLVariableDeclaration(unrollCount2Var));
        
        // add variables for saving away the old heaps
        //////////////////////////////////////////////
        localVariables.add(new BPLVariableDeclaration(new BPLVariable(OLD_HEAP1, new BPLTypeName(HEAP_TYPE))));
        localVariables.add(new BPLVariableDeclaration(new BPLVariable(OLD_HEAP2, new BPLTypeName(HEAP_TYPE))));
    }

    private void addCheckingBlocks(ArrayList<BPLCommand> invAssertions,
            ArrayList<BPLCommand> invAssumes,
            ArrayList<BPLCommand> localInvAssertions,
            ArrayList<BPLCommand> localInvAssumes,
            List<BPLBasicBlock> methodBlocks) {
        // ///////////////////////////////////
        // checking blocks (intern and extern)
        // ///////////////////////////////////
        List<BPLCommand> checkingCommand = new ArrayList<BPLCommand>();
        checkingCommand.add(new BPLAssertCommand(logicalOr(
                logicalAnd(
                        isEqual(var(TranslationController.SP1), new BPLIntLiteral(0)),
                        isEqual(var(TranslationController.SP2), new BPLIntLiteral(0))
                        ),
                logicalAnd(
                        greater(var(TranslationController.SP1), new BPLIntLiteral(0)),
                        greater(var(TranslationController.SP2), new BPLIntLiteral(0))
                        ),
                logicalAnd(
                        isLocalPlace(stack1(var(PLACE_VARIABLE))),
                        isLocalPlace(stack2(var(PLACE_VARIABLE)))
                        )
                )));
        methodBlocks.add(new BPLBasicBlock(tc
                .getCheckLabel(), checkingCommand.toArray(new BPLCommand[checkingCommand.size()]), new BPLGotoCommand(
                CHECK_BOUNDARY_RETURN_LABEL, CHECK_BOUNDARY_CALL_LABEL, CHECK_LOCAL_LABEL)));

        // ////////////////////////////////
        // assertions of the check return block
        // ///////////////////////////////
        checkingCommand = new ArrayList<BPLCommand>();
        checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                isEqual(var(TranslationController.SP1), new BPLIntLiteral(0)),
                isEqual(var(TranslationController.SP2), new BPLIntLiteral(0)))));
        checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                logicalNot(isLocalPlace(stack1(var(PLACE_VARIABLE)))),
                logicalNot(isLocalPlace(stack2(var(PLACE_VARIABLE)))))));

        checkingCommand.add(new BPLAssertCommand(isEqual(
                stack1(var(METH_FIELD)), stack2(var(METH_FIELD)))));
        
        checkingCommand.add(new BPLAssignmentCommand(
              heap1(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                      var(EXPOSED_FIELD)), ifThenElse(
                      logicalAnd(hasReturnValue(stack1(var(METH_FIELD))),
                              nonNull(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV))),
                              nonNull(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)))),
                      BPLBoolLiteral.TRUE,
                      heap1(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                              var(EXPOSED_FIELD)))));
        checkingCommand.add(new BPLAssignmentCommand(
              heap2(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                      var(EXPOSED_FIELD)), ifThenElse(
                      logicalAnd(hasReturnValue(stack2(var(METH_FIELD))),
                              nonNull(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV))),
                              nonNull(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)))),
                      BPLBoolLiteral.TRUE,
                      heap2(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                              var(EXPOSED_FIELD)))));
        checkingCommand.add(new BPLAssignmentCommand(related(
              stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
              stack2(var(RESULT_PARAM+REF_TYPE_ABBREV))), ifThenElse(
              logicalAnd(nonNull(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV))),
                      nonNull(stack2(var(RESULT_PARAM+REF_TYPE_ABBREV)))),
              BPLBoolLiteral.TRUE,
              related(stack1(var(RESULT_PARAM+REF_TYPE_ABBREV)),
                      stack2(var(RESULT_PARAM+REF_TYPE_ABBREV))))));
        
        if(config.isAssumeWellformedHeap()){
          checkingCommand.add(new BPLAssumeCommand(wellformedHeap(var(TranslationController.HEAP1))));
          checkingCommand.add(new BPLAssumeCommand(wellformedHeap(var(TranslationController.HEAP2))));
        }

        checkingCommand.add(new BPLAssertCommand(
                implies(hasReturnValue(stack1(var(METH_FIELD))),
                        logicalAnd(
                                relNull(stack1(var(RESULT_VAR
                                        + REF_TYPE_ABBREV)),
                                        stack2(var(RESULT_VAR
                                                + REF_TYPE_ABBREV)),
                                        var(RELATED_RELATION)),
                                isEqual(stack1(var(RESULT_VAR
                                        + INT_TYPE_ABBREV)),
                                        stack2(var(RESULT_VAR
                                                + INT_TYPE_ABBREV)))))));

        String o1 = "o1";
        BPLVariable o1Var = new BPLVariable(o1, new BPLTypeName(REF_TYPE));
        String o2 = "o2";
        BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        
        checkingCommand.add(new BPLAssertCommand(forall(o1Var, o2Var, implies(related(var(o1), var(o2)), relNull(var(o1), var(o2), var(RELATED_RELATION))))));
        
        //invariant
        checkingCommand.addAll(invAssertions);
        
        methodBlocks.add(new BPLBasicBlock(CHECK_BOUNDARY_RETURN_LABEL, checkingCommand
                .toArray(new BPLCommand[checkingCommand.size()]),
                new BPLReturnCommand()));

        // ////////////////////////////////
        // assertions of the check call block
        // ////////////////////////////////
        checkingCommand.clear();
        checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                greater(var(TranslationController.SP1), new BPLIntLiteral(0)),
                greater(var(TranslationController.SP2), new BPLIntLiteral(0)))));
        checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                logicalNot(isLocalPlace(stack1(var(PLACE_VARIABLE)))),
                logicalNot(isLocalPlace(stack2(var(PLACE_VARIABLE)))))));

        checkingCommand.add(new BPLAssertCommand(isEqual(
                stack1(var(METH_FIELD)), stack2(var(METH_FIELD)))));

        for (BPLVariable var : tc.stackVariables()
                .values()) {
            if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_r")) {
                checkingCommand
                        .add(new BPLAssertCommand(
                                logicalOr(
                                        relNull(stack1(var(var.getName())),
                                                stack2(var(var.getName())),
                                                var(RELATED_RELATION)),
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
                                        var(EXPOSED_FIELD)), ifThenElse(
                                        logicalAnd(nonNull(stack1(var(var
                                                .getName()))),
                                                nonNull(stack2(var(var
                                                        .getName())))),
                                        BPLBoolLiteral.TRUE,
                                        heap1(stack1(var(var.getName())),
                                                var(EXPOSED_FIELD)))));
                checkingCommand
                        .add(new BPLAssignmentCommand(
                                heap2(stack2(var(var.getName())),
                                        var(EXPOSED_FIELD)), ifThenElse(
                                        logicalAnd(nonNull(stack1(var(var
                                                .getName()))),
                                                nonNull(stack2(var(var
                                                        .getName())))),
                                        BPLBoolLiteral.TRUE,
                                        heap2(stack2(var(var.getName())),
                                                var(EXPOSED_FIELD)))));
                checkingCommand.add(new BPLAssignmentCommand(related(
                        stack1(var(var.getName())),
                        stack2(var(var.getName()))), ifThenElse(
                        logicalAnd(nonNull(stack1(var(var.getName()))),
                                nonNull(stack2(var(var.getName())))),
                        BPLBoolLiteral.TRUE,
                        related(stack1(var(var.getName())),
                                stack2(var(var.getName()))))));
                
                if(config.isAssumeWellformedHeap()){
                    checkingCommand.add(new BPLAssumeCommand(wellformedHeap(var(TranslationController.HEAP1))));
                    checkingCommand.add(new BPLAssumeCommand(wellformedHeap(var(TranslationController.HEAP2))));
                }
            } else if (var.getName().matches(PARAM_VAR_PREFIX + "\\d+_i")) {
                checkingCommand.add(new BPLAssertCommand(isEqual(
                        stack1(var(var.getName())),
                        stack2(var(var.getName())))));
            }
        }
        checkingCommand.add(new BPLAssertCommand(forall(o1Var, o2Var, implies(related(var(o1), var(o2)), relNull(var(o1), var(o2), var(RELATED_RELATION))))));
        
        //invariant
        checkingCommand.addAll(invAssertions);
        checkingCommand.addAll(localInvAssertions);
        
        // check if we want to use havoc to handle boudary call
        /////////////////////////////////////////////////////////
        BPLExpression sp1MinusOne = sub(var(TranslationController.SP1), new BPLIntLiteral(1));
        BPLExpression sp2MinusOne = sub(var(TranslationController.SP2), new BPLIntLiteral(1));
        checkingCommand.add(new BPLAssertCommand(
                isEquiv(useHavoc(stack1(sp1MinusOne, var(PLACE_VARIABLE))), useHavoc(stack2(sp2MinusOne, var(PLACE_VARIABLE)))))
                );
        checkingCommand.add(new BPLAssumeCommand(
                logicalAnd(useHavoc(stack1(sp1MinusOne, var(PLACE_VARIABLE))), useHavoc(stack2(sp2MinusOne, var(PLACE_VARIABLE)))))
                );
        
        // save away the old heaps
        checkingCommand.add(new BPLAssignmentCommand(var(OLD_HEAP1), var(TranslationController.HEAP1)));
        checkingCommand.add(new BPLAssignmentCommand(var(OLD_HEAP2), var(TranslationController.HEAP2)));
        
        checkingCommand.add(new BPLHavocCommand(var(TranslationController.HEAP1), var(TranslationController.HEAP2)));
        
        // the exposed and createdByCtxt flags have to be preserved for the invariant to be applicable
        String sp = "sp";
        BPLVariable spVar = new BPLVariable(sp, new BPLTypeName(STACK_PTR_TYPE));
        String v = "v";
        BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)));
//            checkingCommand.add(new BPLAssumeCommand(
//                    forall(
//                            spVar, vVar,
//                            logicalAnd(
//                                    implies(oldHeap1(stack1(var(sp), var(v)), var(EXPOSED_FIELD)), heap1(stack1(var(sp), var(v)), var(EXPOSED_FIELD))),
//                                    isEqual(oldHeap1(stack1(var(sp), var(v)), var(CREATED_BY_CTXT_FIELD)), heap1(stack1(var(sp), var(v)), var(CREATED_BY_CTXT_FIELD)))
//                            )
//                            )
//                    ));
//            checkingCommand.add(new BPLAssumeCommand(
//                    forall(
//                            spVar, vVar,
//                            logicalAnd(
//                                    implies(oldHeap2(stack2(var(sp), var(v)), var(EXPOSED_FIELD)), heap2(stack2(var(sp), var(v)), var(EXPOSED_FIELD))),
//                                    isEqual(oldHeap2(stack2(var(sp), var(v)), var(CREATED_BY_CTXT_FIELD)), heap2(stack2(var(sp), var(v)), var(CREATED_BY_CTXT_FIELD)))
//                            )
//                            )
//                    ));
        
        // relate stack and heap again
        ///////////////////////////////////
//            checkingCommand.add(new BPLAssumeCommand(CodeGenerator.wellformedStack(var(TranslationController.STACK1), var(TranslationController.SP1), var(TranslationController.HEAP1))));
//            checkingCommand.add(new BPLAssumeCommand(CodeGenerator.wellformedStack(var(TranslationController.STACK2), var(TranslationController.SP2), var(TranslationController.HEAP2))));
        
        // relate the new heap with the old one
        ///////////////////////////////////////
        checkingCommand.add(new BPLAssumeCommand(validHeapSucc(var(OLD_HEAP1), var(TranslationController.HEAP1), var(TranslationController.STACK1))));
        checkingCommand.add(new BPLAssumeCommand(validHeapSucc(var(OLD_HEAP2), var(TranslationController.HEAP2), var(TranslationController.STACK2))));
        
        //invariant
        checkingCommand.addAll(invAssumes);
        checkingCommand.addAll(localInvAssumes);
        

        methodBlocks.add(new BPLBasicBlock(CHECK_BOUNDARY_CALL_LABEL, checkingCommand
                .toArray(new BPLCommand[checkingCommand.size()]),
                new BPLGotoCommand(TranslationController.LABEL_PREFIX1 + RETTABLE_LABEL)));
        
        
        // ////////////////////////////////
        // assertions of the check local block
        // ////////////////////////////////
        checkingCommand.clear();
        checkingCommand.add(new BPLAssumeCommand(logicalAnd(
                isLocalPlace(stack1(var(PLACE_VARIABLE))),
                isLocalPlace(stack2(var(PLACE_VARIABLE))))));

        
        checkingCommand.addAll(localInvAssertions);

        methodBlocks.add(new BPLBasicBlock(CHECK_LOCAL_LABEL, checkingCommand
                .toArray(new BPLCommand[checkingCommand.size()]),
                new BPLReturnCommand()));
    }

    private void addDefinesMethodAxioms(List<BPLDeclaration> programDecls) {
        // insert all method definition axioms
        ///////////////////////////////////////
        for (String className : tc.methodDefinitions()
                .keySet()) {
            Set<String> methodNames = tc
                    .methodDefinitions().get(className);
            String m = "m";
            BPLVariable mVar = new BPLVariable(m, new BPLTypeName(
                    METHOD_TYPE));
            if(!methodNames.isEmpty()){
                List<BPLExpression> methodExprs = new ArrayList<BPLExpression>();

                for (String methodName : methodNames) {
                    methodExprs.add(isEqual(var(m), var(methodName)));
                }
                programDecls.add(new BPLAxiom(forall(
                        mVar,
                        isEquiv(definesMethod(var(className), var(m)),
                                logicalOr(methodExprs
                                        .toArray(new BPLExpression[methodExprs
                                                                   .size()]))))));
            } else {
                programDecls.add(new BPLAxiom(forall(
                        mVar,
                        logicalNot(definesMethod(var(className), var(m)))
                        )
                        ));
            }
        }
    }

    private LibraryDefinition compileSpecification(String[] fileNames)
            throws FileNotFoundException {
        tc.resetReturnLabels();
        tc.resetLocalPlaces();

        Project project = Project.fromCommandLine(fileNames, new PrintWriter(
                System.out));
        CodeGenerator.setProject(project);
        CodeGenerator.setTranslationController(tc);

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
        translator.setTranslationController(tc);
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
                                tc.stackVariables().put(
                                        var.getName(), var);
                            } else {
                                tc.usedVariables().put(
                                        var.getName(), var);
                            }
                        }
                    }
                    for (BPLVariable outParam : proc.getOutParameters()) {
                        tc.usedVariables().put(
                                outParam.getName(), outParam);
                    }

                    methodLabel = tc.prefix(proc.getName());
                    
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
                            stack(var(PLACE_VARIABLE)), var(tc
                                    .buildPlace(proc.getName(), true)))));
                    
                    preMethodCommands
                    .add(new BPLAssumeCommand(isEqual(
                            stack(var(METH_FIELD)),
                            var(GLOBAL_VAR_PREFIX
                                    + MethodTranslator
                                    .getMethodName(method)))));
                    if(!method.isStatic()){
                        preMethodCommands.add(new BPLAssumeCommand(memberOf(
                                var(GLOBAL_VAR_PREFIX
                                        + MethodTranslator.getMethodName(method)),
                                        var(GLOBAL_VAR_PREFIX + classType.getName()),
                                        typ(stack(receiver()),
                                                var(tc.getHeap())))));
                    }

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
        tc.maxLocals = Math.max(tc.maxLocals, maxLocals);
        tc.maxStack = Math.max(tc.maxStack, maxStack);
        
        ///////////////////////////////////////////////
        // add default constructor for java.lang.Object in case it is called inside the methods/constructors of the library
        ////////////////////////////////////////////////
        String constructorLabelObject = tc.prefix(Object.class.getName()+"."+CONSTRUCTOR_NAME);
        List<BPLCommand> objectConstructorCommands = new ArrayList<BPLCommand>();
        objectConstructorCommands.add(new BPLAssignmentCommand(CodeGenerator.stack(var(RESULT_PARAM+REF_TYPE_ABBREV)), stack(receiver())));
        methodBlocks.add(
                0,
                new BPLBasicBlock(constructorLabelObject, objectConstructorCommands.toArray(new BPLCommand[objectConstructorCommands.size()]),
                        new BPLGotoCommand(tc.prefix(RETTABLE_LABEL)))
                );
        

        // //////////////////////////////////////
        // callTable and returnTable
        // //////////////////////////////////////
        String callTableLabel = tc.prefix(CALLTABLE_LABEL);
        String callTableInitLabel = callTableLabel + INIT_LABEL_POSTFIX;
        
        String retTableLabel = tc.prefix(RETTABLE_LABEL);
        String retTableInitLabel = retTableLabel + INIT_LABEL_POSTFIX;
        String[] returnLabels = tc.returnLabels().toArray(
                new String[tc.returnLabels().size()]);
        
        String placeTableLabel = tc.prefix(LOCAL_PLACES_TABLE_LABEL);
        String[] placesLabels = tc.getLocalPlaces().toArray(
                new String[tc.getLocalPlaces().size()]);
        
        String constTableLabel = tc.prefix(CONSTRUCTOR_TABLE_LABEL);

        BPLTransferCommand dispatchTransferCmd;
        
        // //////////////////////////////////////
        // commands before localPlacesTable
        // /////////////////////////////////////
        List<BPLCommand> dispatchCommands;
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(CodeGenerator.isLocalPlace(stack(var(PLACE_VARIABLE)))));
        BPLExpression unrollLoop = var(tc.prefix(ITranslationConstants.UNROLL_COUNT));
        dispatchCommands.add(new BPLAssignmentCommand(unrollLoop, add(unrollLoop, new BPLIntLiteral(1))));
        dispatchCommands.add(new BPLAssertCommand(less(unrollLoop, var(ITranslationConstants.MAX_LOOP_UNROLL))));
        
        if (placesLabels.length > 0) {
            dispatchTransferCmd = new BPLGotoCommand(placesLabels);
        } else {
            dispatchTransferCmd = new BPLReturnCommand();
        }
        methodBlocks.add(
                0,
                new BPLBasicBlock(placeTableLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        dispatchTransferCmd));
        
        
        // //////////////////////////////////////
        // commands before callTable
        // /////////////////////////////////////
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack(var(PLACE_VARIABLE))))));
        dispatchCommands.add(new BPLAssignmentCommand(unrollLoop, add(unrollLoop, new BPLIntLiteral(1))));
        dispatchCommands.add(new BPLAssertCommand(less(unrollLoop, var(ITranslationConstants.MAX_LOOP_UNROLL))));
        
        if (methodLabels.size() > 0) {
            dispatchTransferCmd = new BPLGotoCommand(methodLabels
                    .toArray(new String[methodLabels.size()]));
        } else {
            dispatchTransferCmd = new BPLReturnCommand();
        }
        methodBlocks.add(
                0,
                new BPLBasicBlock(callTableLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        dispatchTransferCmd));



        // /////////////////////////////////////////
        // commands before callTableInit (preconditions of the calltable)
        // /////////////////////////////////////////
        BPLExpression sp = var(tc.getStackPointer());

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
        dispatchCommands.add(new BPLAssumeCommand(logicalNot(isLocalPlace(stack(var(PLACE_VARIABLE))))));
        dispatchCommands.add(new BPLAssignmentCommand(unrollLoop, add(unrollLoop, new BPLIntLiteral(1))));
        dispatchCommands.add(new BPLAssertCommand(less(unrollLoop, var(ITranslationConstants.MAX_LOOP_UNROLL))));
        
        
        if (returnLabels.length > 0) {
            dispatchTransferCmd = new BPLGotoCommand(returnLabels);
        } else {
            dispatchTransferCmd = new BPLReturnCommand();
        }
        methodBlocks.add(
                0,
                new BPLBasicBlock(retTableLabel, dispatchCommands
                        .toArray(new BPLCommand[dispatchCommands.size()]),
                        dispatchTransferCmd));

        // /////////////////////////////////////////
        // commands before returnTableInit (preconditions of the returntable)
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
        dispatchCommands.add(new BPLAssumeCommand(
                logicalOr(
                    isPublic(typ(stack(receiver()),var(tc.getHeap()))),
                    logicalNot(heap(stack(receiver()), var(CREATED_BY_CTXT_FIELD)))
                )
                ));
//        dispatchCommands.add(new BPLAssumeCommand(isCallable(
//                typ(stack(receiver()), var(tc.getHeap())),
//                stack(var(METH_FIELD)))));
        dispatchCommands.add(new BPLAssignmentCommand(unrollLoop, add(unrollLoop, new BPLIntLiteral(1))));
        dispatchCommands.add(new BPLAssertCommand(less(unrollLoop, var(ITranslationConstants.MAX_LOOP_UNROLL))));
        
        if (constructorLabels.size() > 0) {
            dispatchTransferCmd = new BPLGotoCommand(constructorLabels.toArray(new String[constructorLabels.size()]));
        } else {
            dispatchTransferCmd = new BPLReturnCommand();
        }
        methodBlocks.add(
                0,
                new BPLBasicBlock(constTableLabel, dispatchCommands.toArray(new BPLCommand[dispatchCommands.size()]),
                        dispatchTransferCmd)
                );
        

        // //////////////////////////////////////
        // commands before dispatch
        // //////////////////////////////////////
        dispatchCommands = new ArrayList<BPLCommand>();
        dispatchCommands.add(new BPLAssumeCommand(
                logicalOr(
                    isPublic(typ(stack(receiver()),var(tc.getHeap()))),
                    logicalNot(heap(stack(receiver()), var(CREATED_BY_CTXT_FIELD)))
                )
                ));
//        dispatchCommands.add(new BPLAssumeCommand(isCallable(
//                typ(stack(receiver()), var(tc.getHeap())),
//                stack(var(METH_FIELD)))));
        methodBlocks
                .add(0,
                        new BPLBasicBlock(
                                tc.getDispatchLabel(),
                                dispatchCommands
                                        .toArray(new BPLCommand[dispatchCommands
                                                .size()]), new BPLGotoCommand(
                                        callTableInitLabel, retTableInitLabel, placeTableLabel)));

        return new LibraryDefinition(programDecls, methodBlocks);
    }

    public void check() {
        BoogieRunner.setVerify(config.isVerify());
        BoogieRunner.setSmokeTest(config.isSmokeTestOn());
        BoogieRunner.setLoopUnroll(config.getLoopUnrollCap()+1);
        try {
            log.debug("Checking " + config.output());
            BoogieRunner.runBoogie(config.output());
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

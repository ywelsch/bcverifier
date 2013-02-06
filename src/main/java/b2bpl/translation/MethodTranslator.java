package b2bpl.translation;

import static b2bpl.translation.CodeGenerator.add;
import static b2bpl.translation.CodeGenerator.bitAnd;
import static b2bpl.translation.CodeGenerator.bitOr;
import static b2bpl.translation.CodeGenerator.bitShl;
import static b2bpl.translation.CodeGenerator.bitShr;
import static b2bpl.translation.CodeGenerator.bitUShr;
import static b2bpl.translation.CodeGenerator.bitXor;
import static b2bpl.translation.CodeGenerator.bool2int;
import static b2bpl.translation.CodeGenerator.cast;
import static b2bpl.translation.CodeGenerator.classRepr;
import static b2bpl.translation.CodeGenerator.ctxtType;
import static b2bpl.translation.CodeGenerator.emptyInteractionFrame;
import static b2bpl.translation.CodeGenerator.exists;
import static b2bpl.translation.CodeGenerator.fieldAccess;
import static b2bpl.translation.CodeGenerator.greater;
import static b2bpl.translation.CodeGenerator.greaterEqual;
import static b2bpl.translation.CodeGenerator.heap;
import static b2bpl.translation.CodeGenerator.ifThenElse;
import static b2bpl.translation.CodeGenerator.intToInt;
import static b2bpl.translation.CodeGenerator.isCallable;
import static b2bpl.translation.CodeGenerator.isEqual;
import static b2bpl.translation.CodeGenerator.isInRange;
import static b2bpl.translation.CodeGenerator.isInstanceOf;
import static b2bpl.translation.CodeGenerator.isNull;
import static b2bpl.translation.CodeGenerator.isOfType;
import static b2bpl.translation.CodeGenerator.less;
import static b2bpl.translation.CodeGenerator.lessEqual;
import static b2bpl.translation.CodeGenerator.libType;
import static b2bpl.translation.CodeGenerator.logicalAnd;
import static b2bpl.translation.CodeGenerator.logicalNot;
import static b2bpl.translation.CodeGenerator.logicalOr;
import static b2bpl.translation.CodeGenerator.map;
import static b2bpl.translation.CodeGenerator.map1;
import static b2bpl.translation.CodeGenerator.memberOf;
import static b2bpl.translation.CodeGenerator.modulo_int;
import static b2bpl.translation.CodeGenerator.divide_int;
import static b2bpl.translation.CodeGenerator.multiply;
import static b2bpl.translation.CodeGenerator.neg;
import static b2bpl.translation.CodeGenerator.nonNull;
import static b2bpl.translation.CodeGenerator.notEqual;
import static b2bpl.translation.CodeGenerator.nullLiteral;
import static b2bpl.translation.CodeGenerator.quantVarName;
import static b2bpl.translation.CodeGenerator.receiver;
import static b2bpl.translation.CodeGenerator.spmap;
import static b2bpl.translation.CodeGenerator.stack;
import static b2bpl.translation.CodeGenerator.stack1;
import static b2bpl.translation.CodeGenerator.stack2;
import static b2bpl.translation.CodeGenerator.sub;
import static b2bpl.translation.CodeGenerator.typ;
import static b2bpl.translation.CodeGenerator.type;
import static b2bpl.translation.CodeGenerator.var;
import static b2bpl.translation.CodeGenerator.wellformedHeap;
import static b2bpl.translation.CodeGenerator.wellformedStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import b2bpl.Project;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssignmentCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLBuiltInType;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLEnsuresClause;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLGotoCommand;
import b2bpl.bpl.ast.BPLHavocCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLModifiesClause;
import b2bpl.bpl.ast.BPLNullLiteral;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLRawCommand;
import b2bpl.bpl.ast.BPLRequiresClause;
import b2bpl.bpl.ast.BPLReturnCommand;
import b2bpl.bpl.ast.BPLSpecification;
import b2bpl.bpl.ast.BPLSpecificationClause;
import b2bpl.bpl.ast.BPLTransferCommand;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableDeclaration;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.ExceptionHandler;
import b2bpl.bytecode.IInstructionVisitor;
import b2bpl.bytecode.IOpCodes;
import b2bpl.bytecode.InstructionHandle;
import b2bpl.bytecode.JArrayType;
import b2bpl.bytecode.JBaseType;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.JNullType;
import b2bpl.bytecode.JType;
import b2bpl.bytecode.TypeLoader;
import b2bpl.bytecode.analysis.BasicBlock;
import b2bpl.bytecode.analysis.ControlFlowGraph;
import b2bpl.bytecode.analysis.Edge;
import b2bpl.bytecode.analysis.StackFrame;
import b2bpl.bytecode.bml.ast.BMLExpression;
import b2bpl.bytecode.bml.ast.BMLLoopModifiesClause;
import b2bpl.bytecode.bml.ast.BMLLoopSpecification;
import b2bpl.bytecode.bml.ast.BMLLoopVariant;
import b2bpl.bytecode.bml.ast.BMLMethodSpecification;
import b2bpl.bytecode.bml.ast.BMLSpecificationCase;
import b2bpl.bytecode.bml.ast.BMLStoreRef;
import b2bpl.bytecode.instructions.AALoadInstruction;
import b2bpl.bytecode.instructions.AAStoreInstruction;
import b2bpl.bytecode.instructions.AConstNullInstruction;
import b2bpl.bytecode.instructions.ALoadInstruction;
import b2bpl.bytecode.instructions.ANewArrayInstruction;
import b2bpl.bytecode.instructions.AReturnInstruction;
import b2bpl.bytecode.instructions.AStoreInstruction;
import b2bpl.bytecode.instructions.AThrowInstruction;
import b2bpl.bytecode.instructions.AbstractIfInstruction;
import b2bpl.bytecode.instructions.ArrayLengthInstruction;
import b2bpl.bytecode.instructions.CheckCastInstruction;
import b2bpl.bytecode.instructions.Dup2Instruction;
import b2bpl.bytecode.instructions.Dup2X1Instruction;
import b2bpl.bytecode.instructions.Dup2X2Instruction;
import b2bpl.bytecode.instructions.DupInstruction;
import b2bpl.bytecode.instructions.DupX1Instruction;
import b2bpl.bytecode.instructions.DupX2Instruction;
import b2bpl.bytecode.instructions.GetFieldInstruction;
import b2bpl.bytecode.instructions.GetStaticInstruction;
import b2bpl.bytecode.instructions.GotoInstruction;
import b2bpl.bytecode.instructions.IBinArithInstruction;
import b2bpl.bytecode.instructions.IBitwiseInstruction;
import b2bpl.bytecode.instructions.IIncInstruction;
import b2bpl.bytecode.instructions.ILoadInstruction;
import b2bpl.bytecode.instructions.INegInstruction;
import b2bpl.bytecode.instructions.IReturnInstruction;
import b2bpl.bytecode.instructions.IStoreInstruction;
import b2bpl.bytecode.instructions.IfACmpInstruction;
import b2bpl.bytecode.instructions.IfICmpInstruction;
import b2bpl.bytecode.instructions.IfInstruction;
import b2bpl.bytecode.instructions.IfNonNullInstruction;
import b2bpl.bytecode.instructions.IfNullInstruction;
import b2bpl.bytecode.instructions.InstanceOfInstruction;
import b2bpl.bytecode.instructions.InvokeInstruction;
import b2bpl.bytecode.instructions.InvokeInterfaceInstruction;
import b2bpl.bytecode.instructions.InvokeSpecialInstruction;
import b2bpl.bytecode.instructions.InvokeStaticInstruction;
import b2bpl.bytecode.instructions.InvokeVirtualInstruction;
import b2bpl.bytecode.instructions.LBinArithInstruction;
import b2bpl.bytecode.instructions.LBitwiseInstruction;
import b2bpl.bytecode.instructions.LCmpInstruction;
import b2bpl.bytecode.instructions.LLoadInstruction;
import b2bpl.bytecode.instructions.LNegInstruction;
import b2bpl.bytecode.instructions.LReturnInstruction;
import b2bpl.bytecode.instructions.LStoreInstruction;
import b2bpl.bytecode.instructions.LdcInstruction;
import b2bpl.bytecode.instructions.LocalVariableInstruction;
import b2bpl.bytecode.instructions.LookupSwitchInstruction;
import b2bpl.bytecode.instructions.MultiANewArrayInstruction;
import b2bpl.bytecode.instructions.NewArrayInstruction;
import b2bpl.bytecode.instructions.NewInstruction;
import b2bpl.bytecode.instructions.NopInstruction;
import b2bpl.bytecode.instructions.Pop2Instruction;
import b2bpl.bytecode.instructions.PopInstruction;
import b2bpl.bytecode.instructions.PutFieldInstruction;
import b2bpl.bytecode.instructions.PutStaticInstruction;
import b2bpl.bytecode.instructions.ReturnInstruction;
import b2bpl.bytecode.instructions.SwapInstruction;
import b2bpl.bytecode.instructions.TableSwitchInstruction;
import b2bpl.bytecode.instructions.VALoadInstruction;
import b2bpl.bytecode.instructions.VAStoreInstruction;
import b2bpl.bytecode.instructions.VCastInstruction;
import b2bpl.bytecode.instructions.VConstantInstruction;
import b2bpl.translation.helpers.ModifiedHeapLocation;
import de.unikl.bcverifier.Library;
import de.unikl.bcverifier.TranslationController;
import de.unikl.bcverifier.TranslationController.BoogiePlace;
import de.unikl.bcverifier.exceptionhandling.Traces;
import de.unikl.bcverifier.specification.Place;
import de.unikl.bcverifier.specification.SpecAssignment;
import de.unikl.bcverifier.specification.SpecExpr;


/**
 * The main entry point to the translation of a bytecode method to a BoogiePL
 * procedure.
 * 
 * <p>
 * Some aspects of the translation process can be configured by passing an
 * appropriate {@link Project} instance containing the desired translation
 * settings upon creating the {@code MethodTranslator}. In particular, the
 * following aspects of the translation can be configured:
 * <ul>
 * <li> The verification methodology for object invariants (see
 * {@link Project#isThisInvariantsOnly()}). </li>
 * <li> Whether to explicitly model runtime exceptions instead of ruling them
 * out by verification conditions (see
 * {@link Project#isModelRuntimeExceptions()}). </li>
 * </ul>
 * </p>
 * 
 * <p>
 * The {@code MethodTranslator} is responsible for the following aspects of the
 * translation process:
 * <ul>
 * <li> The translation of the individual bytecode instructions and the method's
 * program flow. </li>
 * <li> The generation of a set of assumptions justified by the JVM environment.
 * This mainly includes the translation of the method's static type information
 * but also the modeling of properties guaranteed by the JVM such as the fact
 * that the this object is never aliased at the beginning of a constructor.
 * </li>
 * <li> The translation of the local BML specifications such as assertions,
 * assumptions, and loop specifications. </li>
 * <li> The generation of proof obligations as well as assumptions as required
 * or justified, respectively, by the verification methodology used. This mainly
 * includes the handling of object invariants and method specifications
 * according to the verification methodology. </li>
 * </ul>
 * The actual translation of BML specification expressions and store references
 * is thereby not performed directly by the {@code MethodTranslator} but instead
 * delegated to the {@link SpecificationTranslator} and {@link ModifiesFilter}
 * classes, respectively.
 * </p>
 * 
 * @see Project#isThisInvariantsOnly()
 * @see Project#isModelRuntimeExceptions()
 * @see SpecificationTranslator
 * @see ModifiesFilter
 * 
 * @author Ovidio Mallo, Samuel Willimann
 */
public class MethodTranslator implements ITranslationConstants {

    /** The project containing the settings of the translation. */
    private final Project project;
    
    /** The translation controller used */
    private TranslationController tc;

    /**
     * The translation context to be used during the translation of the current
     * bytecode method.
     */
    private ITranslationContext context;

    /** The bytecode method currently being translated. */
    private BCMethod method;

    /**
     * The label of the current BoogiePL basic block or {@code null} if no such
     * block is active at the moment.
     */
    private String blockLabel;

    /**
     * A list for accumulating BoogiePL commands inside a basic block during the
     * translation.
     */
    private List<BPLCommand> commands;

    /**
     * A list for accumulating BoogiePL basic blocks inside the procedure during
     * the translation.
     */
    private List<BPLBasicBlock> blocks;

    /**
     * The variables which store a copy of the current heap at each loop header.
     * These variables are dynamically "allocated" during the translation of the
     * method.
     */
    private HashMap<BasicBlock, String> loopHeapVars;

    /**
     * The variables which store the value of loop variant expressions at each
     * loop header. These variables are dynamically "allocated" during the
     * translation of the method.
     */
    private HashMap<BMLLoopVariant, String> loopVariantVars;

    /**
     * Number of call statements in the current method. For every individual call
     * statement, a fresh set of variables (return state and return value) is
     * used.
     */
    private int callStatements = 0;

	private TypeLoader typeLoader;

	private ArrayList<String> blockComments;
    
    public void setTranslationController(TranslationController controller){
        this.tc = controller;
    }

    /**
     * Creates a new method translator which is configured by the given
     * {@code project}. Once a translator has been created, it can be used to
     * translate different bytecode methods under the same configuration (given by
     * the here provided {@code project}).
     * 
     * @param project The project containing the configurations of the
     *          translation.
     * 
     * @see #translate(ITranslationContext, BCMethod)
     */
    public MethodTranslator(TypeLoader typeLoader, Project project) {
    	this.typeLoader = typeLoader;
        this.project = project;
    }

    /**
     * Performs the actual translation of the given bytecode {@code method} and
     * returns a BoogiePL procedure representing it.
     * 
     * @param context The {@code TranslationContext} to be used for translating
     *          type/field/constant/... references.
     * @param method The bytecode method to be translated.
     * @return The BoogiePL procedure resulting from the translation of the given
     *         bytecode method.
     */
    public BPLProcedure translate(ITranslationContext context, BCMethod method) {
        this.context = context;
        this.method = method;
        initTranslation();
        translateInit();
        // translatePre();
        translateInstructions();
        translatePost();
        return buildProcedure(); 
    }

    /**
     * Builds and the return the actual BoogiePL procedure resulting from the
     * translation of the bytecode method. This method should be called once the
     * actual translation of the method body has finished.
     * 
     * @return The BoogiePL procedure resulting from the translation of the given
     *         bytecode method.
     */
    private BPLProcedure buildProcedure() {
        List<BPLVariableDeclaration> vars = new ArrayList<BPLVariableDeclaration>();

        // The local variables.
        for (int i = 0; i < method.getMaxLocals(); i++) {
            BPLVariable regr = new BPLVariable(refLocalVar(i), new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)));
            BPLVariable regi = new BPLVariable(intLocalVar(i), new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT));

            // vars.add(new BPLVariableDeclaration(regr, regi));
//            vars.add(filterVariableDeclarations(blocks, regr, regi));
            vars.add(new BPLVariableDeclaration(regr));
            vars.add(new BPLVariableDeclaration(regi));
        }

        // The stack variables.
        for (int i = 0; i < method.getMaxStack(); i++) {
            BPLVariable stackr = new BPLVariable(refStackVar(i), new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)));
            BPLVariable stacki = new BPLVariable(intStackVar(i), new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT));

            // vars.add(new BPLVariableDeclaration(stackr, stacki));
            //      vars.add(filterVariableDeclarations(blocks, stackr, stacki));
            vars.add(new BPLVariableDeclaration(stackr));
            vars.add(new BPLVariableDeclaration(stacki));
        }

        // Return variables for method calls
        for (int i = 0; i < callStatements; i++) {
            BPLVariable rvr = new BPLVariable(refReturnValueVar(i), new BPLTypeName(REF_TYPE));
            BPLVariable rvi = new BPLVariable(intReturnValueVar(i), BPLBuiltInType.INT);
            BPLVariable exr = new BPLVariable(exceptionVar(i), new BPLTypeName(REF_TYPE));

            vars.add(filterVariableDeclarations(blocks, rvr, rvi, exr));
        }

        /*
    // Helper variables for storing the return value of a method call.
    BPLVariable callResultr = new BPLVariable(REF_CALL_RESULT_VAR, BPLBuiltInType.REF);
    BPLVariable callResulti = new BPLVariable(INT_CALL_RESULT_VAR, BPLBuiltInType.INT);

    // vars.add(new BPLVariableDeclaration(callResultr, callResulti));
    vars.add(filterVariableDeclarations(blocks, callResultr, callResulti));
         */

        // Helper variables for swapping two values.
        BPLVariable swapr = new BPLVariable(REF_SWAP_VAR, new BPLTypeName(REF_TYPE));
        BPLVariable swapi = new BPLVariable(INT_SWAP_VAR, BPLBuiltInType.INT);

        // vars.add(new BPLVariableDeclaration(swapr, swapi));
        vars.add(filterVariableDeclarations(blocks, swapr, swapi));

        // The diverse heap variables being maintained.
        /*
    BPLVariable heap = new BPLVariable(tc.getHeap(), new BPLTypeName(HEAP_TYPE));
    BPLVariable oldHeap = new BPLVariable(OLD_tc.getHeap(), new BPLTypeName(HEAP_TYPE));
    BPLVariable preHeap = new BPLVariable(PRE_tc.getHeap(), new BPLTypeName(HEAP_TYPE));
    vars.add(new BPLVariableDeclaration(heap, oldHeap, preHeap));
    vars.add(filterVariableDeclarations(blocks, heap, oldHeap, preHeap));
         */

        // The variables which store a copy of the current heap at each loop header.
        // These variables are dynamically "allocated" during the translation of the
        // method.
        if (loopHeapVars.size() > 0) {
            List<BPLVariable> lhVars = new ArrayList<BPLVariable>();
            for (String loopHeapVar : loopHeapVars.values()) {
                lhVars.add(new BPLVariable(loopHeapVar, new BPLTypeName(HEAP_TYPE)));
            }
            vars.add(new BPLVariableDeclaration(lhVars.toArray(new BPLVariable[lhVars.size()])));
        }

        // The variables which store the value of loop variant expressions at each
        // loop header. These variables are dynamically "allocated" during the
        // translation of the method.
        if (loopVariantVars.size() > 0) {
            List<BPLVariable> lvVars = new ArrayList<BPLVariable>();
            for (String loopHeapVar : loopVariantVars.values()) {
                lvVars.add(new BPLVariable(loopHeapVar, BPLBuiltInType.INT));
            }
            vars.add(new BPLVariableDeclaration(lvVars.toArray(new BPLVariable[lvVars.size()])));
        }



        // Prepare list of input parameters
        JType[] paramTypes = method.getRealParameterTypes();
        boolean provideReturnValue = !method.isVoid() || method.isConstructor();

        BPLVariable[] inParams = new BPLVariable[paramTypes.length];
        //@deprecated inParams[0] = new BPLVariable(PRE_HEAP_VAR, new BPLTypeName(HEAP_TYPE));
        /*for (int i = 0; i < inParams.length; i++) {
            if(tc.isActive()){
                BPLType bplType = new BPLTypeName(VAR_TYPE, type(paramTypes[i]));
                vars.add(new BPLVariableDeclaration(new BPLVariable(paramVar(i, paramTypes[i]), bplType)));
            }
            BPLType bplType = type(paramTypes[i]);
            inParams[i] = new BPLVariable(paramVar(i, paramTypes[i]), bplType);
        }*/

        // Prepare list of output parameters
        // BPLVariable[] outParams = BPLVariable.EMPTY_ARRAY;
        List<BPLVariable> outParams = new ArrayList<BPLVariable>();
        //@deprecated outParams.add(new BPLVariable(RETURN_HEAP_PARAM, new BPLTypeName(HEAP_TYPE)));
//        if (provideReturnValue) {
//            if(tc.isActive()){
//                if (method.isConstructor()) {
//                    vars.add(new BPLVariableDeclaration(new BPLVariable(RESULT_PARAM + REF_TYPE_ABBREV, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)))));
//                } else {
//                    vars.add(new BPLVariableDeclaration(new BPLVariable(RESULT_PARAM + typeAbbrev(type(method.getReturnType())), new BPLTypeName(VAR_TYPE, type(method.getReturnType())))));
//                }
//            } else {
//                if (method.isConstructor()) {
//                    outParams.add(new BPLVariable(RESULT_PARAM + REF_TYPE_ABBREV, new BPLTypeName(REF_TYPE)));
//                } else {
//                    outParams.add(new BPLVariable(RESULT_PARAM + typeAbbrev(type(method.getReturnType())), type(method.getReturnType())));
//                }
//            }
//        }
        // always provide the result variables (because we need them in the invariants
        vars.add(new BPLVariableDeclaration(new BPLVariable(RESULT_PARAM + REF_TYPE_ABBREV, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)))));
        vars.add(new BPLVariableDeclaration(new BPLVariable(RESULT_PARAM + INT_TYPE_ABBREV, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT))));
        outParams.add(new BPLVariable(EXCEPTION_PARAM, new BPLTypeName(REF_TYPE)));

        // Build the different parts of the BoogiePL procedure.
        String name = getProcedureName(method);
        BPLImplementationBody body = new BPLImplementationBody(
                vars.toArray(new BPLVariableDeclaration[vars.size()]),
                blocks.toArray(new BPLBasicBlock[blocks.size()]));


        BPLImplementation implementation = new BPLImplementation(
                name,
                inParams,
                outParams.toArray(new BPLVariable[outParams.size()]),
                body);

        BPLSpecification spec = new BPLSpecification(
                getRequiresClauses(),
//                new BPLModifiesClause[] {
//                    translateModifiesClause(method, getInParameters())
//                }, /* getModifiesClauses(), */
                new BPLModifiesClause[0],
                getEnsuresClauses()
                );

        //System.out.println("[" + method.getName() + "]  " + translateMethodFrame(method, getInParameters()).toString());
        printSpecification(spec);

        return new BPLProcedure(
                name,
                inParams,
                outParams.toArray(new BPLVariable[outParams.size()]), spec, implementation
                );
    }


    private void printSpecification(BPLSpecification spec) {
        //System.out.println("Specification for " + method.getQualifiedName());
        for (BPLSpecificationClause clause : spec.getClauses()) {
            // TODO
            //System.out.println("\t" + clause.toString());
        }
    }


    /**
     * @return BPLRequiresClause declaring the precondition of the current procedure.
     */
    private BPLRequiresClause[] getRequiresClauses() {

        List<BPLRequiresClause> requiresClauses = new ArrayList<BPLRequiresClause>();
        JType[] params = method.getRealParameterTypes();

        // If we have a "this" object, then it must not be null.
        boolean hasThisParameter = !(method.isStatic() || method.isConstructor());

        if (method.isConstructor()) {
            //      requiresClauses.add(new BPLRequiresClause(logicalAnd(
            //         alive(
            //           rval(var(thisVar())),
            //           var(tc.getHeap())
            //         ),
            //         isOfType(
            //           rval(var(thisVar())),
            //           typeRef(params[0])
            //         ) /* ,
            //         notEqual(
            //           var(thisVar()),
            //           BPLNullLiteral.NULL
            //         )*/
            //       )));
        }
        else if (hasThisParameter) {
            //      requiresClauses.add(new BPLRequiresClause(logicalAnd(
            //        alive(
            //          rval(var(thisVar())),
            //          var(tc.getHeap())
            //        ),
            //        isInstanceOf(
            //          rval(var(thisVar())),
            //          typeRef(params[0])
            //        )
            //      )));
        }

        // For every method parameter, we do the following:
        //   - assume its type is a subtype of the static type
        //   - assume the parameter's value is alive
        //   - assign the parameter to the corresponding local variable in the stack frame  
        for (int i = (hasThisParameter ? 1 : 0); i < params.length; i++) {
            BPLExpression typeRef = typeRef(params[i]);
            if (!params[i].isReferenceType()) {
                // Base type: There is no need to assume aliveness of base types.
                //requiresClauses.add(new BPLRequiresClause(isOfType(
                //  ival(var(paramVar(i))),
                //  typeRef)));
            } else {
                if (!method.isConstructor()) {
                    //          requiresClauses.add(new BPLRequiresClause(logicalAnd(
                    //            alive(
                    //              rval(var(paramVar(i, params[i]))),
                    //              var(tc.getHeap())
                    //            ),
                    //            isInstanceOf( // isOfType
                    //              rval(var(paramVar(i, params[i]))),
                    //              typeRef
                    //            )
                    //          )));
                }
            }
            // addAssignment(var(localVar(i, params[i])), var(paramVar(i)));
        }

        // Special handling for constructors.
        if (method.isConstructor()) {
            // The JVM guarantees us that the this object is not aliased at the
            // beginning of a constructor, so let's assume that.

            // No parameter is equal to the "this" object.
            for (int i = 1; i < params.length; i++) {
                // We only insert the appropriate assumption for types which are
                // compatible to the type of the this object since other assumptions are
                // redundant.
                if (method.getOwner().isSubtypeOf(params[i]) || params[i].isSubtypeOf(method.getOwner())) {
                    requiresClauses.add(new BPLRequiresClause(notEqual(
                            var(thisVar()),
                            var(localVar(i, params[i]))
                            )));
                }
            }

            // No object in the heap is equal to the this object.
            /* TODO revise the following two requirements
      String l = quantVarName("l");
      BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
      requiresClauses.add(new BPLRequiresClause(forall(lVar, notEqual(
          rval(var(thisVar())),
          get(var(tc.getHeap()), var(l))))));

      // Initialize the fields of the this object to their default values.
      String f = quantVarName("f");
      BPLVariable fVar = new BPLVariable(f, BPLBuiltInType.NAME);
      requiresClauses.add(new BPLRequiresClause(forall(fVar, isEqual(get(
          var(tc.getHeap()),
          fieldLoc(var(thisVar()), var(f))), initVal(fieldType(var(f)))))));
             */
        }


        // Assume the appropriate invariants if it is not a constructor.
        if (method.isConstructor()) {
            requiresClauses.add(new BPLRequiresClause(this.getInvariantUponEnteringConstructor()));
        } else { 
            // TODO
            // requiresClauses.add(requireAllInvariants(method.isConstructor()));
            requiresClauses.add(new BPLRequiresClause(this.getInvariantUponEnteringMethod()));
        }

        // Assume the method's effective precondition.
//        requiresClauses.add(new BPLRequiresClause(translatePrecondition(
//                method,
//                getInParameters())));

        /*
         * if (!method.isStatic()) {
         *   BPLExpression this_type = typeRef(method.getOwner());
         *   BPLExpression this_not_null = notEqual(rval(var(thisVar())), BPLNullLiteral.NULL);
         *   BPLExpression this_has_correct_type = isSubtype(typ(rval(var(thisVar()))), this_type);
         *   // TODO: C == type of "this" object
         *   result = logicalAnd(result, this_not_null, this_has_correct_type);
         * }
         */

        return requiresClauses.toArray(new BPLRequiresClause[requiresClauses.size()]);
    }

    /**
     * Returns an expression containing the preconditional invariant
     * for a constructor method call.
     * We can safely assume that all invariants hold, 
     * because the "this" object is not yet an instance of any type,
     * and hence its invariant holds.
     * @return BPLExpression invariant (upon entering constructor)
     */
    private BPLExpression getInvariantUponEnteringConstructor() {
        // return BPLBoolLiteral.TRUE;

        String o = quantVarName("o");
        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));

        String t = quantVarName("t");
        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));

        // return forall(oVar, tVar, implies(notEqual(var(o), var(thisVar())), inv(var(t), var(o), var(tc.getHeap()))));
        //    if (project.performInvariantChecks()) {    
        //      return forall(
        //          oVar, tVar,
        //          implies(
        //              logicalAnd(
        //                  alive(rval(var(o)), var(tc.getHeap())),
        //                  isSubtype(var(t), typ(rval(var(o)))),
        //                  (method.isConstructor()) ? notEqual(var(o), var(thisVar())) : BPLBoolLiteral.TRUE
        //              ),
        //              inv(var(t), var(o), var(tc.getHeap()))
        //          )
        //      );
        //    } else {
        return BPLBoolLiteral.TRUE;
        //    }

    }

    /**
     * Returns an expression stating that all invariants hold.
     * @return BPLExpression invariant (upon entering method)
     */
    private BPLExpression getInvariantUponEnteringMethod() {
        String o = quantVarName("o");
        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));

        String t = quantVarName("t");
        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));

        //    if (project.performInvariantChecks()) {
        //      return forall(
        //          oVar, tVar,
        //              implies(
        //                  logicalAnd(
        //                      alive(rval(var(o)), var(tc.getHeap())),
        //                      isSubtype(var(t), typ(rval(var(o))))
        //                  ),
        //                  inv(var(t), var(o), var(tc.getHeap()))
        //              )
        //          );
        //    } else {
        return BPLBoolLiteral.TRUE;
        //    }
    }

    /**
     * Returns an expression stating that, for all modified class fields,
     * their invariant must hold.
     * @return BPLExpression object invariant (only referring to modified class fields)
     */
    private BPLExpression getInvariantBeforeLeavingMethod() {
        BPLExpression isModifiedVar = BPLBoolLiteral.TRUE;
        for (BPLVariableExpression v : modifiedVariables) {
            // isModifiedVar = logicalAnd(isModifiedVar, inv(var(s.getType().toString()), var(s.getName()), var(tc.getHeap())));
            //         isModifiedVar = logicalAnd(isModifiedVar, inv(typ(rval(v)), v, var(tc.getHeap())));
        }

        //    if (project.performInvariantChecks()) {
        //      return isModifiedVar;
        //    } else {
        return BPLBoolLiteral.TRUE;
        //    }

        // return forall(oVar, tVar, implies(notInModVars, inv(var(t), var(o), var(tc.getHeap()))));
    }


    private BPLExpression getInvariantAfterLeavingMethod() {

        //    for (BPLVariableExpression s : modifiedVariables) {
        //      System.out.println("\t" + s.getIdentifier() /* + "\t" + s.getType().toString() */);
        //    }

        String o = quantVarName("o");
        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));

        String t = quantVarName("t");
        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));

        BPLExpression isUnmodifiedVar = BPLBoolLiteral.TRUE;

        //    for (BPLVariableExpression v : modifiedVariables) {
        //      isUnmodifiedVar = logicalAnd(isUnmodifiedVar, logicalOr(notEqual(var(o), v), notEqual(var(t), typ(rval(v)))));
        //    }

        //    if (project.performInvariantChecks()) {
        //      return forall(oVar, tVar, implies(isUnmodifiedVar, inv(var(t), var(o), var(tc.getHeap()))));
        //    } else {
        return BPLBoolLiteral.TRUE;
        //    }

        /*

    BPLExpression notInModVars = BPLBoolLiteral.FALSE;
    for (BPLExpression ref : method.getModifiedObjectRefs()) {
      if (!method.isModifiedObjectRef(ref)) {
        notInModVars = logicalOr(notInModVars, isEqual(var(t), var(VALUE_TYPE_PREFIX + ref)));
      }
    }

    return forall(oVar, tVar,
        implies(notInModVars, inv(var(t), var(o), var(tc.getHeap())))
    ); */
    }


    /*
     * @return BPLMeasureClauses declaring global variables that are modified in
     *         the procedure.
     */
    @Deprecated
    private BPLModifiesClause[] getModifiesClauses() {

        return null;

        /*
    List<BPLModifiesClause> modifiesClauses = new ArrayList<BPLModifiesClause>();

    // TODO read modified variables from BPL file
    translateModifiesClause(method, get)

    modifiesClauses.add(new BPLModifiesClause(var(tc.getHeap())));

    return modifiesClauses.toArray(new BPLModifiesClause[modifiesClauses.size()]);
         */
    }


    /**
     * @return BPLEnsuresClause List of ensures-clauses declaring the postcondition of the current procedure.
     */
    private BPLEnsuresClause[] getEnsuresClauses() {

        List<BPLEnsuresClause> ensuresClauses = new ArrayList<BPLEnsuresClause>();

//        // Prepare precondition (since the postcondition is always implied by the precondition)
//        /*
//    BPLExpression P = translatePrecondition(
//        method,
//        getInParameters()
//    );
//         */
//
//        // Assert the effective normal postcondition and the method frame of the method.
//        BPLExpression Q = translatePostcondition(
//                method,
//                RESULT_VAR,
//                getInParameters()
//                );
//
//        /*
//    BPLExpression FC = translateMethodFrame(
//        method,
//        getInParameters()
//    );
//         */
//
//        // create clause to ensure aliveness of all objects
//        // (if an object was alive on the heap prior to the method call,
//        // it will be alive on the heap after the method call as well)
//        //    String v = quantVarName("v");
//        //    String l = quantVarName("l");
//        //    BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
//        //    BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
//        //    BPLExpression guaranteeAliveness = forall(
//        //        vVar,
//        //        implies(
//        //            alive(var(v), old(var(tc.getHeap()))),
//        //            alive(var(v), var(tc.getHeap()))
//        //        )
//        //    );
//
//        //    BPLExpression allModifiedHeapLocations = BPLBoolLiteral.TRUE;
//        //    for (ModifiedHeapLocation mhl : modifiedHeapLocations) {
//        //        allModifiedHeapLocations = logicalAnd(
//        //            // TODO replace
//        //            notEqual(var(l), mhl.getLocation()),
//        //            allModifiedHeapLocations
//        //        );
//        //    }
//        //    BPLExpression guaranteeValues = forall(
//        //        lVar,
//        //        implies(
//        //          allModifiedHeapLocations,
//        //          implies(
//        //              alive(rval(obj(var(l))), old(var(tc.getHeap()))), // only consider objects which were allocated on the old heap
//        //              isEqual(
//        //                  get(var(tc.getHeap()), var(l)),
//        //                  get(old(var(tc.getHeap())), var(l))
//        //              )
//        //          )
//        //        )
//        //    );
//
//        // If no method specifications are provided (BPLBoolLiteral.TRUE),
//        // establish default frame condition
//        /*
//    if (FC == BPLBoolLiteral.TRUE) {
//      String r = quantVarName("r");
//      BPLVariable ref = new BPLVariable(r, BPLBuiltInType.REF);
//      FC = forall(
//          ref,
//          implies(
//              alive(rval(var(r)), old(var(tc.getHeap()))),
//              logicalAnd(
//                  isEqual(
//                      get(var(tc.getHeap()), fieldLoc(var(r), typ(rval(var(r))))),
//                      get(old(var(tc.getHeap())), fieldLoc(var(r), typ(rval(var(r)))))
//                  ),
//                  alive(rval(var(r)), var(tc.getHeap()))
//              )
//          )
//      );
//    }*/
//
//        // Does this method have a return value. Note that constructors have an
//        // implicit return value as well (the instantiated object).
//        boolean provideReturnValue = !method.isVoid() || method.isConstructor();
//
//        // Ensure normal postcondition
//        BPLExpression returnStateCondition = isNormalReturnState(var(RETURN_STATE_PARAM));
//        BPLExpression qResult = BPLBoolLiteral.TRUE;
//
//        //    if (method.isConstructor()) {
//        //      qResult = logicalAnd(
//        //            Q,
//        //            alive(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), var(tc.getHeap())),
//        //            isInstanceOf(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), typeRef(method.getOwner())),
//        //            notEqual(var(RESULT_PARAM + REF_TYPE_ABBREV), BPLNullLiteral.NULL)//,
//        ////            guaranteeAliveness,
//        ////            guaranteeValues
//        //      );
//        //    } else {
//        //      if (provideReturnValue) {
//        //        if (method.getReturnType().isReferenceType()) {
//        //          // The method's return value is a reference type (ref)
//        //          qResult = logicalAnd(
//        //            // isNormalReturnState(var(RETURN_STATE_PARAM)),
//        //            Q,
//        //            alive(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), var(tc.getHeap())),
//        //            isOfType(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), typeRef(method.getReturnType())));
//        //        } else {
//        //          // The method's return value is not a reference type
//        //          // (in this particular case, we assume type int)
//        //          qResult = logicalAnd(
//        //            // isNormalReturnState(var(RETURN_STATE_PARAM)),
//        //            Q,
//        //            alive(ival(var(RESULT_PARAM + typeAbbrev(type(method.getReturnType())))), var(tc.getHeap())),
//        //            isOfType(ival(var(RESULT_PARAM + typeAbbrev(type(method.getReturnType())))), typeRef(method.getReturnType())));
//        //        }
//        //      } else {
//        //        qResult = Q;
//        //      }
//        //    }
//
//        // Add postcondition for normal method termination
//        if (method.getExceptionTypes().length > 0) {
//            // If there are exceptions associated with the method,
//            // the "normal" method termination must be stated explicitely
//            ensuresClauses.add(new BPLEnsuresClause(
//                    //    implies(
//                    //        P,
//                    implies(returnStateCondition, qResult /* instead of logicalAnd(FC, qResult) */)
//                    //    )
//                    ));
//        } else{
//            // If there are no exceptions associated with the method,
//            // the "normal" method termination does not need to be indicated
//            // explicitely, for every method termination is "normal"
//            // (that is if we abstain from unchecked exceptions)
//            ensuresClauses.add(new BPLEnsuresClause(qResult));
//            //    implies(
//            //        P,
//            //        logicalAnd(FC, qResult)
//            //    )
//        }
//
//        // Handle the different exceptional terminations of the method.
//        JClassType[] exceptions = method.getExceptionTypes();
//        for (JClassType exception : exceptions) {
//
//            // addAssume(isInstanceOf(rval(var(refStackVar(0))), typeRef(exception)));
//
//            BPLExpression Qex = translateXPostcondition(
//                    method,
//                    exception,
//                    refStackVar(0),
//                    getInParameters()
//                    );
//
//            BPLExpression exceptionCondition = logicalAnd(
//                    isExceptionalReturnState(var(RETURN_STATE_PARAM)),
//                    isOfType(var(EXCEPTION_PARAM), typeRef(exception))
//                    );
//            //      BPLExpression qException =  logicalAnd(Qex,
//            //          alive(rval(var(EXCEPTION_PARAM)), var(tc.getHeap()))
//            //      );
//
//            // Add exceptional postcondition for this particular exception
//            //      ensuresClauses.add(new BPLEnsuresClause(
//            //      //    implies(
//            //      //        P,
//            //              implies(
//            //                  exceptionCondition,
//            //                  qException
//            //                  // logicalAnd(FC, qException)
//            //              )
//            //      //    )
//            //      ));
//        }
//
//        // Remove redundant (empty) clauses
//        if (project.simplifyLogicalExpressions()) {
//            for (int i = ensuresClauses.size() - 1; i >= 0; i--) {
//                if (ensuresClauses.get(i).getExpression() == BPLBoolLiteral.TRUE) {
//                    ensuresClauses.remove(i);
//                }
//            }
//        }
//
//        // Ensure exposed (object) invariants
//        // ensuresClauses.add(ensureExposedInvariants(false));
//        ensuresClauses.add(new BPLEnsuresClause(this.getInvariantBeforeLeavingMethod()));

        return ensuresClauses.toArray(new BPLEnsuresClause[ensuresClauses.size()]);
    }


    /**
     * Filters only variable declarations for variables which actually appear in
     * the procedure implementation.
     * 
     * @param blocks List of blocks of the current procedure.
     * @param vars List of BPLVariables which might be used in this procedure.
     * @return BPLVariableDeclaration declaring all variables which actually
     *         appear in the implementation.
     */
    private BPLVariableDeclaration filterVariableDeclarations(
            List<BPLBasicBlock> blocks,
            BPLVariable... vars) {
        List<BPLVariable> new_vars = new ArrayList<BPLVariable>();
        for (BPLBasicBlock block : blocks) {
            for (BPLCommand command : block.getCommands()) {
                for (BPLVariable var : vars) {
                    if (command.toString().contains(var.getName())
                            && !new_vars.contains(var)) {
                        new_vars.add(var);
                    }
                }
            }
        }
        return new BPLVariableDeclaration(new_vars.toArray(new BPLVariable[new_vars.size()]));
    }


    /**
     * Returns the name to use for the BoogiePL procedure resulting from the
     * translation of the given bytecode {@code method}. The returned string is a
     * valid BoogiePL identifier and it is guaranteed to be different from the
     * procedure name returned for any different method (including overloaded
     * methods).
     * 
     * @param method The bytecode method for which to return the BoogiePL
     *          procedure name.
     * @return The BoogiePL procedure name.
     */
    public static String getProcedureName(BCMethod method) {
        String name;

        // The names of constructors and class initializers used in the JVM are not
        // valid BoogiePL identifiers, so we give them different names which may
        // not clash with names of ordinary methods.
        if (method.isConstructor()) {
            name = method.getOwner().getName() + "." + CONSTRUCTOR_NAME;
        } else if (method.isClassInitializer()) {
            name = method.getOwner().getName() + "." + CLASS_INITIALIZER_NAME;
        } else {
            name = method.getQualifiedName();
        }
        
        if(!method.isVoid()){
            name += "#" + method.getReturnType().getName();
        }

        // Append the names of the method's parameters in order to correctly handle
        // overloaded methods.
        for (JType type : method.getParameterTypes()) {
            name += "$";
            if (type.isArrayType()) {
                // For array types, we append the array's component type name followed
                // by the array's dimension.
                JArrayType arrayType = (JArrayType)type;
                JType componentType = arrayType.getComponentType();
                name += componentType.getName() + "#" + arrayType.getDimension();
            } else {
                name += type.getName();
            }
        }

        return name;
    }

    public static String getMethodName(BCMethod method) {
        String name;

        // The names of constructors and class initializers used in the JVM are not
        // valid BoogiePL identifiers, so we give them different names which may
        // not clash with names of ordinary methods.
        if (method.isConstructor()) {
            name = "." + CONSTRUCTOR_NAME + "#" + method.getOwner().getName();
        } else if (method.isClassInitializer()) {
            name = "." + CLASS_INITIALIZER_NAME;
        } else if(method.isStatic()) {
            name = method.getOwner().getName() + "." + method.getName();
        } else {
            name = method.getName();
        }
        
        if(!method.isVoid()){
            name += "#" + method.getReturnType().getName();
        }

        // Append the names of the method's parameters in order to correctly handle
        // overloaded methods.
        for (JType type : method.getParameterTypes()) {
            name += "$";
            if (type.isArrayType()) {
                // For array types, we append the array's component type name followed
                // by the array's dimension.
                JArrayType arrayType = (JArrayType)type;
                JType componentType = arrayType.getComponentType();
                name += componentType.getName() + "#" + arrayType.getDimension();
            } else {
                name += type.getName();
            }
        }

        return name;
    }

    /**
     * Convenience method returning the names of the in-parameter names of the
     * BoogiePL procedure being generated.
     * 
     * @return The names of the procedure's in-parameters.
     */
//    private String[] getInParameters() {
//        JType[] paramTypes = method.getRealParameterTypes();
//        String[] params = new String[paramTypes.length];
//        for (int i = 0; i < params.length; i++) {
//            params[i] = paramVar(i, paramTypes[i]);
//        }
//        return params;
//    }

    /**
     * Generates a set of assumptions justified by the JVM and its type system. In
     * addition, the initialization of the first stack frame of the method is
     * translated to BoogiePL. The information assumed at this point and
     * guaranteed by the JVM includes the following:
     * <ul>
     * <li> The this object, if any, is never {@code null}. </li>
     * <li> The types of the parameter values are subtypes of their static types
     * and all reachable values are alive. </li>
     * <li> If we are inside a constructor, the this object is not aliased and its
     * instance fields are initialized by their default values. </li>
     * </ul>
     */
    private void translateInit() {

        aliasMap.clear();
        modifiedVariables.clear();
        modifiedHeapLocations.clear();


        // constructors return an initialized "this"-parameter,
        // so we add an alias telling that "param0" actually refers to "result"
        // in the constructor's postcondition
        BPLType t = new BPLTypeName(method.getReturnType().getInternalName());
        if (method.isConstructor()) addAlias(RESULT_PARAM + typeAbbrev(type(method.getReturnType())), thisVar());

        startBlock(INIT_BLOCK_LABEL);
        
        blockComments.add(Traces.makeComment("call to " + method.getQualifiedName()));
        

        callStatements = 0; // count the number of call statements used so far

        /*
         * // Keep a copy of the method's pre-state heap.
         * addAssignment(var(OLD_HEAP_VAR), var(tc.getHeap()));
         *  // If we have a this object, then it is not null. if
         * (!method.isStatic()) { addAssume(nonNull(var(thisVar()))); }
         *  // For every method parameter, we do the following: // - assume its type
         * is a subtype of the static type // - assume the parameter's value is
         * alive // - assign the parameter to the corresponding local variable in
         * the stack // frame JType[] params = method.getRealParameterTypes(); for
         * (int i = 0; i < params.length; i++) { BPLExpression typeRef =
         * typeRef(params[i]); if (params[i].isBaseType()) { // There is no need to
         * assume aliveness of value types.
         * addAssume(isOfType(ival(var(paramVar(i))), typeRef)); } else {
         * addAssume(alive(rval(var(paramVar(i))), var(tc.getHeap())));
         * addAssume(isOfType(rval(var(paramVar(i))), typeRef)); }
         * addAssignment(var(localVar(i, params[i])), var(paramVar(i))); }
         *  // Special handling for constructors. if (method.isConstructor()) { //
         * The JVM guarantees us that the this object is not aliased at the //
         * beginning of a constructor, so let's assume that.
         *  // No parameter is equal to the this object. for (int i = 1; i <
         * params.length; i++) { // We only insert the appropriate assumption for
         * types which are // compatible to the type of the this object since other
         * assumptions are // redundant. if
         * (method.getOwner().isSubtypeOf(params[i]) ||
         * params[i].isSubtypeOf(method.getOwner())) {
         * addAssume(notEqual(var(thisVar()), var(paramVar(i)))); } }
         *  // No object in the heap is equal to the this object. String l =
         * quantVarName("l"); BPLVariable lVar = new BPLVariable(l, new
         * BPLTypeName(LOCATION_TYPE)); addAssume(forall( lVar,
         * notEqual(rval(var(thisVar())), get(var(tc.getHeap()), var(l)))));
         *  // Initialize the fields of the this object to their default values.
         * String f = quantVarName("f"); BPLVariable fVar = new BPLVariable(f,
         * BPLBuiltInType.NAME); addAssume(forall( fVar, isEqual( get(var(tc.getHeap()),
         * fieldLoc(var(thisVar()), var(f))), initVal(fieldType(var(f)))))); }
         * 
         * endBlock(PRE_BLOCK_LABEL);
         */

        // Assume the appropriate invariants.
        // [SW]: REMOVED (this is checked implicitely)
        // assumeAllInvariants(method.isConstructor());

        // Assume the method's effective precondition.
        // [SW]: REMOVED (this is checked implicitely)
        // addAssume(translatePrecondition(method, getInParameters()));

        //@deprecated addAssume(notEqual(var(thisVar()), BPLNullLiteral.NULL));
        //@deprecated addAssume(alive(rval(var(thisVar())), var(tc.getHeap())));

        JType[] params = method.getParameterTypes();
        if(tc.isActive()){
        	if (!method.isStatic()) {
        		addAssume(nonNull(stack(var(thisVar()))));
        		addAssume(isOfType(stack(var(localVar(0, method.getOwner()))), var(tc.getHeap()), context.translateTypeReference(method.getOwner())));
        	}
        }

        for (int i = 0; i < params.length; i++) {
            if(tc.isActive()){
                if(params[i].isBaseType()){
                    JBaseType baseType = (JBaseType)params[i];
                    addAssume(isInRange(stack(var(localVar(method.isStatic() ? i : i+1, baseType))), typeRef(baseType)));
                } else if(params[i].isClassType()){
                    JClassType classType = (JClassType)params[i];
                    addAssume(isOfType(stack(var(localVar(method.isStatic() ? i : i+1, classType))), var(tc.getHeap()), context.translateTypeReference(classType)));
//                    addAssume(obj(var(tc.getHeap()), stack(var(paramVar(i, classType))))); //TODO this would mean all parameters are non-null
                } else {
                    //TODO arrays
                }
                // special case static methods. reg_i == param_(i+1)
//                if(method.isStatic()){
//                    if(i>0){
//                        addAssignment(stack(var(localVar(i-1, params[i]))), stack(var(paramVar(i, params[i]))), "init " + localVarName(i-1));
//                    }
//                } else {
//                    addAssignment(stack(var(localVar(i, params[i]))), stack(var(paramVar(i, params[i]))), "init " + localVarName(i));
//                }
            } else {
//                addAssignment(var(localVar(i, params[i])), var(paramVar(i, params[i])));
            }
        }

        //@deprecated addAssignment(var(tc.getHeap()), var(PRE_HEAP_VAR));

        // requires param0 != null;
        // requires alive(rval(param0), heap);

        endBlock(method.getCFG().getEntryBlock().outEdgeIterator().next());
    }

    private String localVarName(int i) {
        String name = method.getParameterNames()[i]; 
        if(name != null){
            return name;
        } else {
            return "unkonwn variable";
        }
    }
    
    private String localVarName(LocalVariableInstruction insn){
        String name = insn.getVariableName(); 
        if(name != null){
            return name;
        } else {
            return "unkonwn variable";
        }
    }

    /**
     * Initializes the internal state of the translator. Should be called whenever
     * a new bytecode method is translated.
     */
    private void initTranslation() {
        blockLabel = null;
        commands = null;
        blocks = new ArrayList<BPLBasicBlock>();
        loopHeapVars = new LinkedHashMap<BasicBlock, String>();
        loopVariantVars = new LinkedHashMap<BMLLoopVariant, String>();
    }

    /**
     * Generates a set of assumptions justified by the verification methodology
     * employed. In particular, the following is assumed at the beginning of the
     * method:
     * <ul>
     * <li> The invariants of all objects, eventually excluding the this object in
     * case we are inside a constructor. </li>
     * <li> The method's effective precondition. </li>
     * </ul>
     * @deprecated
     */
    @Deprecated
    private void translatePre() {
        startBlock(PRE_BLOCK_LABEL);

        // Assume the appropriate invariants.
        assumeAllInvariants(method.isConstructor());

        // Assume the method's effective precondition.
//        addAssume(translatePrecondition(method, getInParameters()));

        endBlock(method.getCFG().getEntryBlock().outEdgeIterator().next());
    }

    /**
     * Generates a set of assertions for normal as well as exceptional
     * terminations of the method being translated in order to enforce the desired
     * verification methodology. In particular, the following assertions need to
     * be satisfied when the method terminates:
     * <ul>
     * <li> The method's effective (exceptional) postcondition must hold. </li>
     * <li> The invariants of the method's receiver type must hold for all
     * relevant objects, even if the method terminates with an exception. </li>
     * <li> The method's frame condition must hold, even if the method terminates
     * with an exception. </li>
     * </ul>
     */
    private void translatePost() {

        startBlock(EXCEPTION_HANDLERS_LABEL);

        /*
         * // Handle the normal termination of the method.
         * startBlock(POST_BLOCK_LABEL);
         *  // Assert the effective normal postcondition of the method. // TODO[sw]:
         * remove this check and insert appropriate "ensures" clause in the //
         * procedure declaration addAssert(translatePostcondition( method,
         * OLD_HEAP_VAR, RESULT_VAR, getInParameters()));
         * endBlock(EXIT_BLOCK_LABEL);
         */

        // Handle the different exceptional terminations of the method.
        JClassType[] exceptions = method.getExceptionTypes();
        for (JClassType exception : exceptions) {
            startBlock(postXBlockLabel(exception));

            // addAssume(isInstanceOf(rval(var(refStackVar(0))), typeRef(exception)));
            addAssume(isOfType(var(EXCEPTION_PARAM), var(tc.getHeap()), typeRef(exception)));
            //      addAssume(alive(rval(var(EXCEPTION_PARAM)), var(tc.getHeap())));

            /*
             * REMOVE: exceptional postconditions are checked implicitely by Boogie
             * addAssert(translateXPostcondition( method, exception, OLD_HEAP_VAR,
             * refStackVar(0), getInParameters()));
             */
            endBlock(EXIT_BLOCK_LABEL);
        }

        // The exit block contains all the verification conditions which must hold
        // even if the method terminates with an exception, namely:
        // - the invariants of the relevant objects
        // - the method's frame condition
        startBlock(EXIT_BLOCK_LABEL);

        /*
         * assertExposedInvariants(false);
         *  // Assert the method's frame condition.
         * addAssert(translateMethodFrame(method, OLD_HEAP_VAR, getInParameters()));
         */

        //@deprecated addAssignment(var(RETURN_HEAP_PARAM), var(tc.getHeap()));

        boolean provideReturnValue = !method.isVoid() || method.isConstructor();

        if (provideReturnValue) {

            JType retType = method.isConstructor()
                    ? method.getOwner()
                            : method.getReturnType();

                    
                    BPLExpression topElem = stack(var(resVar(retType)));
                    
                    if(tc.isActive() && method.isConstructor()){
                        addAssignment(topElem, stack(var(localVar(0, retType))));
                    }

                    if (method.getReturnType().isReferenceType() || method.isConstructor()) {
                        //        addAssume(alive(rval(topElem), var(tc.getHeap())));
                        addAssume(isOfType(topElem, var(tc.getHeap()), typeRef(retType)));
                    } else {
                        //        addAssume(alive(ival(topElem), var(tc.getHeap())));
                        //        addAssume(isOfType(topElem, var(VALUE_TYPE_PREFIX + JBaseType.INT.toString())));
                        addAssume(isInRange(topElem, typeRef(retType)));
                    }

//                    if(tc.isActive()){
//                        addAssignment(stack(var(RESULT_PARAM + typeAbbrev(type(retType)))), stack(var(stackVar(0, retType))));
//                    } else {
//                        addAssignment(var(RESULT_PARAM + typeAbbrev(type(retType))), stack(var(stackVar(0, retType))));
//                    }

        }
        
        String methodName = MethodTranslator.getMethodName(method);
//        addCommand(new BPLAssumeCommand(isEqual(stack(var(PLACE_VARIABLE)), var(tc.buildPlace(getProcedureName(method), true)))));
        addCommand(new BPLAssumeCommand(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX+methodName))));
        addAssignment(stack(var(PLACE_VARIABLE)), var(tc.buildPlace(method, true)));
        
        String currentLabel = blockLabel;
        
        String boundaryReturnLabel = currentLabel + "_boundary_return";
        String internReturnLabel = currentLabel + "_intern_return";
        endBlock(boundaryReturnLabel, internReturnLabel);
        
        startBlock(boundaryReturnLabel);
        blockComments.add(Traces.makeComment("boundary return"));
        addAssume(logicalAnd(isEqual(spmap(), intLiteral(0)), isEqual(modulo_int(var(tc.getInteractionFramePointer()), new BPLIntLiteral(2)), new BPLIntLiteral(1))));

        if(method.isConstructor()){
            rawEndBlock(tc.getNextConstructorLabel());
        } else {
            rawEndBlock(tc.getCheckLabel());
        }
        
        String retTableLabel = tc.prefix("rettable");
        startBlock(internReturnLabel);
        blockComments.add(Traces.makeComment("internal return"));
        addAssume(logicalAnd(greater(spmap(), intLiteral(0)), isEqual(modulo_int(var(tc.getInteractionFramePointer()), new BPLIntLiteral(2)), new BPLIntLiteral(1))));
        rawEndBlock(retTableLabel);
    }

    /**
     * Translates the method's bytecode instructions along with all the local BML
     * annotations (assertions, loop specifications, ...). The translation of the
     * program flow follows the method's control flow graph.
     */
    private void translateInstructions() {
        InstructionTranslator insnTranslator = new InstructionTranslator();
        ControlFlowGraph cfg = method.getCFG();
        Iterator<BasicBlock> cfgBlocks = cfg.blockIterator();
        while (cfgBlocks.hasNext()) {
            BasicBlock cfgBlock = cfgBlocks.next();
            // Filter out the synthetic entry and exit blocks from the control flow
            // graph.
            if (!cfg.isSyntheticBlock(cfgBlock)) {
                translateCFGBlockStart(cfgBlock);
                // Let the instruction translator know which is the current basic block
                // in the control flow graph.
                insnTranslator.cfgBlock = cfgBlock;
                Iterator<InstructionHandle> insns = cfgBlock.instructionIterator();
                while (insns.hasNext()) {
                    InstructionHandle insn = insns.next();
                    // Translate local annotations such as assertions and assumptions.
                    // Note that loop specifications are not translated here as they may
                    // only appear at the beginning of a basic block in the control flow
                    // graph and not at any arbitrary instruction.
                    // FIXME the following method generates assertions that are not
                    //       explicitely specified in the BML file.
                    // translateInstructionAnnotations(insn);
                    // Let the instruction translator know which is the current
                    // instruction handle before doing the actual translation.
                    if(insn.getSourceLine() != -1){
                        List<Place> localPlaces = tc.getLocalPlacesBetween(insn.getSourceLine()-1, insn.getSourceLine(), method.getOwner().getName());
                        if(localPlaces != null){
                            insnTranslator.translateLocalPlaces(localPlaces);
                        }
                        
                        addCommand(new BPLRawCommand("// " + Traces.makeComment(method.getOwner().getName(), insn.getSourceLine(), "(trace position)")));
                    }
                    insnTranslator.handle = insn;
                    insn.accept(insnTranslator);
                }

                // If we are still inside a BoogiePL block after having translated all
                // the instructions inside a basic block of the control flow graph, we
                // must have a fall through edge in the control flow graph which is not
                // translated by the individual instructions but here instead.
                if (isInsideBlock()) {
                    InstructionHandle nextInsn = cfgBlock.getLastInstruction().getNext();
                    BasicBlock nextBlock = method.getCFG().findBlockStartingAt(nextInsn);
                    Edge fallThroughEdge = cfgBlock.getSuccessorEdge(nextBlock);
                    endBlock(fallThroughEdge);
                }
            }
        }
    }

    /**
     * Assumes all invariants of all objects, eventually excluding the
     * {@code this} object.
     * 
     * @param excludeThisObject Whether to exclude the {@code this} object from
     *          the set of objects on which the invariants are assumed.
     */
    private void assumeAllInvariants(boolean excludeThisObject) {
        // TODO
        /*
    String t = quantVarName("t");
    String o = quantVarName("o");
    BPLVariable tVar = new BPLVariable(t, BPLBuiltInType.NAME);
    BPLVariable oVar = new BPLVariable(o, BPLBuiltInType.REF);
    if (excludeThisObject) {
      // Assume all invariants of all objects but the this object.
      addAssume(forall(tVar, oVar, implies(
          notEqual(var(o), var(thisVar())),
          inv(var(t), var(o), var(tc.getHeap())))));
    } else {
      // Assume all invariants of all objects.
      addAssume(forall(tVar, oVar, inv(var(t), var(o), var(tc.getHeap()))));
    }
         */
    }

    /** 
     * @deprecated
     */
    @Deprecated
    private BPLRequiresClause requireAllInvariants(boolean excludeThisObject) {
//        // TODO
//        String t = quantVarName("t");
//        String o = quantVarName("o");
//        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
//        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
//        if (excludeThisObject) {
//            // Assume all invariants of all objects but the this object.
//            return new BPLRequiresClause(forall(tVar, oVar, implies(notEqual(
//                    var(o),
//                    var(thisVar())), inv(var(t), var(o), var(tc.getHeap())))));
//        } else {
//            // Assume all invariants of all objects.
//            return new BPLRequiresClause(forall(tVar, oVar, inv(
//                    var(t),
//                    var(o),
//                    var(tc.getHeap()))));
//        }
        return new BPLRequiresClause(BPLBoolLiteral.TRUE);
    }

    /**
     * Generates proof obligations for verifying the invariants of all the objects
     * which are considered to be exposed, meaning that their invariants may have
     * been broken.
     * 
     * @param excludeThisObject Whether to exclude the {@code this} object from
     *          the set of objects on which the invariants are checked.
     */
    /*
  private void assertExposedInvariants(boolean excludeThisObject) {
    BPLExpression type = typeRef(method.getOwner());
    if (project.isThisInvariantsOnly()) {
      if (!method.isStatic() && !excludeThisObject) {
        addAssert(inv(type, var(thisVar()), var(tc.getHeap())));
      }
    } else {
      String o = quantVarName("o");
      BPLVariable oVar = new BPLVariable(o, BPLBuiltInType.REF);
      if (excludeThisObject) {
        addAssert(forall(oVar,
            implies(
              notEqual(var(o), var(thisVar())),
              inv(
                type,
                var(o),
                var(tc.getHeap())
              )
            )
        ));
      } else {
        addAssert(forall(oVar, inv(type, var(o), var(tc.getHeap()))));
      }
    }
  }
     */

    /**
     * @deprecated
     */
    @Deprecated
    private BPLEnsuresClause ensureExposedInvariants(boolean excludeThisObject) {
//        BPLExpression type = typeRef(method.getOwner());
//
//        if (project.isThisInvariantsOnly()) {
//            if (!method.isStatic() && !excludeThisObject) {
//                return new BPLEnsuresClause(inv(type, var(thisVar()), var(tc.getHeap())));
//            } else {
//                return new BPLEnsuresClause(BPLBoolLiteral.TRUE);
//            }
//        } else {
//            String o = quantVarName("o");
//            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
//            if (excludeThisObject) {
//                return new BPLEnsuresClause(forall(oVar, implies(notEqual(
//                        var(o),
//                        var(thisVar())), inv(type, var(o), var(tc.getHeap())
//                                ))));
//            } else {
//                return new BPLEnsuresClause(forall(oVar, inv(
//                        type,
//                        var(o),
//                        var(tc.getHeap())
//                        )));
//            }
//        }
        return new BPLEnsuresClause(BPLBoolLiteral.TRUE);
    }

    /**
     * Translates the local BML annotations (assertions, assumptions, ...)
     * attached to the given instruction.
     * 
     * @param insn The instruction handle at which to translate the local BML
     *          annotations.
     */
    /*
  private void translateInstructionAnnotations(InstructionHandle insn) {
    for (BMLAssertStatement assertion : insn.getAssertions()) {
      addAssert(translateLocalSpecification(assertion.getPredicate(), insn));
    }
    for (BMLAssumeStatement assumption : insn.getAssumptions()) {
      addAssume(translateLocalSpecification(assumption.getPredicate(), insn));
    }
  }
     */

    /**
     * Translates the start of a new basic block in the method's control flow
     * graph. Beside starting a new BoogiePL block, this method also handles the
     * translation of loop headers by inserting all the loop invariants resulting
     * from BML specifications (loop specifications) but also from the
     * verification methodology itself.
     * 
     * @param cfgBlock The basic block of the method's control flow graph to
     *          start.
     */
    private void translateCFGBlockStart(BasicBlock cfgBlock) {
        startBlock(blockLabel(cfgBlock));

        // Check whether the new basic block is a loop header.
        if (cfgBlock.isBackEdgeTarget()) {
            // update unroll count when entering a loop
            ///////////////////////////////////////////
            InstructionHandle insn = cfgBlock.getFirstInstruction();

            // Assume the type information contained in the loop headers stack frame
            // in order to preserve that information for potential loop targets.
            StackFrame frame = insn.getFrame();

            // Assume the type information of the local variables.
            // Assume the type information of the local variables.
//            for (int i = 0; i < frame.getLocalCount(); i++) {
//                JType type = frame.getLocal(i);
//                if (type != null) {
//                    if (type.isBaseType()) {
//                        //            addAssume(isOfType(var(localVar(i, type)), typeRef(type)));
//                    } else if (type != JNullType.NULL) {
//                        addAssume(isEqual(typ(stack(var(localVar(i, type))), var(tc.getHeap())), typeRef(type)));
//                    }
//                }
//            }
            // Assume the type information of the stack variables.
//            for (int i = 0; i < frame.getStackSize(); i++) {
//                JType type = frame.peek(i);
//                if (type != null) {
//                    if (type.isBaseType()) {
//                        //            addAssume(isOfType(var(stackVar(i, type)), typeRef(type)));
//                    } else if (type != JNullType.NULL) {
//                        addAssume(isEqual(typ(stack(var(stackVar(i, type))), var(tc.getHeap())), typeRef(type)));
//                    }
//                }
//            }

            // Assume that objects allocated at the loop entry remain allocated inside
            // the loop. Note that this assumption ignores the effect of any potential
            // garbage collector de-allocating objects inside the loop.
            String loopHeap = getLoopHeapVar(cfgBlock);
            String v = quantVarName("v");
            //      BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
            //      addAssume(forall(vVar, implies(alive(var(v), var(loopHeap)), alive(
            //          var(v),
            //          var(tc.getHeap())))));

            // If we are verifying the invariants on all objects of the method's
            // receiver type (and not only on the this object), we must enforce that
            // the invariants of objects of that type allocated inside the loop
            // satisfy their invariants at each loop iteration. This is a necessary
            // restriction since in a sound verification of loops we usually have
            // no information about the state of objects allocated inside the loop
            // when leaving the loop, meaning that if we do not verify their
            // invariants at this point, we could not verify them anymore. Note that
            // this statement is also true to some extent for objects which were
            // already allocated when entering the loop but their state can be
            // maintained through loop iterations by explicitly specifying loop
            // invariants in BML while objects allocated inside the loop cannot be
            // referred to in such explicit invariants. Therefore, we insert the
            // following implicit invariant of the verification methodology which
            // ensures that the invariants of objects allocated inside the loop hold
            // at every loop iteration.
            // ... end of comment ;) ...
            if (!project.isThisInvariantsOnly()) {
                String o = quantVarName("o");
                BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
                //        addAssert(forall(oVar,
                //            implies(
                //              logicalNot(alive(
                //                rval(var(o)),
                //                var(loopHeap)
                //              )), 
                //              inv(
                //                typeRef(method.getOwner()),
                //                var(o),
                //                var(tc.getHeap())
                //              )
                //            )
                //        ));
            }

            // Translate all the loop specifications.
            // TODO implement loop invariant handling
//            for (BMLLoopSpecification loopSpec : getLoopSpecificationsAt(cfgBlock)) {
//                // Assert the loop invariant itself.
//                addAssert(translateLoopInvariant(loopSpec.getInvariant(), insn));
//
//                BMLLoopVariant variant = loopSpec.getVariant();
//                String variantVar = getLoopVariantVar(variant);
//                // Assert the non-negativity of loop variant expressions.
//                addAssert(lessEqual(intLiteral(0), translateLoopVariant(variant, insn)));
//
//                // Assert the loop modifies clause.
//                addAssert(translateLoopFrame(loopSpec.getModifies(), insn));
//
//                // Remember the value of the loop variant expression at the beginning
//                // of the loop. This value will be used at the back edges of this loop
//                // in order to check that the loop variant expression indeed decreases
//                // a each iteration.
//                addAssignment(var(variantVar), translateLoopVariant(variant, insn));
//            }
        }
    }

    private List<BMLLoopSpecification> getLoopSpecificationsAt(BasicBlock cfgBlock) {
        List<BMLLoopSpecification> specs = new ArrayList<BMLLoopSpecification>();
        Iterator<Edge> inEdges = cfgBlock.inEdgeIterator();
        specs.addAll(cfgBlock.getFirstInstruction().getLoopSpecifications());
        while (inEdges.hasNext()) {
            Edge inEdge = inEdges.next();
            if (inEdge.isBackEdge()) {
                InstructionHandle lastInsn = inEdge.getSource().getLastInstruction();
                specs.addAll(lastInsn.getLoopSpecifications());
            }
        }
        return specs;
    }

    /**
     * Translates the {@code method}'s effective precondition.
     * 
     * @param method The method whose precondition should be translated.
     * @param parameters The names of the method's parameters.
     * @return A BoogiePL predicate expressing the method's effective
     *         precondition.
     */
//    private BPLExpression translatePrecondition(
//            BCMethod method,
//            String[] parameters) {
//        SpecificationTranslator translator = SpecificationTranslator.forPrecondition(tc.getHeap(), parameters);
//        return translator.translate(context, project.getSpecificationDesugarer().getPrecondition(method));
//    }

    /**
     * Translates the {@code method}'s effective modified variables.
     * 
     * @param method The method whose modifies variables should be translated.
     * @return A BoogiePL predicate expressing the method's effective
     *         modified variables.
     */
//    private BPLModifiesClause translateModifiesClause(
//            BCMethod method,
//            String[] parameters) {
//        SpecificationTranslator translator = SpecificationTranslator.forModifiesClause(tc.getHeap(), parameters);
//        return translator.translateModifiesStoreRefs(context, project.getSpecificationDesugarer().getModifiesStoreRefs(method));
//    }

    /**
     * Translates the {@code method}'s effective normal postcondition.
     * 
     * @param method The method whose normal postcondition should be translated.
     * @param oldHeap The name of the method's pre-state heap.
     * @param result The name of the method's return value.
     * @param parameters The names of the method's parameters.
     * @return A BoogiePL predicate expressing the method's effective normal
     *         postcondition.
     */
//    private BPLExpression translatePostcondition(
//            BCMethod method,
//            /*String oldHeap,*/
//            String result,
//            String[] parameters) {
//        SpecificationTranslator translator = SpecificationTranslator.forPostcondition(tc.getHeap(), /* oldHeap, */ result, parameters);
//        return translator.translate(context, project.getSpecificationDesugarer().getPostcondition(method));
//    }

    /**
     * Translates the {@code method}'s effective exceptional postcondition.
     * 
     * @param method The method whose exceptional postcondition should be
     *          translated.
     * @param exception The exception type thrown.
     * @param oldHeap The name of the method's pre-state heap.
     * @param exceptionObject The name of the exception object thrown.
     * @param parameters The names of the method's parameters.
     * @return A BoogiePL predicate expressing the method's effective exceptional
     *         postcondition.
     */
//    private BPLExpression translateXPostcondition(
//            BCMethod method,
//            JType exception,
//            /* String oldHeap, */
//            String exceptionObject,
//            String[] parameters) {
//        SpecificationTranslator translator = SpecificationTranslator.forPostcondition(tc.getHeap(), /* oldHeap, */ exceptionObject, parameters);
//        return translator.translate(context, project.getSpecificationDesugarer().getExceptionalPostcondition(method, exception));
//    }

//    /**
//     * Convenience method which returns the names of the local variables in the
//     * stack frame of the given bytecode instruction.
//     * 
//     * @param insn The instruction for which to return the names of the stack
//     *          frame's local variables.
//     * @return The names of the stack frame's local variables.
//     */
//    private static String[] getLocalVariablesAt(InstructionHandle insn) {
//        StackFrame frame = insn.getFrame();
//        String[] localVariables = new String[frame.getLocalCount()];
//        for (int i = 0; i < localVariables.length; i++) {
//            if (frame.getLocal(i) != null) {
//                localVariables[i] = localVar(i, frame.getLocal(i));
//            }
//        }
//        return localVariables;
//    }

    /**
     * Convenience method which returns the names of the stack variables in the
     * stack frame of the given bytecode instruction.
     * 
     * @param insn The instruction for which to return the names of the stack
     *          frame's stack variables.
     * @return The names of the stack frame's stack variables.
     */
    private static String[] getStackVariablesAt(InstructionHandle insn) {
        StackFrame frame = insn.getFrame();
        String[] stackVariables = new String[frame.getStackSize()];
        for (int i = 0; i < stackVariables.length; i++) {
            if (frame.peek(i) != null) {
                stackVariables[i] = stackVar(i, frame.peek(i));
            }
        }
        return stackVariables;
    }

    /**
     * Translates the local BML expression (assertion, loop specification, ...) at
     * the given instruction.
     * 
     * @param expression The local BML expression to translate.
     * @param insn The instruction handle to which the BML annotation belongs.
     * @return The BML annotation translated to BoogiePL.
     */
//    private BPLExpression translateLocalSpecification(
//            BMLExpression expression,
//            InstructionHandle insn) {
//        SpecificationTranslator translator = SpecificationTranslator
//                .forLocalSpecification(
//                        tc.getHeap(),
//                        PRE_HEAP_VAR,
//                        getLocalVariablesAt(insn),
//                        getStackVariablesAt(insn),
//                        getInParameters());
//        return translator.translate(context, expression);
//    }

//    private BPLExpression translateLoopInvariant(
//            BMLLoopInvariant invariant,
//            InstructionHandle loopHead) {
//        return translateLocalSpecification(invariant.getPredicate(), loopHead);
//    }

//    private BPLExpression translateLoopVariant(
//            BMLLoopVariant variant,
//            InstructionHandle loopHead) {
//        return translateLocalSpecification(variant.getExpression(), loopHead);
//    }

    public BPLExpression translateMethodFrame(
            BCMethod method,
            String[] parameters) {

        List<BPLExpression> expr = new ArrayList<BPLExpression>();
        List<BCMethod> overrides = method.getOverrides();
        for (BCMethod override : overrides) {


            // TODO REMOVE
            //System.out.println("Modified types for " + override.getName() + ":");
            BPLExpression[] refs = override.getModifiedObjectRefs();
            //if (refs.length == 0) System.out.println("  (none)");
            //      for (BPLExpression ref : refs) {
            //        System.out.println("  - " + ref);
            //      }
            /*
      System.out.println("Propagated types for " + override.getName());
      refs = override.getPropagatedFields();
      for (String ref : refs) {
        System.out.println("  - " + ref);
      } */

            BMLMethodSpecification spec = override.getSpecification();
            if (spec != null) {
                BMLSpecificationCase[] specCases = spec.getCases();
                for (int i = 0; i < specCases.length; i++) {
                    BMLStoreRef[] storeRefs = specCases[i].getModifies().getStoreRefs();
                    BMLExpression requires;
                    if (specCases.length == 1) {
                        requires = spec.getRequires().getPredicate();
                    } else {
                        requires = specCases[i].getRequires().getPredicate();
                    }
                    expr.add(translateMethodFrame(
                            requires,
                            storeRefs,
                            parameters));
                }
            }
        }
        if (expr.size() > 0) {
            return logicalAnd(expr.toArray(new BPLExpression[expr.size()]));
        } else {
            return BPLBoolLiteral.TRUE;
        }
    }

    private BPLExpression translateMethodFrame(
            BMLExpression requires,
            BMLStoreRef[] storeRefs,
            String[] parameters) {
        if (storeRefs.length > 0) {
            String l = quantVarName("l");
//            ModifiesFilter filter = ModifiesFilter.forMethod(old(var(tc.getHeap())).toString(), parameters, l);

            /* TODO: REMOVE (previous version)
      BPLExpression expr = implies(logicalAnd(
                                     alive(rval(obj(var(l))),
                                           old(var(tc.getHeap()))
                                     ),
                                     filter.translate(context, storeRefs)
                                   ),
                                   isEqual(
                                     get(var(tc.getHeap()), var(l)),
                                     get(old(var(tc.getHeap())), var(l))
                                   )
                           );
             */ // END (previous version)

            String o = quantVarName("o");

            //      BPLExpression expr = logicalAnd(
            //          implies(
            //              // alive(rval(var(o)), old(var(tc.getHeap()))),
            //              alive(rval(obj(var(l))), old(var(tc.getHeap()))),
            //              logicalAnd(
            //                // alive(rval(var(o)), var(tc.getHeap()))
            //                alive(rval(obj(var(l))), var(tc.getHeap())),
            //                isEqual(
            //                    get(var(tc.getHeap()), var(l)),
            //                    get(old(var(tc.getHeap())), var(l))
            //                )
            //              )
            //          )
            //          /* [SW]: removed due to issues with invariant checks:,
            //          implies(
            //              filter.translate(context, storeRefs),
            //              isEqual(
            //                  get(var(tc.getHeap()), var(l)),
            //                  get(old(var(tc.getHeap())), var(l))
            //              )
            //          )*/
            //      );
            //
            //      SpecificationTranslator translator = SpecificationTranslator.forPrecondition(var(tc.getHeap()).toString(), parameters);
            //      BPLExpression pre = translator.translate(context, requires);
            //      expr = implies(pre, expr);

            //      BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
            // BPLVariable oVar = new BPLVariable(o, BPLBuiltInType.REF);
            //      return forall(lVar, expr);
        }
        return BPLBoolLiteral.TRUE;
        // REVIEW[om]: Remove!
        // BMLStoreRef[] storeRefs =
        //   project.getSpecificationDesugarer().getModifiesStoreRefs(method);
        // if (storeRefs.length > 0) {
        //   String l = quantVarName("l");
        //   ModifiesFilter filter =
        //     ModifiesFilter.forMethod(oldHeap, parameters, l);
        //   BPLExpression expr = filter.translate(context, storeRefs);
        //   expr = logicalAnd(alive(rval(obj(var(l))), var(oldHeap)), expr);
        //   BPLExpression left = get(var(tc.getHeap()), var(l));
        //   BPLExpression right = get(var(oldHeap), var(l));
        //   expr = implies(expr, isEqual(left, right));
        //   BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //   expr = forall(lVar, expr);
        //   return expr;
        // }
        // return BPLBoolLiteral.TRUE;
    }

    private BPLExpression translateLoopFrame(
            BMLLoopModifiesClause modifies,
            InstructionHandle loopHead) {
        BMLStoreRef[] storeRefs = modifies.getStoreRefs();
        if (storeRefs.length > 0) {
            BasicBlock loopHeader = method.getCFG().findBlockStartingAt(loopHead);
            String loopHeap = getLoopHeapVar(loopHeader);
            String l = quantVarName("l");
//            ModifiesFilter filter = ModifiesFilter.forLoop(
//                    loopHeap,
//                    PRE_HEAP_VAR,
//                    thisVar(),
//                    getLocalVariablesAt(loopHead),
//                    getStackVariablesAt(loopHead),
//                    getInParameters(),
//                    l);
            //      BPLExpression expr = filter.translate(context, storeRefs);
            //      expr = logicalAnd(alive(rval(obj(var(l))), var(loopHeap)), expr);
            //      BPLExpression left = get(var(tc.getHeap()), var(l));
            //      BPLExpression right = get(var(loopHeap), var(l));
            //      expr = implies(expr, isEqual(left, right));
            //      BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
            //      expr = forall(lVar, expr);
            //      return expr;
        }
        return BPLBoolLiteral.TRUE;
    }

    /**
     * Convenience method for translating an integer constant.
     * 
     * @param literal The integer constant to translate.
     * @return A BoogiePL expression representing the given integer constant.
     */
    private BPLExpression intLiteral(long literal) {
        return context.translateIntLiteral(literal);
    }

    /**
     * Convenience method for translating a type reference.
     * 
     * @param type The type reference to translate.
     * @return A BoogiePL expression representing the given type reference.
     */
    private BPLExpression typeRef(JType type) {
        return context.translateTypeReference(type);
    }

    /**
     * Starts a new BoogiePL block with the given {@code label} in the current
     * translation. BoogiePL commands generated during the translation are then
     * added to this new block until it is closed.
     * 
     * @param label The label of the new BoogiePL block.
     */
    private void startBlock(String label) {
        blockLabel = label;
        blockComments = new ArrayList<String>();
        commands = new ArrayList<BPLCommand>();
    }

    /**
     * Returns whether a BoogiePL block is currently active meaning that it has
     * been opened but not closed yet.
     * 
     * @return Whether a BoogiePL block is currently active in the translation.
     */
    private boolean isInsideBlock() {
        return blockLabel != null;
    }

    /**
     * Adds the given {@code command} to the currently active BoogiePL block.
     * 
     * @param command The command to add to the currently active BoogiePL block.
     */
    private void addCommand(BPLCommand command) {
        commands.add(command);
    }

    /**
     * Adds the given {@code comment} to the given {@code command} and
     * adds the command to the currently active BoogiePL block.
     * 
     * @param command The command to add to the currently active BoogiePL block.
     * @param comment The comment to add to the command.
     */
    private void addCommentedCommand(BPLCommand command, String comment) {
        command.addComment(comment);
        commands.add(command);
    }

    /**
     * Adds an assignment for the given operands to the currently active BoogiePL
     * block.
     * 
     * @param lhs The LHS expression of the assignment.
     * @param rhs The RHS expression of the assignment.
     * @requires lhs != null && rhs != null;
     */
    private void addAssignment(BPLExpression lhs, BPLExpression rhs) {
        addCommand(new BPLAssignmentCommand(lhs, rhs));

        //try {

        //TODO manage modified variables according to heap and stack model
//        if (!(lhs instanceof BPLVariableExpression)) return;
//        String lhs_label = ((BPLVariableExpression)lhs).getIdentifier();
//
//        String rhs_label = null;
//        if (rhs instanceof BPLVariableExpression) {
//            BPLVariableExpression vrhs = ((BPLVariableExpression)rhs);
//            rhs_label = vrhs.getIdentifier();
//
//            addAlias(rhs_label, lhs_label);
//            for (String v : getAliasedValues(lhs_label)) {
//                addModifiedVariable(var(v));
//            }
//        } else if (rhs instanceof BPLFunctionApplication) {
//            BPLFunctionApplication farhs = ((BPLFunctionApplication)rhs);
//
//            if (lhs_label == tc.getHeap()) {
//
//                if (farhs.getFunctionName() == UPDATE_FUNC) {
//
//                    BPLFunctionApplication fieldLocFunction = ((BPLFunctionApplication)farhs.getArguments()[1]);
//                    BPLVariableExpression ref = ((BPLVariableExpression)fieldLocFunction.getArguments()[0]);
//
//                    String ref_name = ref.getVariable().getName();
//                    rhs_label = rhs.toString();
//
//                    addAlias(rhs_label, lhs_label);
//                    for (String v : getAliasedValues(ref_name)) {
//                        addModifiedVariable(var(rhs_label.replace(ref_name, v)));
//                    }
//                }
//            } else {
//                //        System.out.println("Class of Expression: " + rhs.getClass().getName());
//            }
//        }    
    }
    
    private void addAssignment(BPLExpression lhs, BPLExpression rhs, String comment) {
        addCommentedCommand(new BPLAssignmentCommand(lhs, rhs), comment);
    }

    /**
     * Adds an assertion for the given {@code expression} to the currently active
     * BoogiePL block.
     * 
     * @param expression The assertion's expression.
     * @requires expression != null;
     */
//    private void addAssert(BPLExpression expression) {
//        addCommand(new BPLAssertCommand(expression));
//    }
    
    /**
     * Adds an assertion for the given {@code expression} to the currently active
     * BoogiePL block.
     * 
     * @param expression The assertion's expression.
     * @requires expression != null;
     */
    private void addAssert(BPLExpression expression, String comment) {
    	addCommentedCommand(new BPLAssertCommand(expression), comment);
    }

    /**
     * Adds an assumption for the given {@code expression} to the currently active
     * BoogiePL block.
     * 
     * @param expression The assumption's expression.
     */
    private void addAssume(BPLExpression expression) {
        addCommand(new BPLAssumeCommand(expression));
    }
    
    /**
     * Adds an assumption for the given {@code expression} to the currently active
     * BoogiePL block.
     * 
     * @param expression The assumption's expression.
     */
    private void addAssume(BPLExpression expression, String comment) {
        addCommentedCommand(new BPLAssumeCommand(expression), comment);
    }

    /**
     * Adds a havoc statement for the given {@code variables} to the currently
     * active BoogiePL block.
     * 
     * @param variables The variables of the havoc statement.
     */
    private void addHavoc(BPLVariableExpression... variables) {
        addCommand(new BPLHavocCommand(variables));
    }

    /**
     * Ends the currently active BoogiePL block and terminates it by the given
     * {@code transferCommand}.
     * 
     * @param transferCommand The transfer command of the BoogiePL block to
     *          terminate.
     */
    private void endBlock(BPLTransferCommand transferCommand) {
        BPLTransferCommand transCmd;
        if(transferCommand instanceof BPLGotoCommand){
            String[] targetLabels = new String[transferCommand.getTargetLabels().length];
            for(int i=0; i<transferCommand.getTargetLabels().length; i++){
                targetLabels[i] = tc.prefix(getProcedureName(method) + "_" + transferCommand.getTargetLabels()[i]);
            }
            transCmd = new BPLGotoCommand(targetLabels);
        } else {
            transCmd = transferCommand;
        }
        BPLBasicBlock block = new BPLBasicBlock(
                tc.prefix(getProcedureName(method) + "_" + blockLabel),
                commands.toArray(new BPLCommand[commands.size()]),
                transCmd
                );
        for (String comment : blockComments) {
        	block.addComment(comment);
        }
        blocks.add(block);
        blockLabel = null;
    }
    
    private void rawEndBlock(String... labels) {
        BPLTransferCommand transCmd = new BPLGotoCommand(labels);
        BPLBasicBlock block = new BPLBasicBlock(
                tc.prefix(getProcedureName(method) + "_" + blockLabel),
                commands.toArray(new BPLCommand[commands.size()]),
                transCmd
                );
        for (String comment : blockComments) {
        	block.addComment(comment);
        }
        blocks.add(block);
        blockLabel = null;
    }

    /**
     * Ends the currently active BoogiePL block and terminates it by a transfer
     * command which branches to the blocks identified by the given, and possibly
     * empty, set of {@code labels}.
     * 
     * @param labels The labels identifying the BoogiePL blocks to which the block
     *          being closed should branch.
     */
    private void endBlock(String... labels) {
        if (labels.length == 0) {
            endBlock(new BPLReturnCommand());
        } else {
            endBlock(new BPLGotoCommand(labels));
        }
    }

    /**
     * Ends the currently active BoogiePL block by a transfer command which
     * branches to the BoogiePL block representing the target of the given
     * {@code cfgEdge} of the method's control flow graph. This method should be
     * used whenever the reason for terminating a BoogiePL block is an explicit
     * edge in the method's control flow graph. Beside the actual termination of
     * the current BoogiePL block, this method treats back edges and other
     * branches to loop headers specially by asserting the decreasing nature of
     * loop variant expressions (only along back edges) and by keeping a copy of
     * the current heap (only along non-back-edges to loop headers).
     * 
     * @param cfgEdge The edge in the method's control flow graph which triggers
     *          the termination of the current BoogiePL block.
     */
    private void endBlock(Edge cfgEdge) {
        BasicBlock cfgBlock = cfgEdge.getTarget();
        if (cfgBlock.isBackEdgeTarget()) {
            InstructionHandle insn = cfgBlock.getFirstInstruction();
            //TODO implement loop handling
//            for (BMLLoopSpecification loopSpec : getLoopSpecificationsAt(cfgBlock)) {
//                if (cfgEdge.isBackEdge()) {
////                    BMLLoopVariant variant = loopSpec.getVariant();
////                    // REVIEW[om]: This is a temporary hack to cope with what JACK gives
////                    // us.
////                    if (!(variant.getExpression() instanceof BMLIntLiteral)) {
////                        // Assert that the loop variant expression indeed decreased in the
////                        // current iteration. To that end, we use the old value of that
////                        // expression as previously evaluated and stored at the
////                        // corresponding loop header.
////                        String variantVar = getLoopVariantVar(variant);
////                        addAssert(less(translateLoopVariant(variant, insn), var(variantVar)));
////                    }
//                } else {
//                    // If we are branching to a loop header along a non-back-edge, we
//                    // keep a copy of the current heap which is used at the loop header
//                    // itself for translating loop invariants.
//                    String loopHeap = getLoopHeapVar(cfgEdge.getTarget());
//                    addAssignment(var(loopHeap), var(tc.getHeap()));
//                }
//            }
        }

        // Now, do the actual branch.
        endBlock(blockLabel(cfgEdge.getTarget()));
    }

    /**
     * Returns the name of the variable to use for storing a copy of the heap when
     * entering the loop starting at the given basic block of the method's control
     * flow graph.
     * 
     * @param cfgBlock The basic block where the loop starts.
     * @return The name of the loop heap variable.
     */
    private String getLoopHeapVar(BasicBlock cfgBlock) {
        String var = loopHeapVars.get(cfgBlock);
        if (var == null) {
            var = LOOP_HEAP_VAR_PREFIX + loopHeapVars.size();
            loopHeapVars.put(cfgBlock, var);
        }
        return var;
    }

    /**
     * Returns the name of the variable to use for storing a copy of the given
     * loop variant expression at the beginning of a loop.
     * 
     * @param variant The loop variant in question.
     * @return The name of the variable to store the value of the loop variant
     *         expression.
     */
    private String getLoopVariantVar(BMLLoopVariant variant) {
        String var = loopVariantVars.get(variant);
        if (var == null) {
            var = LOOP_VARIANT_VAR_PREFIX + loopVariantVars.size();
            loopVariantVars.put(variant, var);
        }
        return var;
    }

    /**
     * Returns the label to be used for a BoogiePL block representing the given
     * basic block of the method's control flow graph. The label is guaranteed to
     * be unique.
     * 
     * @param cfgBlock The basic block of the control flow graph.
     * @return The label for the BoogiePL block.
     * @requires cfgBlock != null;
     * @ensures \result != null;
     */
    private static String blockLabel(BasicBlock cfgBlock) {
        String label = BLOCK_LABEL_PREFIX + cfgBlock.getID();
        if (cfgBlock.isBackEdgeTarget()) {
            label += LOOP_BLOCK_LABEL_SUFFIX;
        }
        return label;
    }

    /**
     * Returns the label to be used for a BoogiePL block where the exceptional
     * postcondition of the given {@code exception} is checked at the end of a
     * method. The label is guaranteed to be unique.
     * 
     * @param exception The exception by which the method terminated.
     * @return The label to be used for the exceptional exit block.
     * @requires exception != null;
     * @ensures \result != null;
     */
    private static String postXBlockLabel(JType exception) {
        return POSTX_BLOCK_LABEL_PREFIX + exception.getName();
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for assuming that the guard of a conditional branch has evaluated
     * to {@code true}. The label is guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the conditional branch appears.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null;
     * @ensures \result != null;
     */
    private String trueBranchBlockLabel(BasicBlock cfgBlock) {
        return blockLabel(cfgBlock) + TRUE_BLOCK_LABEL_SUFFIX;
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for assuming that the guard of a conditional branch has evaluated
     * to {@code false}. The label is guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the conditional branch appears.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null;
     * @ensures \result != null;
     */
    private String falseBranchBlockLabel(BasicBlock cfgBlock) {
        return blockLabel(cfgBlock) + FALSE_BLOCK_LABEL_SUFFIX;
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for a concrete case statement of a switch statement and which
     * assumes that the constant of the switch statement has the given {@code key}
     * as its value. The label is guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the switch statement appears.
     * @param key The key of the concrete case statement.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null && key >= 0;
     * @ensures \result != null;
     */
    private String caseBlockLabel(BasicBlock cfgBlock, int key) {
        return blockLabel(cfgBlock) + CASE_BLOCK_LABEL_SUFFIX + key;
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for the default statement of a switch statement and which assumes
     * that the constant of the switch statement has a value different from all
     * the values handled by the individual case statements. The label is
     * guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the switch statement appears.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null;
     * @ensures \result != null;
     */
    private String defaultBlockLabel(BasicBlock cfgBlock) {
        return blockLabel(cfgBlock) + DEFAULT_BLOCK_LABEL_SUFFIX;
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for method calls having declared checked exceptions in case the
     * method terminates without throwing any exception. The label is guaranteed
     * to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the method call appears.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null;
     * @ensures \result != null;
     */
    private String normalPostBlockLabel(BasicBlock cfgBlock) {
        return blockLabel(cfgBlock) + NO_EXCEPTION_BLOCK_LABEL_SUFFIX;
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for method calls having declared checked exceptions in case the
     * method terminates by throwing the given {@code exception}. The label is
     * guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the method call appears.
     * @param exception The exception thrown by the method.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null && exception != null;
     * @ensures \result != null;
     */
    private String exceptionalPostBlockLabel(BasicBlock cfgBlock, JType exception) {
        return blockLabel(cfgBlock) + EXCEPTION_BLOCK_LABEL_SUFFIX + exception.getName();
    }

    /**
     * Returns the label to be used for the synthetic BoogiePL block which is
     * generated for branches to exception handlers when some exception is thrown.
     * The label is guaranteed to be unique.
     * 
     * @param cfgBlock The basic block of the method's control flow graph in which
     *          the branch to the exception handler appears.
     * @param exception The exception caught by the exception handler.
     * @return The label to be used for the BoogiePL block.
     * @requires cfgBlock != null && exception != null;
     * @ensures \result != null;
     */
    private String handlerBlockLabel(BasicBlock cfgBlock, JType exception) {
        return blockLabel(cfgBlock) + HANDLER_BLOCK_LABEL_SUFFIX + exception.getName();
    }

    /**
     * @requires type != null;
     * @ensures \result != null;
     */
    private static String typeAbbrev(BPLType type) {
        return (type == BPLBuiltInType.INT) ? INT_TYPE_ABBREV : REF_TYPE_ABBREV;
    }

    /*private static String paramVar(int index, JType type) {
        return localVar(index, type);
    }*/

    private String thisVar() {
        return localVar(0, new JClassType(typeLoader, "java.lang.Object")); //TODO 'this' is not really of type Object, but of some subtype
        // return THIS_VAR;
    }

    private static String localVar(int index, JType type) {
        return LOCAL_VAR_PREFIX + index + typeAbbrev(type(type));
    }

    private static String intLocalVar(int index) {
        return LOCAL_VAR_PREFIX + index + INT_TYPE_ABBREV;
    }

    private static String refLocalVar(int index) {
        return LOCAL_VAR_PREFIX + index + REF_TYPE_ABBREV;
    }

    private static String stackVar(int index, JType type) {
        return STACK_VAR_PREFIX + index + typeAbbrev(type(type));
    }
    
    private static String resVar(JType type) {
        return RESULT_PARAM + typeAbbrev(type(type));
    }

    private static String intStackVar(int index) {
        return STACK_VAR_PREFIX + index + INT_TYPE_ABBREV;
    }

    private static String refStackVar(int index) {
        return STACK_VAR_PREFIX + index + REF_TYPE_ABBREV;
    }

//    private static String returnStateVar(int index) {
//        return RETURN_STATE_VAR + index;
//    }

    private static String returnValueVar(int index, JType type) {
        return RETURN_VALUE_VAR + index + typeAbbrev(type(type));
    }

    private static String intReturnValueVar(int index) {
        return RETURN_VALUE_VAR + index + INT_TYPE_ABBREV;
    }

    private static String refReturnValueVar(int index) {
        return RETURN_VALUE_VAR + index + REF_TYPE_ABBREV;
    }

    private static String exceptionVar(int index) {
        return EXCEPTION_VAR + index;
    }

    private static String swapVar(JType type) {
        return SWAP_VAR_PREFIX + typeAbbrev(type(type));
    }

    // private List<String> localReferenceVariables = new ArrayList<String>();

    /**
     * Contains a mapping of local BoogiePL variable names to variables names
     * which are either globally defined or passed as method arguments.
     * The idea is that only the invariants of those variables need to be checked,
     * and all locally defined variables need not be taken into consideration.
     * This is only a helper structure used for aliasing purposes. The actually modified
     * objects are stored in {@link modifiedObjects}.
     * 
     */
    private static HashMap<String, ArrayList<String>> aliasMap = new HashMap<String, ArrayList<String>>();

    /**
     * Contains all modified objects (only method arguments or globally defined values).
     * The invariants of these objects need to be checked at the end of method bodies.
     */
    private static ArrayList<BPLVariableExpression> modifiedVariables = new ArrayList<BPLVariableExpression>();

    /**
     * Contains all modified heap locations for the current method.
     * The invariants of the objects located at the given locations on the heap
     * ned to be checked at the end of method bodies.
     */
    private static ArrayList<ModifiedHeapLocation> modifiedHeapLocations = new ArrayList<ModifiedHeapLocation>();

    /**
     * Adds a new alias to our data structure.
     * @param actualVar name of the actual variable (i.e. method argument or global variable)
     * @param alias name of the new (local) variable, e.g. register or stack variable
     * @requires actualRef != null && alias != null;
     */
    private static void addAlias(String actualRef, String alias) {
        ArrayList<String> existingAliases;

        // Find existing aliases
        if (aliasMap.containsKey(alias)) {
            // Load existing aliases
            existingAliases = aliasMap.get(alias);
        } else {
            // Create new list of aliases
            existingAliases = new ArrayList<String>();
        }

        // Only alias ref variables
        if (!isRefVariable(alias)) return;

        // Check whether {@see actualRef} is an alias too
        if (getAliasedValues(actualRef).isEmpty()) { // (alias)
            // Add new alias
//            if (isMethodArgument(actualRef) || isReturnValue(actualRef)) {
//                if (!existingAliases.contains(actualRef)) {
//                    existingAliases.add(actualRef);
//                }
//            }
        } else {
            // Iterate over all existing aliases
            for (String v : getAliasedValues(actualRef)) {
                if (!existingAliases.contains(v)) {
                    // Resolve aliases and add them to the list
                    existingAliases.add(v);
                }
            }
        }

        aliasMap.put(alias, existingAliases);
    }


    /**
     * Gets the name of the original variable (i.e. method argument or global variable)
     * from a given local variable name (e.g. register or stack variable).
     * @param alias name of the local register or stack variable
     * @return name of the actual variable (i.e. method argument or global variable)
     * @requires alias != null;
     */
    private static ArrayList<String> getAliasedValues(String alias) {

        // TODO: remove
        // dump current hash table
        //    System.out.println("CURRENT ALIASING HASHMAP:");
        //    System.out.println("-------------------------");
        //    for (String key : aliasMap.keySet()) {
        //      System.out.println(key + " =>");
        //      for (String value : aliasMap.get(key)) {
        //        System.out.println("\t" + value);
        //      }
        //    }

        if (aliasMap.containsKey(alias)) {
            return aliasMap.get(alias);
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Adds a given BPLVariable (i.e. the surrounding BPLVariableExpression)
     * to the internal list of modified variables.
     * @param v BPLVariableExpression containing the BPLVariable object from the modified variable.
     * @requires v != null;
     */
    private static void addModifiedVariable(BPLVariableExpression v) {
        for (BPLVariableExpression ve : modifiedVariables) {
            if (ve.getIdentifier() == v.getIdentifier()) return;
        }
        modifiedVariables.add(v);
    }

    /**
     * Adds a given BPLVariableExpression (i.e. the surrounding ModifiedHeapLocation)
     * to the internal list of modified variables.
     * @param mhl ModidifedHeapLocation containting the heap location of the modified object.
     * @requires mhl != null;
     */
    public static void addModifiedHeapLocation(ModifiedHeapLocation mhl) {
        if (!modifiedHeapLocations.contains(mhl)) {
            modifiedHeapLocations.add(mhl);
        }
    }

    /**
     * Returns {@code true} if the given variable name is locally defined.
     * @param string variable name
     * @return {@code true} if it is a locally defined variable, {@code false} if it is a globally defined variable or a method argument.
     * @deprecated
     */
    @Deprecated
    private static boolean isLocalVariable(BPLVariableExpression v) {
        Pattern pattern = Pattern.compile("^(" + LOCAL_VAR_PREFIX + "|" + STACK_VAR_PREFIX + "|" + RETURN_VALUE_VAR + ")(\\d)+" + REF_TYPE_ABBREV + "$");
        Matcher matcher = pattern.matcher(v.getIdentifier());
        return matcher.find();
    }

    /**
     * Returns {@code true} if the given variable name is a method argument.
     * @param v variable name
     * @return {@code true} if it is a method argument, {@code false} if it is any other variable.
     * @requires v != null;
     */
//    private static boolean isMethodArgument(String v) {
//        Pattern pattern = Pattern.compile("^" + LOCAL_VAR_PREFIX + "(\\d)+$");
//        Matcher matcher = pattern.matcher(v);
//        return matcher.find();
//    }

    /**
     * Returns {@code true} if the given variable name is a return value.
     * @param v variable name
     * @return {@code true} if it is a return value, {@code false} otherwise.
     */
    private static boolean isReturnValue(String v)
    {
        return v.startsWith(RESULT_PARAM); //TODO better solution here
    }

    /**
     * Returns (@code true} if the given variable name is of type ref.
     * @param v variable name
     * @return {@code true} if the given variable name is of type ref, {@code false} if it is of type int.
     */
    private static boolean isRefVariable(String v) {
        Pattern pattern = Pattern.compile("^(.*)" + REF_TYPE_ABBREV + "$");
        Matcher matcher = pattern.matcher(v);
        return matcher.find();
    }

    /**
     * The visitor performing the actual translation of the bytecode instructions.
     * 
     * @author Ovidio Mallo, Samuel Willimann
     */
    private final class InstructionTranslator implements IInstructionVisitor {

        /**
         * The basic block in the method's control flow graph to which the
         * instruction being translated belongs. Should be updated by the
         * {@code MethodTranslator} as appropriate.
         */
        private BasicBlock cfgBlock;

        /**
         * The instruction handle of the instruction being translated. Should be
         * updated by the {@code MethodTranslator} as appropriate.
         */
        private InstructionHandle handle;

        /**
         * Translates the occurrence of a runtime exception as thrown by the
         * bytecode instruction currently being translated.
         * 
         * @param exceptionName The name of the runtime exception eventually thrown.
         * @param normalConditions The conditions under which the runtime exception
         *          does <i>not</i> occur.
         */
        private void translateRuntimeException(
                String exceptionName,
                BPLExpression... normalConditions) {
            // If we are not modeling runtime exceptions, we simply rule them out
            // by asserting that the conditions under which the runtime exception
            // occurs do not hold.
            if (!project.isModelRuntimeExceptions()) {
                for (BPLExpression normalCondition : normalConditions) {
                    addAssert(normalCondition, "!Runtime exception");
                }
                return;
            }

            // Let's find the exception handler basic block which will catch the
            // runtime exception. Note that we will always have at most one exception
            // handler since for runtime exceptions we know the exact runtime type
            // of the exception being thrown meaning that we will never branch to
            // an exception handler whose handler type is a proper subtype of the
            // runtime exception (as is usually necessary for other exceptions thrown
            // e.g. by method calls or the ATHROW instruction).
            JType exception = typeLoader.getClassType(exceptionName);
            Set<String> labels = new LinkedHashSet<String>();
            for (ExceptionHandler handler : method.getExceptionHandlers()) {
                if (handler.isActiveFor(handle)) {
                    if (exception.isSubtypeOf(handler.getType())) {
                        InstructionHandle target = handler.getHandler();
                        labels.add(blockLabel(method.getCFG().findBlockStartingAt(target)));
                        break;
                    }
                }
            }
            // If we have not found any exception handler for the runtime exception,
            // we search for a matching checked exception of the method.
            if (labels.size() == 0) {
                for (JClassType methodException : method.getExceptionTypes()) {
                    if (exception.isSubtypeOf(methodException)) {
                        labels.add(postXBlockLabel(methodException));
                    }
                }
                // In any case, we have to at least branch to the exit block which
                // contains the proof obligations which must be satisfied even if the
                // method terminates with an exception.
                if (labels.size() == 0) {
                    labels.add(EXIT_BLOCK_LABEL);
                }
            }

            // Construct the names of the synthetic BoogiePL blocks which will assume
            // the conditions under which a runtime exception is thrown or not,
            // respectively.
            String trueBlock  = blockLabel(cfgBlock) + RUNTIME_EXCEPTION_TRUE_BLOCK_LABEL_SUFFIX  + exception.getName();
            String falseBlock = blockLabel(cfgBlock) + RUNTIME_EXCEPTION_FALSE_BLOCK_LABEL_SUFFIX + exception.getName();
            endBlock(trueBlock, falseBlock);

            // First, we generate the block which handles the thrown exception.
            startBlock(trueBlock);
            addAssume(logicalNot(logicalAnd(normalConditions)));
            // Havoc the exception object and assume its static type.
            // addHavoc(var(refStackVar(0)));
            // addAssume(alive(rval(var(refStackVar(0))), var(tc.getHeap())));
            // addAssume(nonNull(var(refStackVar(0))));
            //      addAssume(isEqual(typ(rval(var(refStackVar(0)))), typeRef(exception)));
            endBlock(labels.toArray(new String[labels.size()]));

            // Subsequently, we generate the block for the case where no exception is
            // thrown. Note that we do not end this BoogiePL block since the
            // translation of the instruction throwing the runtime exception can be
            // directly appended to it.
            startBlock(falseBlock);
            addAssume(logicalAnd(normalConditions));
        }

        private void translateLocalPlaces(List<Place> localPlaces) {
            final String CONT_POSTFIX = "_cont";
            final String SKIP_POSTFIX = "_skip";
            final String CHECK_POSTFIX = "_check";
            final String STALL_POSTFIX = "_stall";
            final String NOT_STALL_POSTFIX = "_notstall";
            String contLabel = blockLabel + CONT_POSTFIX;
            String skipLabel = blockLabel + SKIP_POSTFIX;
            List<String> contLabels = new ArrayList<String>();
            contLabels.add(skipLabel);
            for(Place place : localPlaces){
                contLabels.add(place.getName() + CHECK_POSTFIX);
            }
            endBlock(contLabels.toArray(new String[contLabels.size()]));
            
            
            for(Place localPlace : localPlaces){
            	String placeStallLabel = localPlace.getName() + STALL_POSTFIX;
            	String placeNotStallLabel = localPlace.getName() + NOT_STALL_POSTFIX;
                
            	// create the check-block for localPlace
                startBlock(localPlace.getName() + CHECK_POSTFIX);
                blockComments.add(Traces.makeComment("check at local place " + localPlace.getName()));
                
                Logger.getLogger(InstructionTranslator.class).debug("adding local place "+localPlace.getName());
                
                // assign place variable
                BoogiePlace bp = new BoogiePlace(localPlace.getName(), method, true);
                tc.addPlace(bp);
                addAssignment(stack(var(PLACE_VARIABLE)), var(localPlace.getName()), "local place");
                
                // assume place "when" condition
                addAssert(localPlace.getCondition().getWelldefinednessExpr(), "Check place condition " + localPlace.getCondition().getComment());
                addAssume(localPlace.getCondition().getExpr()); //TODO raw expression here
                
                // do assignments for place
                for (SpecAssignment s : localPlace.getAssignments()) {
                	addAssert(s.getWelldefinednessExpr(), "Do assignment " + s.getComment());
                	addCommand(s.getAssignCommand());
                }
                if (!localPlace.isNosync()) { 
                	rawEndBlock(tc.getCheckLabel());

                	startBlock(localPlace.getName());
                	blockComments.add(Traces.makeComment("continue from local place " + localPlace.getName()));
                	
                	addAssume(localPlace.getCondition().getExpr()); //TODO raw expression here
                	
                	SpecExpr oldStallCondition = localPlace.getOldStallCondition();
					addAssert(oldStallCondition.getWelldefinednessExpr(), 
							"Check old stall condition: " + oldStallCondition.getComment());
                	addAssume(isEqual(var(tc.getStallMap()), oldStallCondition.getExpr())); //TODO raw expression here
                	addAssume(isEqual(stack(var(PLACE_VARIABLE)), var(localPlace.getName())));
                	addAssume(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + getMethodName(method))));

                	if(handle != null){
                		// type informations of the stack
                		StackFrame stackFrame = handle.getFrame();
                		JType elemType;
                		for(int i=0; i<handle.getFrame().getStackSize(); i++){
                			elemType = stackFrame.peek(i);
                			if(elemType.isBaseType()){
                				addAssume(isInRange(stack(var(stackVar(i, elemType))), typeRef(elemType)));
                			} else {
                				addAssume(isOfType(stack(var(stackVar(i, elemType))), var(tc.getHeap()), typeRef(elemType)));
                			}
                		}

                		//type information of the local variables
                		for(int i=0; i<stackFrame.getLocalCount(); i++){
                			elemType = stackFrame.getLocal(i);
                			if (elemType != null) {
                				if(elemType.isBaseType()){
                					addAssume(isInRange(stack(var(localVar(i, elemType))), typeRef(elemType)));
                				} else {
                					addAssume(isOfType(stack(var(localVar(i, elemType))), var(tc.getHeap()), typeRef(elemType)));
                				}
                			}
                		}
                	}

                	typeMethodParams();

                	addAssume(nonNull(stack(receiver())));

                	//addAssignment(var(tc.getOldPlaceVar()), stack(var(PLACE_VARIABLE)));
                	SpecExpr oldMeasure = localPlace.getOldMeasure();
					if(!tc.isRound2() && oldMeasure != null){
                		addAssert(oldMeasure.getWelldefinednessExpr(), 
                				"Save old measure " + oldMeasure.getComment());
                		addAssignment(var(OLD_MEASURE), oldMeasure.getExpr());
                		//                    addAssert(lessEqual(intLiteral(0), var(tc.getOldMeasure())));
                	}
                	String placeLabel = tc.prefix(getProcedureName(method) + "_" + localPlace.getName());
                	tc.addLocalPlace(placeLabel);
                	endBlock(placeNotStallLabel, placeStallLabel);

                	startBlock(placeStallLabel);
                	blockComments.add(Traces.makeComment("stalling at local place " + localPlace.getName()));
                	
                	addAssume(var(tc.getStallMap()));
                	rawEndBlock(tc.getCheckLabel());
                	
                	startBlock(placeNotStallLabel);
                	addAssume(logicalNot(var(tc.getStallMap())));
                	endBlock(contLabel);
                } else {
                	endBlock(contLabel);
                }
            }
            
            startBlock(skipLabel);
            for(Place place : localPlaces){
                addAssume(logicalNot(place.getCondition().getExpr())); //TODO raw expression here
            }
            endBlock(contLabel);
            
            startBlock(contLabel);
        }

        //@ requires insn != null;
        public void visitNopInstruction(NopInstruction insn) {
            // do nothing
        }

        //@ requires insn != null;
        public void visitILoadInstruction(ILoadInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            int local = insn.getIndex();
            addAssignment(stack(var(intStackVar(stack))), stack(var(intLocalVar(local))), "loading "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitLLoadInstruction(LLoadInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            int local = insn.getIndex();
            addAssignment(stack(var(intStackVar(stack))), stack(var(intLocalVar(local))), "loading "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitALoadInstruction(ALoadInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            int local = insn.getIndex();
            addAssignment(stack(var(refStackVar(stack))), stack(var(refLocalVar(local))), "loading "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitIStoreInstruction(IStoreInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            int local = insn.getIndex();
            addAssignment(stack(var(intLocalVar(local))), stack(var(intStackVar(stack))), "storing "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitLStoreInstruction(LStoreInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            int local = insn.getIndex();
            addAssignment(stack(var(intLocalVar(local))), stack(var(intStackVar(stack))), "storing "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitAStoreInstruction(AStoreInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            int local = insn.getIndex();
            addAssignment(stack(var(refLocalVar(local))), stack(var(refStackVar(stack))), "storing "+localVarName(insn));
        }

        //@ requires insn != null;
        public void visitVConstantInstruction(VConstantInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            int constant = insn.getConstant();
            addAssignment(stack(var(intStackVar(stack))), intLiteral(constant));
        }

        //@ requires insn != null;
        public void visitLdcInstruction(LdcInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            Object constant = insn.getConstant();
            if (constant instanceof Integer) {
                Integer integer = (Integer) constant;
                addAssignment(stack(var(intStackVar(stack))), intLiteral(integer.intValue()));
            } else if (constant instanceof Long) {
                Long integer = (Long) constant;
                addAssignment(stack(var(intStackVar(stack))), intLiteral(integer.intValue()));
            } else if (constant instanceof String) {
                String string = (String) constant;
                BPLExpression stringExpr = context.translateStringLiteral(string);
                addAssignment(stack(var(refStackVar(stack))), stringExpr);
            } else if (constant instanceof JType) {
                JType type = (JType) constant;
                BPLExpression typeExpr = context.translateClassLiteral(type);
                addAssignment(stack(var(refStackVar(stack))), typeExpr);
            }
        }

        //@ requires insn != null;
        public void visitAConstNullInstruction(AConstNullInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            addAssignment(stack(var(refStackVar(stack))), nullLiteral());
        }

        //@ requires insn != null;
        public void visitGetFieldInstruction(GetFieldInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            BCField field = insn.getField();

            if(tc.getConfig().isNullChecks()){
                addAssert(nonNull(stack(var(refStackVar(stack)))), 
                		"Receiver must not be null when accessing field " + field.getName());
            } else {
                addAssume(nonNull(stack(var(refStackVar(stack)))));
            }

            BPLExpression ref = stack(var(refStackVar(stack)));
            BPLExpression get = fieldAccess(context, tc.getHeap(), ref, field);
            addAssignment(stack(var(stackVar(stack, field.getType()))), get);
        }

        //@ requires insn != null;
        public void visitPutFieldInstruction(PutFieldInstruction insn) {
            int stackLhs = handle.getFrame().getStackSize() - 2;
            int stackRhs = handle.getFrame().getStackSize() - 1;
            BCField field = insn.getField();

            if(tc.getConfig().isNullChecks()){
                addAssert(nonNull(stack(var(refStackVar(stackLhs)))),
                		"Receiver must not be null when accessing field " + field.getName());
            } else {
                addAssume(nonNull(stack(var(refStackVar(stackLhs)))));
            }

            BPLExpression lhs = var(refStackVar(stackLhs));
            BPLExpression rhs = var(stackVar(stackRhs, field.getType()));
            
            if(field.getType().isBaseType()){
                addAssume(isInRange(stack(rhs), typeRef(field.getType())));
            } else if(field.getType().isArrayType()){
                //TODO
            } else {
                //TODO assume the type is right (assuming the Java compiler did the check for us)
            }
            //      BPLExpression update = fieldUpdate(context, tc.getHeap(), lhs, field, rhs);

            BPLVariableExpression vlhs = (BPLVariableExpression)lhs;

            //TODO handle modified heap locations according to heap and stack model
//            if (getAliasedValues(vlhs.getIdentifier()).isEmpty()) {
//                addModifiedHeapLocation(new ModifiedHeapLocation(var(refStackVar(stackLhs)), fieldLoc(context, lhs, field)));
//            } else {
//                for (String v : getAliasedValues(vlhs.getIdentifier())) {
//                    // addModifiedVariable(v,);
//                    addModifiedHeapLocation(new ModifiedHeapLocation(var(v), fieldLoc(context, var(v), field)));
//                }
//            }

            BPLExpression heapLocation = new BPLArrayExpression(var(tc.getHeap()), stack(lhs), context.translateFieldReference(field));
            BPLCommand cmd = new BPLAssignmentCommand(heapLocation, stack(rhs));
            addCommand(cmd);
            if(tc.getConfig().isAssumeWellformedHeap()){
                addCommand(new BPLAssumeCommand(wellformedHeap(var(tc.getHeap()))));
            }
        }

        //@ requires insn != null;
        public void visitGetStaticInstruction(GetStaticInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            BCField field = insn.getField();

            BPLExpression get = fieldAccess(context, tc.getHeap(), null, field);
            addAssignment(stack(var(stackVar(stack, field.getType()))), get);
        }

        //@ requires insn != null;
        public void visitPutStaticInstruction(PutStaticInstruction insn) {
            int stackRhs = handle.getFrame().getStackSize() - 1;
            BCField field = insn.getField();

            BPLExpression rhs = var(stackVar(stackRhs, field.getType()));
            //      BPLExpression update = fieldUpdate(context, tc.getHeap(), null, field, rhs);
            BPLExpression heapLocation = new BPLArrayExpression(var(tc.getHeap()), BPLNullLiteral.NULL, var(GLOBAL_VAR_PREFIX+field.getQualifiedName())); //TODO check static fields are accessed correctly
            addAssignment(heapLocation, stack(rhs));
        }

        private void translateArrayLoadInstruction() {
            int stackRef = handle.getFrame().getStackSize() - 2;
            int stackIdx = handle.getFrame().getStackSize() - 1;
            JArrayType arrayType = (JArrayType) handle.getFrame().peek(stackRef);
            JType elemType = arrayType.getIndexedType();
            String ref = refStackVar(stackRef);
            String idx = intStackVar(stackIdx);

            if(!tc.isActive()){
                translateRuntimeException(
                        "java.lang.NullPointerException",
                        nonNull(stack(var(ref))));
            }
            //      translateRuntimeException(
            //          "java.lang.ArrayIndexOutOfBoundsException",
            //          lessEqual(intLiteral(0), var(idx)),
            //          less(var(idx), arrayLength(rval(var(ref)))));

            //TODO how to access an array
            //      BPLExpression get = arrayAccess(tc.getHeap(), arrayType, var(ref), var(idx));
            //      addAssignment(stack(var(stackVar(stackRef, elemType))), get);
        }

        //@ requires insn != null;
        public void visitVALoadInstruction(VALoadInstruction insn) {
            translateArrayLoadInstruction();
        }

        //@ requires insn != null;
        public void visitAALoadInstruction(AALoadInstruction insn) {
            translateArrayLoadInstruction();
        }

        private void translateArrayStoreInstruction() {
            int stackRef = handle.getFrame().getStackSize() - 3;
            int stackIdx = handle.getFrame().getStackSize() - 2;
            int stackVal = handle.getFrame().getStackSize() - 1;
            JArrayType arrayType = (JArrayType) handle.getFrame().peek(stackRef);
            JType elemType = arrayType.getIndexedType();
            String ref = refStackVar(stackRef);
            String idx = intStackVar(stackIdx);
            String val = stackVar(stackVal, elemType);

            if(!tc.isActive()){
                translateRuntimeException(
                        "java.lang.NullPointerException",
                        nonNull(stack(var(ref))));
            }
            //      translateRuntimeException(
            //          "java.lang.ArrayIndexOutOfBoundsException",
            //          lessEqual(intLiteral(0), var(idx)),
            //          less(var(idx), arrayLength(rval(var(ref)))));
            //      if (elemType.isReferenceType()) {
            //        translateRuntimeException("java.lang.ArrayStoreException", isOfType(
            //            rval(var(val)),
            //            elementType(typ(rval(var(ref))))));
            //      }

            //TODO how to update an array
            //      BPLExpression update = arrayUpdate(
            //              tc.getHeap(),
            //          arrayType,
            //          var(ref),
            //          var(idx),
            //          var(val));
            //      addAssignment(var(tc.getHeap()), update);
        }

        //@ requires insn != null;
        public void visitVAStoreInstruction(VAStoreInstruction insn) {
            translateArrayStoreInstruction();
        }

        //@ requires insn != null;
        public void visitAAStoreInstruction(AAStoreInstruction insn) {
            translateArrayStoreInstruction();
        }

        //@ requires insn != null;
        public void visitArrayLengthInstruction(ArrayLengthInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            String ref = refStackVar(stack);

            if(!tc.isActive()){
                translateRuntimeException(
                        "java.lang.NullPointerException",
                        nonNull(stack(var(ref))));
            }

            //      addAssignment(stack(var(intStackVar(stack))), arrayLength(rval(var(ref))));
            //TODO array length
            throw new RuntimeException("Not implemented.");
        }

        //@ requires invokedMethod != null && handle != null;
        private boolean isSuperConstructorCall(
                BCMethod invokedMethod,
                InstructionHandle handle) {
            if (invokedMethod.isConstructor()) {
                JType[] params = invokedMethod.getRealParameterTypes();
                int receiver = handle.getFrame().getStackSize() - params.length;
                JType receiverType = handle.getFrame().peek(receiver);
                return !receiverType.equals(invokedMethod.getOwner());
            }
            return false;
        }


        /**
         * Method to translate the different kinds of method call instructions.
         * 
         * @param insn The method call instruction to translate.
         * @requires insn != null;
         */
        private void translateInvokeInstruction(InvokeInstruction insn) {
            BCMethod invokedMethod = insn.getMethod();
            JType[] invokedMethodParams = invokedMethod.getRealParameterTypes();

            int first = handle.getFrame().getStackSize() - invokedMethodParams.length; // first method argument on the stack
            int stack = handle.getFrame().getStackSize();                 // stack size

            // Prepare arguments of method call
            List<BPLExpression> methodParams = new ArrayList<BPLExpression>();

            // does the invoked method provide a return value?
            //   - non-void method return an object reference or integer value
            //   - constructors "return" the reference to the instantiated owner object
            boolean hasReturnValue = !invokedMethod.isVoid() || invokedMethod.isConstructor();

            // is the invoked method a super-constructor called in the current constructor?
            //   - every constructors calls a super-constructor, the most general is Object..init()
            boolean isSuperConstructor = isSuperConstructorCall(invokedMethod, handle);
            
            BPLExpression spmapMinus1 = sub(spmap(), new BPLIntLiteral(1));
            BPLExpression ipMinus1 = sub(var(tc.getInteractionFramePointer()), new BPLIntLiteral(1));

            // get return type of method
            //   - normal method: explicitely declared return type
            //   - constructor: type of the owner object
            JType retType = (invokedMethod.isConstructor()
                    ? invokedMethod.getOwner()
                            : invokedMethod.getReturnType()
                    );
            
            
            
            //special handling of java.lang.Object.<init>
            if("<init>".equals(insn.getMethod().getName()) && "java.lang.Object".equals(insn.getMethodOwner().getName()) ){
                addAssignment(
                        stack(var(stackVar(first, retType))), 
                        stack(var(stackVar(first, invokedMethodParams[0]))), 
                        "constructor call: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner()
                        );
                return;
            }
            
            

            // Non-static method calls may throw a NullPointerException.
                    
            if (tc.getConfig().isNullChecks() && !invokedMethod.isStatic() && !invokedMethod.isConstructor()) {
                addAssert(nonNull(stack(var(refStackVar(first)))),
                		"Receiver must not be null when invoking " + invokedMethod.getName());
            } else {
                addAssume(nonNull(stack(var(refStackVar(first)))));
            }
            
            if(true){ //TODO remove if, if handling is working
                final String callPostfix = "_call";

                String invokedMethodName = getMethodName(invokedMethod);
                String thisPlace = tc.buildPlace(method, invokedMethodName);
                addAssignment(stack(var(PLACE_VARIABLE)), var(thisPlace), "methodcall: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner()+" in sourceline "+handle.getSourceLine());
                String nextLabel = tc.nextLabel();
                String boundaryLabel = nextLabel + BOUNDARY_LABEL_POSTFIX;
                String internLabel =  nextLabel + INTERN_LABEL_POSTFIX;
                
                BPLVariable iftmpVar = new BPLVariable(INTERACTION_FRAME_TEMP, new BPLTypeName(INTERACTION_FRAME_TYPE));
                tc.usedVariables().put(INTERACTION_FRAME_TEMP, iftmpVar);
                BPLVariable sftmpVar = new BPLVariable(STACK_FRAME_TEMP, new BPLTypeName(STACK_FRAME_TYPE));
                tc.usedVariables().put(STACK_FRAME_TEMP, sftmpVar);
                if(!invokedMethod.isConstructor() && !invokedMethod.isStatic()){
                    BPLTransferCommand transCmd = new BPLGotoCommand(boundaryLabel, internLabel);
                    transCmd.addComment("methodcall: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
                    endBlock(transCmd);
                    
                    ///////////////////////////////////////////////////////////////////////
                    // generate internal call block
                    ///////////////////////////////////////////////////////////////////////
                    
                    startBlock(internLabel);
                    blockComments.add(Traces.makeComment("internal call to " + invokedMethod.getQualifiedName()));
                    addAssignment(spmap(), add(spmap(), new BPLIntLiteral(1)), "create new stack frame");
                    
                    // Pass all other method arguments (the first of which refers to the "this" object
                    // if the method is not static).
                    String[] args = new String[invokedMethodParams.length];
                    for (int i = 0; i < invokedMethodParams.length; i++) {
                        args[i] = stackVar(first + i, invokedMethodParams[i]);
                        methodParams.add(new BPLVariableExpression(args[i]));
                        if(invokedMethodParams[i].isBaseType()){
                            addAssert(isInRange(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be in range for " + invokedMethodParams[i].getName());
                        } else if(invokedMethodParams[i].isClassType()){
                            addAssert(isOfType(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), var(tc.getHeap()), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be of type " + invokedMethodParams[i].getName());
                        } else {
                            //TODO array type
                        }
                        addAssignment(stack(var(localVar(i, invokedMethodParams[i]))), stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])));
                    }
                    
                    addAssignment(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName));
                    
                    String t = "t";
                    BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
                    addAssume(exists(tVar, 
                            logicalAnd(
                                    memberOf(var(tc.getImpl()), var(GLOBAL_VAR_PREFIX+invokedMethodName), var(t), typ(stack(receiver()), var(tc.getHeap()))),
                                    libType(var(tc.getImpl()), var(t))
                                    )
                            ));
                    rawEndBlock(tc.prefix(CALLTABLE_LABEL));
                    
                    
                    ///////////////////////////////////////////////////////////////////
                    // generate boundary call block
                    ////////////////////////////////////////////////////////////////////
                    
                    startBlock(boundaryLabel);
                    blockComments.add(Traces.makeComment("boundary call to " + invokedMethod.getQualifiedName()));
                    
                    addCommentedCommand(new BPLHavocCommand(var(INTERACTION_FRAME_TEMP)), "this empties the frame we will use for the boundary call");
                    addAssume(emptyInteractionFrame(var(INTERACTION_FRAME_TEMP)));
                    addAssignment(map(var(tc.getStack()), add(var(tc.getInteractionFramePointer()), new BPLIntLiteral(1))), var(INTERACTION_FRAME_TEMP));
//                    addAssert(wellformedStack(var(tc.getStack()), var(tc.getInteractionFramePointer()), var(tc.getStackPointerMap()), var(tc.getHeap())));
                    
                    addAssignment(var(tc.getInteractionFramePointer()), add(var(tc.getInteractionFramePointer()), new BPLIntLiteral(1)), "create new interaction frame");
                    addCommand(Library.assumeInIntRange(var(tc.getInteractionFramePointer())));
                    addAssignment(spmap(), new BPLIntLiteral(0), "create the initial stack frame of the new interaction frame");
                    
                    // Pass all other method arguments (the first of which refers to the "this" object
                    // if the method is not static).
                    args = new String[invokedMethodParams.length];
                    for (int i = 0; i < invokedMethodParams.length; i++) {
                        args[i] = stackVar(first + i, invokedMethodParams[i]);
                        methodParams.add(new BPLVariableExpression(args[i]));
                        if(invokedMethodParams[i].isBaseType()){
                            addAssert(isInRange(stack(ipMinus1, var(args[i])), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be in range for " + invokedMethodParams[i].getName());
                        } else if(invokedMethodParams[i].isClassType()){
                            addAssert(isOfType(stack(ipMinus1, var(args[i])), var(tc.getHeap()), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be of type " + invokedMethodParams[i].getName());
                        } else {
                            //TODO array type
                        }
                        addAssignment(stack(var(localVar(i, invokedMethodParams[i]))), stack(ipMinus1, var(args[i])));
                    }
                    
                    addAssignment(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName));
                    
                    addAssume(exists(tVar, 
                            logicalAnd(
                                    memberOf(var(tc.getImpl()), var(GLOBAL_VAR_PREFIX+invokedMethodName), var(t), typ(stack(receiver()), var(tc.getHeap()))),
                                    ctxtType(var(t))
                                    )
                            ));
                    addAssume(isCallable(var(tc.getImpl()), typeRef(method.getOwner()), var(GLOBAL_VAR_PREFIX+invokedMethodName)), "rule out private methods");
                    addAssume(heap(stack(receiver()), var(CREATED_BY_CTXT_FIELD)));
                    rawEndBlock(tc.getCheckLabel());
                } else if(invokedMethod.isConstructor()) {
                    //the invoked method is a constructor of an internal type, but not a superconstructor call
                    
                    
                    
                    addAssignment(spmap(), add(spmap(), new BPLIntLiteral(1)), "create new stack frame");
                    
                    // Pass all other method arguments (the first of which refers to the "this" object
                    // if the method is not static).
                    String[] args = new String[invokedMethodParams.length];
                    for (int i = 0; i < invokedMethodParams.length; i++) {
                        args[i] = stackVar(first + i, invokedMethodParams[i]);
                        methodParams.add(new BPLVariableExpression(args[i]));
                        if(invokedMethodParams[i].isBaseType()){
                            addAssert(isInRange(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be in range of " + invokedMethodParams[i].getName());
                        } else if(invokedMethodParams[i].isClassType()){
                            addAssert(isOfType(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), var(tc.getHeap()), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be of type " + invokedMethodParams[i].getName());
                        } else {
                            //TODO array type
                        }
                        addAssignment(stack(var(localVar(i, invokedMethodParams[i]))), stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])));
                    }
                    
                    addAssignment(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName));
                    
                    
                    
                    BPLTransferCommand transCmd = new BPLGotoCommand(tc.prefix(getProcedureName(invokedMethod))); //this is a static call
                    transCmd.addComment("constructor call: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
                    
                    if(!isSuperConstructor){
                        // we created the object, so it is not createdByCtxt and not exposed
                    	//we have constructed the object on our own
                        addAssert(logicalNot(heap(stack(var(LOCAL_VAR_PREFIX+0+typeAbbrev(type(retType)))), var(CREATED_BY_CTXT_FIELD))),
                        		"Newly created object is created by library");
                      //the object did not yet cross the boundary
                        addAssert(logicalNot(heap(stack(var(LOCAL_VAR_PREFIX+0+typeAbbrev(type(retType)))), var(EXPOSED_FIELD))),
                        		"Newly created object is not exposed to context"); 
                    }
                    
                    BPLBasicBlock block = new BPLBasicBlock(
                            tc.prefix(getProcedureName(method) + "_" + blockLabel),
                            commands.toArray(new BPLCommand[commands.size()]),
                            transCmd
                            );
                    blocks.add(block);
                } else { //method is static
                    addAssignment(spmap(), add(spmap(), new BPLIntLiteral(1)), "create new stack frame");
                    // if the method is static, pass the class object as param0
                    //addAssignment(stack(var(paramVar(0, invokedMethod.getOwner()))), classRepr(typeRef(invokedMethod.getOwner())));
                    
                    // Pass all other method arguments (the first of which refers to the "this" object
                    // if the method is not static).
                    String[] args = new String[invokedMethodParams.length];
                    for (int i = 0; i < invokedMethodParams.length; i++) {
                        args[i] = stackVar(first + i, invokedMethodParams[i]);
                        methodParams.add(new BPLVariableExpression(args[i]));
                        if(invokedMethodParams[i].isBaseType()){
                            addAssert(isInRange(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), typeRef(invokedMethodParams[i])),
                            		"Argument " + i + " must be in range of " + invokedMethodParams[i].getName());
                        } else if(invokedMethodParams[i].isClassType()){
                            addAssert(isOfType(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])), var(tc.getHeap()), typeRef(invokedMethodParams[i])),
                    				"Argument " + i + " must be of type " + invokedMethodParams[i].getName());
                        } else {
                            //TODO array type
                        }
                        addAssignment(stack(var(localVar(i, invokedMethodParams[i]))), stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(args[i])));
                    }
                    
                    addAssignment(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName));
                    
                    
                    BPLTransferCommand transCmd = new BPLGotoCommand(tc.prefix(getProcedureName(invokedMethod)));
                    transCmd.addComment("static methodcall: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
                    BPLBasicBlock block = new BPLBasicBlock(
                            tc.prefix(getProcedureName(method) + "_" + blockLabel),
                            commands.toArray(new BPLCommand[commands.size()]),
                            transCmd
                            );
                    blocks.add(block);
                }
                
                
                
                
//                BPLExpression sp = var(tc.getStackPointer());
//                BPLExpression spMinus1 = sub(sp, intLiteral(1)); 
//                addAssignment(sp, add(sp, intLiteral(1)), "create new stack frame");
//                
//                // if the method is static, pass the class object as param0
//                if(invokedMethod.isStatic()){
//                    addAssignment(stack(var(paramVar(0, invokedMethod.getOwner()))), CodeGenerator.classRepr(typeRef(invokedMethod.getOwner())));
//                }
//                // Pass all other method arguments (the first of which refers to the "this" object
//                // if the method is not static).
//                String[] args = new String[invokedMethodParams.length];
//                for (int i = 0; i < invokedMethodParams.length; i++) {
//                    args[i] = stackVar(first + i, invokedMethodParams[i]);
//                    methodParams.add(new BPLVariableExpression(args[i]));
//                    if(invokedMethodParams[i].isBaseType()){
//                        addAssert(isInRange(stack(spMinus1, var(args[i])), typeRef(invokedMethodParams[i])));
//                    } else if(invokedMethodParams[i].isClassType()){
//                        addAssert(isOfType(stack(spMinus1, var(args[i])), var(tc.getHeap()), typeRef(invokedMethodParams[i])));
//                    } else {
//                        //TODO array type
//                    }
//                    if(invokedMethod.isStatic()){
//                        addAssignment(stack(var(paramVar(i+1, invokedMethodParams[i]))), stack(spMinus1, var(args[i])));
//                    } else {
//                        addAssignment(stack(var(paramVar(i, invokedMethodParams[i]))), stack(spMinus1, var(args[i])));
//                    }
//                }
//
//                /*
//      boolean isSuperConstructor = method.isConstructor() && invokedMethod.isConstructor() &&
//                                   (args.length > 0) ? (args[0].equals(stackVar(0, params[0]))) : false;
//                 */
//                 
//
//                //new code
//                addAssignment(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName));
//
//                if(!invokedMethod.isConstructor() && !invokedMethod.isStatic()){
//                    //                rawEndBlock(tc.getCheckLabel());
//                    String boundaryLabel = blockLabel + BOUNDARY_LABEL_POSTFIX;
//                    String internLabel =  blockLabel + INTERN_LABEL_POSTFIX;
//
//                    BPLTransferCommand transCmd = new BPLGotoCommand(
//                            tc.prefix(getProcedureName(method) + "_" + boundaryLabel),
//                            tc.prefix(getProcedureName(method) + "_" + internLabel));
//                    transCmd.addComment("methodcall: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
//                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
//                    BPLBasicBlock block = new BPLBasicBlock(
//                            tc.prefix(getProcedureName(method) + "_" + blockLabel),
//                            commands.toArray(new BPLCommand[commands.size()]),
//                            transCmd
//                            );
//                    blocks.add(block);
//
//
//                    startBlock(internLabel);
//                    String t = "t";
//                    BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
//                    addAssume(exists(tVar, 
//                            logicalAnd(
//                                    memberOf(var(GLOBAL_VAR_PREFIX+invokedMethodName), var(t), typ(stack(receiver()), var(tc.getHeap()))),
//                                    libType(var(t))
//                                    )
//                            ));
//                    //                block = new BPLBasicBlock(
//                    //                        tc.prefix(getProcedureName(method) + "_" + blockLabel),
//                    //                        commands.toArray(new BPLCommand[commands.size()]),
//                    //                        transCmd
//                    //                        );
//                    //                blocks.add(block);
//                    rawEndBlock(tc.prefix(CALLTABLE_LABEL));
//
//
//                    startBlock(boundaryLabel);
//                    addAssume(exists(tVar, 
//                            logicalAnd(
//                                    memberOf(var(GLOBAL_VAR_PREFIX+invokedMethodName), var(t), typ(stack(receiver()), var(tc.getHeap()))),
//                                    logicalNot(libType(var(t)))
//                                    )
//                            ));
//                    addAssume(isCallable(typeRef(method.getOwner()), var(GLOBAL_VAR_PREFIX+invokedMethodName)), "rule out private methods");
//                    addAssume(heap(stack(receiver()), var(CREATED_BY_CTXT_FIELD)));
//                    //TODO more detailed information about the type here
//                    rawEndBlock(tc.getCheckLabel());
//                } else if(invokedMethod.isConstructor()) {
//                    //the invoked method is a constructor of an internal type, but not a superconstructor call
//                    if(!isSuperConstructor){
//                        first = first - 1; //the stack index is one off if we have a constructor
//                    }
//                    
//                    BPLTransferCommand transCmd = new BPLGotoCommand(tc.prefix(getProcedureName(invokedMethod))); //this is a static call
//                    transCmd.addComment("constructor call: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
//                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
//                    
//                    if(!isSuperConstructor){
//                        // we created the object, so it is not createdByCtxt and not exposed
//                        addAssume(logicalNot(heap(stack(var(PARAM_VAR_PREFIX+0+typeAbbrev(type(retType)))), var(CREATED_BY_CTXT_FIELD)))); //we hace constructed the object on our own
//                        addAssume(logicalNot(heap(stack(var(PARAM_VAR_PREFIX+0+typeAbbrev(type(retType)))), var(EXPOSED_FIELD)))); //the object did not yet cross the boundary
//                    }
//                    
//                    BPLBasicBlock block = new BPLBasicBlock(
//                            tc.prefix(getProcedureName(method) + "_" + blockLabel),
//                            commands.toArray(new BPLCommand[commands.size()]),
//                            transCmd
//                            );
//                    blocks.add(block);
//                } else { //method is static
//                    BPLTransferCommand transCmd = new BPLGotoCommand(tc.prefix(getProcedureName(invokedMethod)));
//                    transCmd.addComment("static methodcall: "+insn.getMethod().getName()+" of type "+insn.getMethodOwner());
//                    transCmd.addComment("Sourceline: "+handle.getSourceLine());
//                    BPLBasicBlock block = new BPLBasicBlock(
//                            tc.prefix(getProcedureName(method) + "_" + blockLabel),
//                            commands.toArray(new BPLCommand[commands.size()]),
//                            transCmd
//                            );
//                    blocks.add(block);
//                }
                
                if (invokedMethod.isConstructor() && !isSuperConstructor) {
                	first = first - 1;
          	  	}
                
                
                
                String internalReturnLabel = tc.nextLabel() + "_internal_return";
                String boundaryReturnLabel = tc.nextLabel() + "_boundary_return";
                String contLabel = tc.nextLabel() + "_cont";
                
                startBlock(tc.nextLabel());

              if(!invokedMethod.isConstructor()){
                  if(!invokedMethod.isStatic()){
                      addAssume(isOfType(stack(receiver()), var(tc.getHeap()), typeRef(insn.getMethodOwner())));
                  } else {
                      //addAssume(isEqual(stack(receiver()), classRepr(typeRef(invokedMethod.getOwner()))), "Class representative for static call");
                  }
              } else {
                  addAssume(nonNull(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))))); //we have constructed the object
                  if(!isSuperConstructor){
                      addAssume(logicalNot(heap(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(CREATED_BY_CTXT_FIELD)))); //we have constructed the object on our own
                      addAssume(logicalNot(heap(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(EXPOSED_FIELD)))); //the object did not yet cross the boundary
                  }
              }
              addAssume(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName)));
              
              if(hasReturnValue){
                  if(retType.isBaseType()){
                      addAssume(isInRange(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), typeRef(retType)));
                  } else if(retType.isClassType()){
                      addAssume(isOfType(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(tc.getHeap()), typeRef(retType)));
                  } else {
                      //TODO array
                  }
              }
              endBlock(new BPLGotoCommand(boundaryReturnLabel, internalReturnLabel));
              
              // internal return: read result from other stack frame and remove it
              ////////////////////////////////////////////////////////////////////
              startBlock(internalReturnLabel);
              blockComments.add(Traces.makeComment("internal return to " + method.getQualifiedName()));
              
              addAssume(isEqual(modulo_int(var(tc.getInteractionFramePointer()), new BPLIntLiteral(2)), new BPLIntLiteral(1)));
              if(hasReturnValue){
            	  addAssignment(stack(var(tc.getInteractionFramePointer()), spmapMinus1, var(stackVar(first, retType))), stack(var(RESULT_PARAM+typeAbbrev(type(retType)))));
              }
              addAssignment(spmap(), sub(spmap(), new BPLIntLiteral(1)));

              addCommentedCommand(new BPLHavocCommand(var(STACK_FRAME_TEMP)), "this empties the frame we used for the internal call");
              addAssignment(map1(var(tc.getStack()), var(tc.getInteractionFramePointer()), add(spmap(), new BPLIntLiteral(1))), var(STACK_FRAME_TEMP));
              addAssume(wellformedStack(var(tc.getStack()), var(tc.getInteractionFramePointer()), var(tc.getStackPointerMap()), var(tc.getHeap())));
              
              endBlock(new BPLGotoCommand(contLabel));
              
              
              // boundary return: read result from other interaction frame and remove it
              ///////////////////////////////////////////////////////////////////////////
              startBlock(boundaryReturnLabel);
              blockComments.add(Traces.makeComment("boundary return to " + method.getQualifiedName()));
              
              addAssume(isEqual(modulo_int(var(tc.getInteractionFramePointer()), new BPLIntLiteral(2)), new BPLIntLiteral(0)));
              if(hasReturnValue){
                  addAssignment(stack(ipMinus1, var(stackVar(first, retType))), stack(var(RESULT_PARAM+typeAbbrev(type(retType)))));
              }
              addAssignment(var(tc.getInteractionFramePointer()), sub(var(tc.getInteractionFramePointer()), new BPLIntLiteral(1)));
              
              addCommentedCommand(new BPLHavocCommand(var(INTERACTION_FRAME_TEMP)), "this empties the frame we used for the boundary call");
              addAssignment(map(var(tc.getStack()), add(var(tc.getInteractionFramePointer()), new BPLIntLiteral(1))), var(INTERACTION_FRAME_TEMP));
              addAssume(wellformedStack(var(tc.getStack()), var(tc.getInteractionFramePointer()), var(tc.getStackPointerMap()), var(tc.getHeap())));
              
              endBlock(new BPLGotoCommand(contLabel));
              
              startBlock(contLabel);
              
              addAssume(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + getMethodName(method))));
              addAssume(isEqual(stack(var(PLACE_VARIABLE)), var(thisPlace)));
              
              // type informations of the stack
              StackFrame stackFrame = handle.getNext().getFrame();
              JType elemType;
              for(int i=0; i<stackFrame.getStackSize(); i++){
                  elemType = stackFrame.peek(i);
                  if(elemType.isBaseType()){
                      addAssume(isInRange(stack(var(stackVar(i, elemType))), typeRef(elemType)));
                  } else {
                      addAssume(isOfType(stack(var(stackVar(i, elemType))), var(tc.getHeap()), typeRef(elemType)));
                  }
              }
              
              //type information of the local variables
              for(int i=0; i<stackFrame.getLocalCount(); i++){
                  elemType = stackFrame.getLocal(i);
                  if (elemType != null) {
                	  if(elemType.isBaseType()){
                		  addAssume(isInRange(stack(var(localVar(i, elemType))), typeRef(elemType)));
                	  } else {
                		  addAssume(isOfType(stack(var(localVar(i, elemType))), var(tc.getHeap()), typeRef(elemType)));
                	  }
                  }
              }
              
              // type information of the method parameters
              typeMethodParams();
              
              addAssume(nonNull(stack(receiver())));
              
              
                
                
                
                
                
                
//                startBlock(tc.nextLabel());
////                addAssignment(stack(receiver()), var("reg0_r"));//TODO correct expression here
//
//                if(!invokedMethod.isConstructor()){
//                    addAssume(isOfType(stack(receiver()), var(tc.getHeap()), typeRef(insn.getMethodOwner())));
//                } else {
//                    addAssume(nonNull(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))))); //we hace constructed the object
//                    if(!isSuperConstructor){
//                        addAssume(logicalNot(heap(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(CREATED_BY_CTXT_FIELD)))); //we hace constructed the object on our own
//                        addAssume(logicalNot(heap(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(EXPOSED_FIELD)))); //the object did not yet cross the boundary
//                    }
//                }
//                addAssume(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + invokedMethodName)));
//                
//                if(hasReturnValue){
//                    if(retType.isBaseType()){
//                        addAssume(isInRange(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), typeRef(retType)));
//                    } else if(retType.isClassType()){
//                        addAssume(isOfType(stack(var(RESULT_PARAM+typeAbbrev(type(retType)))), var(tc.getHeap()), typeRef(retType)));
//                    } else {
//                        //TODO array
//                    }
//                    addAssignment(stack(spMinus1, var(stackVar(first, retType))), stack(var(RESULT_PARAM+typeAbbrev(type(retType)))));
//                }
//                addAssignment(sp, sub(sp, intLiteral(1)));
//                
//                // type informations of the stack
//                StackFrame stackFrame = handle.getFrame();
//                JType elemType;
//                for(int i=0; i<first; i++){
//                    elemType = stackFrame.getLocal(i);
//                    if(elemType.isBaseType()){
//                        addAssume(isInRange(stack(var(stackVar(i, elemType))), typeRef(elemType)));
//                    } else {
//                        addAssume(isOfType(stack(var(stackVar(i, elemType))), var(tc.getHeap()), typeRef(elemType)));
//                    }
//                }
//                
//                // type information of the method parameters
//                JType[] params = new JType[method.getParameterCount() + 1];
//                params[0] = method.getOwner();
//                System.arraycopy(method.getParameterTypes(), 0, params, 1, method.getParameterCount());
//                for(int i=0; i<params.length; i++){
//                    if(params[i].isBaseType()){
//                        addAssume(isInRange(stack(var(paramVar(i, params[i]))), typeRef(params[i])));
//                    } else {
//                        addAssume(isOfType(stack(var(paramVar(i, params[i]))), var(tc.getHeap()), typeRef(params[i])));
//                    }
//                    if(method.isStatic()){
//                        if(i>0){
//                            addAssignment(stack(var(localVar(i-1, params[i]))), stack(var(paramVar(i, params[i]))));
//                        }
//                    } else {
//                        addAssignment(stack(var(localVar(i, params[i]))), stack(var(paramVar(i, params[i]))));
//                    }
//                }
//                
//                addAssume(nonNull(stack(receiver())));
//                
//                addAssume(isEqual(stack(var(METH_FIELD)), var(GLOBAL_VAR_PREFIX + getMethodName(method))));
//                addAssume(isEqual(stack(var(PLACE_VARIABLE)), var(thisPlace)));
                
                callStatements++;
            }
        }

		private void typeMethodParams() {
			JType[] params = method.getParameterTypes();
			if (!method.isStatic()) {
				params = new JType[method.getParameterCount() + 1];
				params[0] = method.getOwner();
				System.arraycopy(method.getParameterTypes(), 0, params, 1, method.getParameterCount());
			}

			for(int i=0; i<params.length; i++){
				if(params[i].isBaseType()){
					addAssume(isInRange(stack(var(localVar(i, params[i]))), typeRef(params[i])));
				} else {
					addAssume(isOfType(stack(var(localVar(i, params[i]))), var(tc.getHeap()), typeRef(params[i])));
				}
			}
		}

        //@ requires insn != null;
        public void visitInvokeVirtualInstruction(InvokeVirtualInstruction insn) {
            translateInvokeInstruction(insn);
        }

        //@ requires insn != null;
        public void visitInvokeStaticInstruction(InvokeStaticInstruction insn) {
            translateInvokeInstruction(insn);
        }

        //@ requires insn != null;
        public void visitInvokeSpecialInstruction(InvokeSpecialInstruction insn) {
            translateInvokeInstruction(insn);
        }

        //@ requires insn != null;
        public void visitInvokeInterfaceInstruction(InvokeInterfaceInstruction insn) {
            translateInvokeInstruction(insn);
        }

        private void translateBinArithInstruction(int opcode) {
            int stackLeft = handle.getFrame().getStackSize() - 2;
            int stackRight = handle.getFrame().getStackSize() - 1;
            String left = intStackVar(stackLeft);
            String right = intStackVar(stackRight);
            BPLExpression expr;
            switch (opcode) {
            case IOpCodes.IADD:
            case IOpCodes.LADD:
                expr = add(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.ISUB:
            case IOpCodes.LSUB:
                expr = sub(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IMUL:
            case IOpCodes.LMUL:
                expr = multiply(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IDIV:
            case IOpCodes.LDIV:
                if(tc.getConfig().isNullChecks()){
                    addAssert(notEqual(stack(var(right)), intLiteral(0)),
                    		"!division by 0");
                } else {
                    addAssume(notEqual(stack(var(right)), intLiteral(0)));
                }
                expr = divide_int(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IREM:
            case IOpCodes.LREM:
            default:
                if(tc.getConfig().isNullChecks()){
                    addAssert(notEqual(stack(var(right)), intLiteral(0)),
                    		"!modulo by 0");
                } else {
                    addAssume(notEqual(stack(var(right)), intLiteral(0)));
                }
                expr = modulo_int(stack(var(left)), stack(var(right)));
                break;
            }
            addAssignment(stack(var(intStackVar(stackLeft))), expr);
        }

        //@ requires insn != null;
        public void visitIBinArithInstruction(IBinArithInstruction insn) {
            translateBinArithInstruction(insn.getOpcode());
            //overflow is not hanled by our implementation (assume result is of type int)
            addAssume(isInRange(stack(var(intStackVar(handle.getFrame().getStackSize() - 2))), typeRef(JBaseType.INT)));
        }

        //@ requires insn != null;
        public void visitLBinArithInstruction(LBinArithInstruction insn) {
            translateBinArithInstruction(insn.getOpcode());
            //overflow is not hanled by our implementation (assume result is of type long)
            addAssume(isInRange(stack(var(intStackVar(handle.getFrame().getStackSize() - 2))), typeRef(JBaseType.LONG)));
        }

        private void translateBitwiseInstruction(int opcode) {
            int stackLeft = handle.getFrame().getStackSize() - 2;
            int stackRight = handle.getFrame().getStackSize() - 1;
            String left = intStackVar(stackLeft);
            String right = intStackVar(stackRight);
            BPLExpression expr;
            switch (opcode) {
            case IOpCodes.ISHL:
            case IOpCodes.LSHL:
                expr = bitShl(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.ISHR:
            case IOpCodes.LSHR:
                expr = bitShr(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IUSHR:
            case IOpCodes.LUSHR:
                expr = bitUShr(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IAND:
            case IOpCodes.LAND:
                expr = bitAnd(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IOR:
            case IOpCodes.LOR:
                expr = bitOr(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IXOR:
            case IOpCodes.LXOR:
            default:
                expr = bitXor(stack(var(left)), stack(var(right)));
                break;
            }
            addAssignment(stack(var(intStackVar(stackLeft))), expr);
        }

        //@ requires insn != null;
        public void visitIBitwiseInstruction(IBitwiseInstruction insn) {
            translateBitwiseInstruction(insn.getOpcode());
            //overflow is not hanled by our implementation (assume result is of type int)
            addAssume(isInRange(stack(var(intStackVar(handle.getFrame().getStackSize() - 2))), typeRef(JBaseType.INT)));
        }

        //@ requires insn != null;
        public void visitLBitwiseInstruction(LBitwiseInstruction insn) {
            translateBitwiseInstruction(insn.getOpcode());
            //overflow is not hanled by our implementation (assume result is of type long)
            addAssume(isInRange(stack(var(intStackVar(handle.getFrame().getStackSize() - 2))), typeRef(JBaseType.LONG)));
        }

        //@ requires insn != null;
        public void visitINegInstruction(INegInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            addAssignment(stack(var(intStackVar(stack))), neg(stack(var(intStackVar(stack)))));
        }

        //@ requires insn != null;
        public void visitLNegInstruction(LNegInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            addAssignment(stack(var(intStackVar(stack))), neg(stack(var(intStackVar(stack)))));
        }

        //@ requires insn != null;
        public void visitIIncInstruction(IIncInstruction insn) {
            int local = insn.getIndex();
            int constant = insn.getConstant();
            BPLExpression iinc = add(stack(var(intLocalVar(local))), intLiteral(constant));
            addAssignment(stack(var(intLocalVar(local))), iinc);
            //overflow not handled by our implementation (assume type of result is int)
            addAssume(isInRange(stack(var(intLocalVar(local))), typeRef(JBaseType.INT)));
        }

        /**
         * Translates the given if instruction by modeling the program flow for the
         * cases in which the guard of the if instruction evaluates to {@code true}
         * or {@code false}.
         * 
         * @param insn The if instruction to translate.
         * @param cmpTrue The condition representing the guard of the if
         *          instruction.
         * @param cmpFalse The condition representing the negation of the guard of
         *          the if instruction.
         * @requires insn != null && cmpTrue != null && cmdFalse != null;
         */
        private void translateIfInstruction(
                AbstractIfInstruction insn,
                BPLExpression cmpTrue,
                BPLExpression cmpFalse) {
            InstructionHandle target = insn.getTarget();
            BasicBlock targetBlock = method.getCFG().findBlockStartingAt(target);

            // Construct the names of the BoogiePL blocks modeling the cases in which
            // the instruction's guard evaluates to true or false, respectively.
            String trueBlock = trueBranchBlockLabel(cfgBlock);
            String falseBlock = falseBranchBlockLabel(cfgBlock);
            endBlock(trueBlock, falseBlock);

            // First, we generate the block modeling the case in which the guard of
            // the if instruction evaluates to true.
            startBlock(trueBlock);
            addAssume(cmpTrue);
            endBlock(cfgBlock.getSuccessorEdge(targetBlock));

            // Subsequently, we generate the block modeling the case in which the
            // guard of the if instruction evaluates to false. Note that we do not end
            // this BoogiePL block since the translation of subsequent bytecode
            // instructions can be appended to it as the case in which the guard
            // evaluates to false always represents a fall through edge.
            startBlock(falseBlock);
            addAssume(cmpFalse);
        }

        //@ requires insn != null;
        public void visitIfICmpInstruction(IfICmpInstruction insn) {
            String left = intStackVar(handle.getFrame().getStackSize() - 2);
            String right = intStackVar(handle.getFrame().getStackSize() - 1);
            BPLExpression cmpTrue;
            BPLExpression cmpFalse;
            switch (insn.getOpcode()) {
            case IOpCodes.IF_ICMPEQ:
                cmpTrue = isEqual(stack(var(left)), stack(var(right)));
                cmpFalse = notEqual(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ICMPNE:
                cmpTrue = notEqual(stack(var(left)), stack(var(right)));
                cmpFalse = isEqual(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ICMPLT:
                cmpTrue = less(stack(var(left)), stack(var(right)));
                cmpFalse = greaterEqual(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ICMPGE:
                cmpTrue = greaterEqual(stack(var(left)), stack(var(right)));
                cmpFalse = less(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ICMPGT:
                cmpTrue = greater(stack(var(left)), stack(var(right)));
                cmpFalse = lessEqual(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ICMPLE:
            default:
                cmpTrue = lessEqual(stack(var(left)), stack(var(right)));
                cmpFalse = greater(stack(var(left)), stack(var(right)));
                break;
            }
            translateIfInstruction(insn, cmpTrue, cmpFalse);
        }

        //@ requires insn != null;
        public void visitIfACmpInstruction(IfACmpInstruction insn) {
            String left = refStackVar(handle.getFrame().getStackSize() - 2);
            String right = refStackVar(handle.getFrame().getStackSize() - 1);
            BPLExpression cmpTrue;
            BPLExpression cmpFalse;
            switch (insn.getOpcode()) {
            case IOpCodes.IF_ACMPEQ:
                cmpTrue = isEqual(stack(var(left)), stack(var(right)));
                cmpFalse = notEqual(stack(var(left)), stack(var(right)));
                break;
            case IOpCodes.IF_ACMPNE:
            default:
                cmpTrue = notEqual(stack(var(left)), stack(var(right)));
                cmpFalse = isEqual(stack(var(left)), stack(var(right)));
                break;
            }
            translateIfInstruction(insn, cmpTrue, cmpFalse);
        }

        //@ requires insn != null;
        public void visitIfInstruction(IfInstruction insn) {
            String operand = intStackVar(handle.getFrame().getStackSize() - 1);
            BPLExpression cmpTrue;
            BPLExpression cmpFalse;
            switch (insn.getOpcode()) {
            case IOpCodes.IFEQ:
                cmpTrue = isEqual(stack(var(operand)), intLiteral(0));
                cmpFalse = notEqual(stack(var(operand)), intLiteral(0));
                break;
            case IOpCodes.IFNE:
                cmpTrue = notEqual(stack(var(operand)), intLiteral(0));
                cmpFalse = isEqual(stack(var(operand)), intLiteral(0));
                break;
            case IOpCodes.IFLT:
                cmpTrue = less(stack(var(operand)), intLiteral(0));
                cmpFalse = greaterEqual(stack(var(operand)), intLiteral(0));
                break;
            case IOpCodes.IFGE:
                cmpTrue = greaterEqual(stack(var(operand)), intLiteral(0));
                cmpFalse = less(stack(var(operand)), intLiteral(0));
                break;
            case IOpCodes.IFGT:
                cmpTrue = greater(stack(var(operand)), intLiteral(0));
                cmpFalse = lessEqual(stack(var(operand)), intLiteral(0));
                break;
            case IOpCodes.IFLE:
            default:
                cmpTrue = lessEqual(stack(var(operand)), intLiteral(0));
                cmpFalse = greater(stack(var(operand)), intLiteral(0));
                break;
            }
            translateIfInstruction(insn, cmpTrue, cmpFalse);
        }

        //@ requires insn != null;
        public void visitIfNonNullInstruction(IfNonNullInstruction insn) {
            String operand = refStackVar(handle.getFrame().getStackSize() - 1);
            translateIfInstruction(insn, nonNull(stack(var(operand))), isNull(stack(var(operand))));
        }

        //@ requires insn != null;
        public void visitIfNullInstruction(IfNullInstruction insn) {
            String operand = refStackVar(handle.getFrame().getStackSize() - 1);
            translateIfInstruction(insn, isNull(stack(var(operand))), nonNull(stack(var(operand))));
        }

        //@ requires insn != null;
        public void visitLCmpInstruction(LCmpInstruction insn) {
            String left = intStackVar(handle.getFrame().getStackSize() - 2);
            String right = intStackVar(handle.getFrame().getStackSize() - 1);

            BPLExpression expr = ifThenElse(
                    greater(stack(var(left)), stack(var(right))),
                    intLiteral(1),
                    ifThenElse(
                            isEqual(stack(var(left)), stack(var(right))),
                            intLiteral(0),
                            intLiteral(-1)));

            addAssignment(stack(var(left)), cast(expr, BPLBuiltInType.INT));
        }

        //@ requires insn != null;
        public void visitGotoInstruction(GotoInstruction insn) {
            InstructionHandle target = insn.getTarget();
            BasicBlock targetBlock = method.getCFG().findBlockStartingAt(target);
            endBlock(cfgBlock.getSuccessorEdge(targetBlock));
        }

        //@ requires insn != null;
        public void visitLookupSwitchInstruction(LookupSwitchInstruction insn) {
            String stackVar = intStackVar(handle.getFrame().getStackSize() - 1);

            int[] keys = insn.getKeys();
            List<String> labels = new ArrayList<String>();
            for (int i = 0; i < keys.length; i++) {
                labels.add(caseBlockLabel(cfgBlock, i));
            }
            labels.add(defaultBlockLabel(cfgBlock));
            endBlock(labels.toArray(new String[labels.size()]));

            InstructionHandle[] targets = insn.getTargets();
            for (int i = 0; i < targets.length; i++) {
                startBlock(caseBlockLabel(cfgBlock, i));
                addAssume(isEqual(stack(var(stackVar)), intLiteral(keys[i])));
                BasicBlock caseBlock = method.getCFG().findBlockStartingAt(targets[i]);
                endBlock(cfgBlock.getSuccessorEdge(caseBlock));
            }
            InstructionHandle dfltTarget = insn.getDefaultTarget();
            startBlock(defaultBlockLabel(cfgBlock));
            if (keys.length > 0) {
                BPLExpression expr = notEqual(stack(var(stackVar)), intLiteral(keys[0]));
                for (int i = 1; i < keys.length; i++) {
                    expr = logicalAnd(expr, notEqual(stack(var(stackVar)), intLiteral(keys[i])));
                }
                addAssume(expr);
            }
            BasicBlock dfltBlock = method.getCFG().findBlockStartingAt(dfltTarget);
            endBlock(cfgBlock.getSuccessorEdge(dfltBlock));
        }

        //@ requires insn != null;
        public void visitTableSwitchInstruction(TableSwitchInstruction insn) {
            String stackVar = intStackVar(handle.getFrame().getStackSize() - 1);

            int minIdx = insn.getMinIndex();
            int maxIdx = insn.getMaxIndex();
            List<String> labels = new ArrayList<String>();
            for (int idx = minIdx; idx <= maxIdx; idx++) {
                labels.add(caseBlockLabel(cfgBlock, idx - minIdx));
            }
            labels.add(defaultBlockLabel(cfgBlock));
            endBlock(labels.toArray(new String[labels.size()]));

            InstructionHandle[] targets = insn.getTargets();
            for (int i = 0; i < targets.length; i++) {
                startBlock(caseBlockLabel(cfgBlock, i));
                addAssume(isEqual(stack(var(stackVar)), intLiteral(minIdx + i)));
                BasicBlock caseBlock = method.getCFG().findBlockStartingAt(targets[i]);
                endBlock(cfgBlock.getSuccessorEdge(caseBlock));
            }
            InstructionHandle dfltTarget = insn.getDefaultTarget();
            startBlock(defaultBlockLabel(cfgBlock));
            addAssume(logicalOr(less(stack(var(stackVar)), intLiteral(minIdx)), greater(
                    stack(var(stackVar)),
                    intLiteral(maxIdx))));
            BasicBlock dfltBlock = method.getCFG().findBlockStartingAt(dfltTarget);
            endBlock(cfgBlock.getSuccessorEdge(dfltBlock));
        }

        //@ requires insn != null;
        public void visitReturnInstruction(ReturnInstruction insn) {
            // endBlock(POST_BLOCK_LABEL);
            endBlock(EXIT_BLOCK_LABEL);
        }

        //@ requires insn != null;
        public void visitIReturnInstruction(IReturnInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;

            // addAssignment(var(RESULT_VAR), var(intStackVar(stack)));
            // endBlock(POST_BLOCK_LABEL);

//            addAssignment(var(RETURN_STATE_PARAM), var(NORMAL_RETURN_STATE));
            if(tc.isActive()){
                addAssignment(stack(var(RESULT_PARAM + INT_TYPE_ABBREV)), stack(var(intStackVar(stack)))); //TODO maybe this is a boolean
            } else {
                addAssignment(var(RESULT_PARAM + INT_TYPE_ABBREV), stack(var(intStackVar(stack)))); //TODO maybe this is a boolean
            }
            endBlock(EXIT_BLOCK_LABEL);
        }

        //@ requires insn != null;
        public void visitLReturnInstruction(LReturnInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;

            // addAssignment(var(RESULT_VAR), var(intStackVar(stack)));
            // endBlock(POST_BLOCK_LABEL);

            if(tc.isActive()){
                addAssignment(stack(var(RESULT_PARAM + INT_TYPE_ABBREV)), stack(var(intStackVar(stack))));
            } else {
                addAssignment(var(RESULT_PARAM + INT_TYPE_ABBREV), stack(var(intStackVar(stack))));
            }
            endBlock(EXIT_BLOCK_LABEL);
        }

        //@ requires insn != null;
        public void visitAReturnInstruction(AReturnInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;

            // addAssignment(var(RESULT_VAR), var(refStackVar(stack)));
            // endBlock(POST_BLOCK_LABEL);

            if(tc.isActive()){
                addAssignment(stack(var(RESULT_PARAM + REF_TYPE_ABBREV)), stack(var(refStackVar(stack))));
            } else {
                addAssignment(var(RESULT_PARAM + REF_TYPE_ABBREV), stack(var(refStackVar(stack))));
            }
            endBlock(EXIT_BLOCK_LABEL);
        }

        //@ requires insn != null;
        public void visitAThrowInstruction(AThrowInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;

//            if(!tc.isActive()){
//                translateRuntimeException(
//                        "java.lang.NullPointerException",
//                        nonNull(stack(var(refStackVar(stack)))));
//            }
//
//            if (stack != 0) {
//                addAssignment(stack(var(refStackVar(0))), stack(var(refStackVar(stack))));
//            }
//
//            branchToHandlers(handle.getFrame().peek());
//            translateReachableExceptionHandlers();
            addCommentedCommand(new BPLAssumeCommand(BPLBoolLiteral.FALSE), "throw exception");
        }

        //@ requires insn != null;
        public void visitNewInstruction(NewInstruction insn) {
            int stack = handle.getFrame().getStackSize();
            addHavoc(var(swapVar(JNullType.NULL)));
            addAssignment(stack(var(refStackVar(stack))), var(swapVar(JNullType.NULL)));
            addAssume(logicalNot(heap(stack(var(refStackVar(stack))), var(ALLOC_FIELD))));
            addAssignment(heap(stack(var(refStackVar(stack))), var(ALLOC_FIELD)), BPLBoolLiteral.TRUE);
            addAssume(nonNull(stack(var(refStackVar(stack)))));
            addAssume(isEqual(typ(stack(var(refStackVar(stack))), var(tc.getHeap())), typeRef(insn.getType())));
            addAssume(logicalNot(heap(stack(var(refStackVar(stack))), var(CREATED_BY_CTXT_FIELD))));
            addAssume(logicalNot(heap(stack(var(refStackVar(stack))), var(EXPOSED_FIELD))));
            if(tc.getConfig().isAssumeWellformedHeap()){
                addAssume(wellformedHeap(var(tc.getHeap())));
            }
            
            //      addHavoc(var(refStackVar(stack)));
            //TODO do we need to do anything to reserve the memory space on the heap?
            //      addAssume(isEqual(
            //          heapNew(context, var(tc.getHeap()), insn.getType()),
            //          rval(var(refStackVar(stack)))));
            //      addAssignment(var(tc.getHeap()), heapAdd(context, var(tc.getHeap()), insn.getType()));
        }

        //@ requires allocationType != null;
        private void translateNewArrayInstruction(JType allocationType) {
            int stack = handle.getFrame().getStackSize() - 1;
            String ref = refStackVar(stack);
            String len = intStackVar(stack);

            if(!tc.isActive()){
                translateRuntimeException(
                        "java.lang.NegativeArraySizeException",
                        lessEqual(intLiteral(0), stack(var(len))));
            }

            addHavoc(var(swapVar(JNullType.NULL)));
            addAssignment(stack(var(ref)), var(swapVar(JNullType.NULL)));
            //      addHavoc(var(ref)));
            //TODO do we need to do anything to reserve the memory space on the heap?
            //      addAssume(isEqual(heapNewArray(
            //          context,
            //          var(tc.getHeap()),
            //          allocationType,
            //          var(len)), rval(var(ref))));
            //      addAssignment(var(tc.getHeap()), heapAddArray(
            //          context,
            //          var(tc.getHeap()),
            //          allocationType,
            //          var(len)));
        }

        //@ requires insn != null;
        public void visitNewArrayInstruction(NewArrayInstruction insn) {
            translateNewArrayInstruction(insn.getType());
        }

        //@ requires insn != null;
        public void visitANewArrayInstruction(ANewArrayInstruction insn) {
            translateNewArrayInstruction(insn.getType());
        }

        //@ requires type != null;
        private BPLExpression buildMultiArrayAllocation(
                JArrayType type,
                int dimension,
                int lengthIdx) {
            //TODO handle arrays
//            if (dimension == 1) {
//                return arrayAlloc(
//                        typeRef(type.getIndexedType()),
//                        stack(var(intStackVar(lengthIdx))));
//            } else {
//                return multiArrayAlloc(
//                        typeRef(type.getIndexedType()),
//                        stack(var(intStackVar(lengthIdx))),
//                        buildMultiArrayAllocation(
//                                (JArrayType) type.getIndexedType(),
//                                dimension - 1,
//                                lengthIdx + 1));
//            }
            return nullLiteral();
        }

        //@ requires insn != null;
        public void visitMultiANewArrayInstruction(MultiANewArrayInstruction insn) {
            JArrayType type = insn.getType();
            int dims = insn.getDimensionCount();
            int first = handle.getFrame().getStackSize() - dims;
            String ref = refStackVar(first);

            BPLExpression[] vc = new BPLExpression[dims];
            for (int i = 0; i < dims; i++) {
                vc[i] = lessEqual(intLiteral(0), stack(var(intStackVar(first + i))));
            }
            if(!tc.isActive()){
                translateRuntimeException("java.lang.NegativeArraySizeException", vc);
            }

            addHavoc(var(swapVar(JNullType.NULL)));
            addAssignment(stack(var(ref)), var(swapVar(JNullType.NULL)));
            //      addHavoc(stack(var(ref)));
            //TODO do we need to do anything to reserve the memory space on the heap?
            //      addAssume(isEqual(heapNew(var(tc.getHeap()), buildMultiArrayAllocation(
            //          type,
            //          dims,
            //          first)), rval(var(ref))));
            //      addAssignment(
            //          var(tc.getHeap()),
            //          heapAdd(
            //              var(tc.getHeap()),
            //              buildMultiArrayAllocation(type, dims, first)
            //          )
            //      );
        }

        //@ requires insn != null;
        public void visitCheckCastInstruction(CheckCastInstruction insn) {
            String stackVar = refStackVar(handle.getFrame().getStackSize() - 1);
            BPLExpression type = typeRef(insn.getType());

            //TODO implement cast checks
            //      translateRuntimeException("java.lang.ClassCastException", isOfType(
            //          rval(var(stackVar)),
            //          type));
        }

        //@ requires insn != null;
        public void visitVCastInstruction(VCastInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            BPLExpression type = typeRef(insn.getTargetType());

            //TODO check the correct type is retrieved from the stack
            addAssignment(stack(var(intStackVar(stack))), intToInt(
                    stack(var(intStackVar(stack))),
                    typeRef(handle.getFrame().peek()),
                    type));
        }

        //@ requires insn != null;
        public void visitInstanceOfInstruction(InstanceOfInstruction insn) {
            int stack = handle.getFrame().getStackSize() - 1;
            BPLExpression type = typeRef(insn.getType());

            addAssignment(stack(var(intStackVar(stack))), bool2int(isInstanceOf(
                    stack(var(refStackVar(stack))),
                    var(tc.getHeap()),
                    type)));
        }

        public void visitPopInstruction(PopInstruction insn) {
            // do nothing
        }

        public void visitPop2Instruction(Pop2Instruction insn) {
            // do nothing
        }

        //@ requires insn != null;
        public void visitSwapInstruction(SwapInstruction insn) {
            int stack1 = handle.getFrame().getStackSize() - 1;
            int stack2 = handle.getFrame().getStackSize() - 2;
            JType type = handle.getFrame().peek();
            addAssignment(var(swapVar(type)), stack(var(stackVar(stack2, type))));
            addAssignment(stack(var(stackVar(stack2, type))), stack(var(stackVar(stack1, type))));
            addAssignment(stack(var(stackVar(stack1, type))), var(swapVar(type)));
        }

        private void translateDupInstruction(int dupCount, int offset) {
            int top = handle.getFrame().getStackSize() - 1;
            for (int i = 0; i < offset; i++) {
                int from = top - i;
                int to = from + dupCount;
                JType type = handle.getFrame().peek(from);
                addAssignment(stack(var(stackVar(to, type))), stack(var(stackVar(from, type))));

            }
            if (dupCount < offset) {
                for (int i = 0; i < dupCount; i++) {
                    int from = (top + dupCount) - i;
                    int to = from - offset;
                    JType type = handle.getFrame().peek(from - dupCount);
                    addAssignment(stack(var(stackVar(to, type))), stack(var(stackVar(from, type))));
                }
            }
        }

        //@ requires insn != null;
        public void visitDupInstruction(DupInstruction insn) {
            translateDupInstruction(1, 1);
        }

        //@ requires insn != null;
        public void visitDup2Instruction(Dup2Instruction insn) {
            int stack1 = handle.getFrame().getStackSize() - 1;
            JType type1 = handle.getFrame().peek(stack1);
            if (type1.isCategory1CompType()) {
                translateDupInstruction(2, 2);
            } else {
                translateDupInstruction(1, 1);
            }
        }

        //@ requires insn != null;
        public void visitDupX1Instruction(DupX1Instruction insn) {
            translateDupInstruction(1, 2);
        }

        //@ requires insn != null;
        public void visitDupX2Instruction(DupX2Instruction insn) {
            int stack2 = handle.getFrame().getStackSize() - 2;
            JType type2 = handle.getFrame().peek(stack2);
            if (type2.isCategory1CompType()) {
                translateDupInstruction(1, 3);
            } else {
                translateDupInstruction(1, 2);
            }
        }

        //@ requires insn != null;
        public void visitDup2X1Instruction(Dup2X1Instruction insn) {
            int stack1 = handle.getFrame().getStackSize() - 1;
            JType type1 = handle.getFrame().peek(stack1);
            if (type1.isCategory1CompType()) {
                translateDupInstruction(2, 3);
            } else {
                translateDupInstruction(1, 2);
            }
        }

        //@ requires insn != null;
        public void visitDup2X2Instruction(Dup2X2Instruction insn) {
            int stack1 = handle.getFrame().getStackSize() - 1;
            int stack2 = handle.getFrame().getStackSize() - 2;
            JType type1 = handle.getFrame().peek(stack1);
            JType type2 = handle.getFrame().peek(stack2);
            if (!type1.isCategory1CompType() && !type2.isCategory1CompType()) {
                translateDupInstruction(1, 2);
            } else {
                int stack3 = handle.getFrame().getStackSize() - 3;
                JType type3 = handle.getFrame().peek(stack3);
                if (!type3.isCategory1CompType()) {
                    translateDupInstruction(2, 3);
                } else if (!type1.isCategory1CompType()) {
                    translateDupInstruction(1, 3);
                } else {
                    translateDupInstruction(2, 4);
                }
            }
        }

        //@ requires exception != null;
        private void branchToHandlers(JType exception) {
            Set<String> labels = new LinkedHashSet<String>();
            boolean definitelyHandled = false;
            JType tightestHandlerType = JNullType.NULL;
            for (ExceptionHandler handler : method.getExceptionHandlers()) {
                if (handler.isActiveFor(handle)) {
                    JType handlerType = handler.getType();
                    if (exception.isSubtypeOf(handlerType)) {
                        labels.add(handlerBlockLabel(cfgBlock, handlerType));
                        definitelyHandled = true;
                        break;
                    } else if (handlerType.isSubtypeOf(exception)
                            && tightestHandlerType.isProperSubtypeOf(handlerType)) {
                        labels.add(handlerBlockLabel(cfgBlock, handlerType));
                        tightestHandlerType = handlerType;
                    }
                }
            }
            if (!definitelyHandled) {
                for (JClassType methodException : method.getExceptionTypes()) {
                    if (exception.isSubtypeOf(methodException)
                            || (methodException.isSubtypeOf(exception) && tightestHandlerType
                                    .isProperSubtypeOf(methodException))) {
                        labels.add(postXBlockLabel(methodException));
                    }
                }
                if (labels.size() == 0) {
                    labels.add(EXIT_BLOCK_LABEL);
                }
            }
            endBlock(labels.toArray(new String[labels.size()]));
        }

        /**
         * Translates a special BoogiePL block for all the exception handlers which
         * are reachable from the current instruction. The block assumes all the
         * type information which is guaranteed whenever the corresponding exception
         * handler is reached at runtime.
         */
        private void translateReachableExceptionHandlers() {
            ExceptionHandler[] handlers = method.getExceptionHandlers();
            for (int i = 0; i < handlers.length; i++) {
                ExceptionHandler handler = handlers[i];
                if (handler.isActiveFor(handle)) {
                    InstructionHandle target = handler.getHandler();
                    BasicBlock block = method.getCFG().findBlockStartingAt(target);
                    // Check whether the exception handler is reachable from the
                    // current basic block in the method's control flow graph.
                    if ((block != null) && cfgBlock.isSuccessor(block)) {
                        JType type = handler.getType();
                        startBlock(handlerBlockLabel(cfgBlock, type));

                        // addAssume(isExceptionalReturnState(var(RETURN_STATE_PARAM )));
//                        addAssignment(
//                                var(RETURN_STATE_PARAM),
//                                var(EXCEPTIONAL_RETURN_STATE));
                        // Assume that the exception object is of the handler's exception type.
                        //            addAssume(isInstanceOf(rval(var(refStackVar(0))), typeRef(type)));
                        // FIXME addAssume(isInstanceOf(rval(var(EXCEPTION_PARAM)), typeRef(type)));

                        // For any previous exception handler at the current instruction,
                        // assume that the type of the exception object is not a subtype
                        // of it since, otherwise, the exception would always be caught
                        // by the previous handler.
                        //            for (int j = 0; j < i; j++) {
                        //              if (handlers[j].getType().isProperSubtypeOf(type)) {
                        //                addAssume(logicalNot(isInstanceOf(
                        //                    rval(var(refStackVar(0))),
                        //                    typeRef(handlers[j].getType()))));
                        //              }
                        //            }

                        addAssignment(var(EXCEPTION_PARAM), stack(var(refStackVar(0))));
                        // FIXME addAssignment(var(RETURN_VALUE_PARAM), var(refStackVar(0)));

                        endBlock(cfgBlock.getSuccessorEdge(block));
                    }
                }
            }
        }
    }
}

package b2bpl.translation;

import static b2bpl.translation.CodeGenerator.add;
import static b2bpl.translation.CodeGenerator.asDirectSubClass;
import static b2bpl.translation.CodeGenerator.baseClass;
import static b2bpl.translation.CodeGenerator.bijective;
import static b2bpl.translation.CodeGenerator.bitAnd;
import static b2bpl.translation.CodeGenerator.bitOr;
import static b2bpl.translation.CodeGenerator.bitShl;
import static b2bpl.translation.CodeGenerator.bitShr;
import static b2bpl.translation.CodeGenerator.classExtends;
import static b2bpl.translation.CodeGenerator.classRepr;
import static b2bpl.translation.CodeGenerator.classReprInv;
import static b2bpl.translation.CodeGenerator.definesMethod;
import static b2bpl.translation.CodeGenerator.divide;
import static b2bpl.translation.CodeGenerator.exists;
import static b2bpl.translation.CodeGenerator.fieldType;
import static b2bpl.translation.CodeGenerator.forall;
import static b2bpl.translation.CodeGenerator.ftype;
import static b2bpl.translation.CodeGenerator.greater;
import static b2bpl.translation.CodeGenerator.hasReturnValue;
import static b2bpl.translation.CodeGenerator.ifThenElse;
import static b2bpl.translation.CodeGenerator.implies;
import static b2bpl.translation.CodeGenerator.intToInt;
import static b2bpl.translation.CodeGenerator.internal;
import static b2bpl.translation.CodeGenerator.isAllocated;
import static b2bpl.translation.CodeGenerator.isClassType;
import static b2bpl.translation.CodeGenerator.isEqual;
import static b2bpl.translation.CodeGenerator.isEquiv;
import static b2bpl.translation.CodeGenerator.isInRange;
import static b2bpl.translation.CodeGenerator.isInstanceOf;
import static b2bpl.translation.CodeGenerator.isMemberlessType;
import static b2bpl.translation.CodeGenerator.isNull;
import static b2bpl.translation.CodeGenerator.isOfType;
import static b2bpl.translation.CodeGenerator.isStaticField;
import static b2bpl.translation.CodeGenerator.isStaticMethod;
import static b2bpl.translation.CodeGenerator.isSubtype;
import static b2bpl.translation.CodeGenerator.isValueType;
import static b2bpl.translation.CodeGenerator.less;
import static b2bpl.translation.CodeGenerator.lessEqual;
import static b2bpl.translation.CodeGenerator.libType;
import static b2bpl.translation.CodeGenerator.logicalAnd;
import static b2bpl.translation.CodeGenerator.logicalNot;
import static b2bpl.translation.CodeGenerator.logicalOr;
import static b2bpl.translation.CodeGenerator.map;
import static b2bpl.translation.CodeGenerator.map1;
import static b2bpl.translation.CodeGenerator.memberOf;
import static b2bpl.translation.CodeGenerator.modulo;
import static b2bpl.translation.CodeGenerator.multiply;
import static b2bpl.translation.CodeGenerator.nonNull;
import static b2bpl.translation.CodeGenerator.notEqual;
import static b2bpl.translation.CodeGenerator.nullLiteral;
import static b2bpl.translation.CodeGenerator.obj;
import static b2bpl.translation.CodeGenerator.objectCoupling;
import static b2bpl.translation.CodeGenerator.oneClassDown;
import static b2bpl.translation.CodeGenerator.quantVarName;
import static b2bpl.translation.CodeGenerator.refOfType;
import static b2bpl.translation.CodeGenerator.relNull;
import static b2bpl.translation.CodeGenerator.sub;
import static b2bpl.translation.CodeGenerator.trigger;
import static b2bpl.translation.CodeGenerator.typ;
import static b2bpl.translation.CodeGenerator.unique;
import static b2bpl.translation.CodeGenerator.validHeapSucc;
import static b2bpl.translation.CodeGenerator.var;
import static b2bpl.translation.CodeGenerator.wellformedCoupling;
import static b2bpl.translation.CodeGenerator.wellformedHeap;
import static b2bpl.translation.CodeGenerator.wellformedStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import b2bpl.Project;
import b2bpl.bpl.ast.BPLArrayAssignment;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLArrayType;
import b2bpl.bpl.ast.BPLAxiom;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLBuiltInType;
import b2bpl.bpl.ast.BPLConstantDeclaration;
import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunction;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLFunctionParameter;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLParameterizedType;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLTrigger;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLTypeAlias;
import b2bpl.bpl.ast.BPLTypeDeclaration;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableDeclaration;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.JBaseType;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.JType;
import b2bpl.bytecode.TypeLoader;
import de.unikl.bcverifier.TranslationController;


/**
 * The main entry point to the translation of a set of bytecode classes to a
 * BoogiePL program.
 *
 * <p>
 * Some aspects of the translation process can be configured by passing an
 * appropriate {@link Project} instance containing the desired translation
 * settings upon creating the {@code Translator}. In particular, the
 * following aspects of the translation can be configured:
 * <ul>
 *   <li>
 *     The verification methodology for object invariants
 *     (see {@link Project#isThisInvariantsOnly()}).
 *   </li>
 *   <li>
 *     Whether to explicitly model runtime exceptions instead of ruling them
 *     out by verification conditions
 *     (see {@link Project#isModelRuntimeExceptions()}).
 *   </li>
 *   <li>
 *     The maximal magnitude of integer constants to represent explicitly in
 *     the BoogiePL program (see {@link Project#getMaxIntConstant()}).
 *   </li>
 * </ul>
 * </p>
 *
 * <p>
 * The {@code Translator} is <i>immediately</i> responsible for generating
 * the following parts of the resulting BoogiePL program:
 * <ul>
 *   <li>
 *     The core part of the background theory which is the same for every
 *     translation. This mainly includes the heap and core type system
 *     axiomatization (array subtyping, value type ranges, ...).
 *   </li>
 *   <li>
 *     The global axiomatization which depends on the concrete bytecode classes
 *     being translated. As an example, every type referenced in a class will
 *     trigger the declaration of a constant representing the type as well as
 *     the generation of a set of axioms defining the type's supertype
 *     hierarchy as well as other properties such as whether the type is final
 *     or not. References to fields and special constants (such as strings)
 *     also lead to a set of global declarations being generated.
 *   </li>
 * </ul>
 * The following parts of a bytecode class are not directly translated by this
 * class but their translation is instead <i>delegated</i> to other classes:
 * <ul>
 *   <li>
 *     The actual BML specifications which appear in the global section of the
 *     resulting BoogiePL program (in particular type specifications such as
 *     invariants) are translated by a {@link SpecificationTranslator}.
 *   </li>
 *   <li>
 *     The individual methods are translated by a {@link MethodTranslator}
 *     which maps every bytecode method to a single BoogiePL procedure.
 *   </li>
 * </ul>
 * Since BML specifications and bytecode methods may contain type references
 * and other references which may require global axioms to be generated, the
 * {@code Translator} passes a {@link ITranslationContext} to every
 * {@code SpecificationTranslator} and {@code MethodTranslator} which
 * should be used to translate those references.
 * </p>
 *
 * @see Project#isThisInvariantsOnly()
 * @see Project#isModelRuntimeExceptions()
 * @see Project#getMaxIntConstant()
 * @see SpecificationTranslator
 * @see MethodTranslator
 * @see ITranslationContext
 *
 * @author Ovidio Mallo, Samuel Willimann
 */
public class Translator implements ITranslationConstants {

    /** The project containing the settings of the translation. */
    private final Project project;
    
    /** The translation controller to use */
    private TranslationController tc;

    /**
     * The {@code TranslationContext} responsible for resolving special
     * references (type/field/string/... references) encountered in the bytecode
     * classes being translated. This context is passed to all
     * {@code SpecificationTranslator}s and {@code MethodTranslator}s
     * to which part of the translation is delegated.
     */
    private Context context;

    /**
     * The set of declarations generated during the translation of the given
     * bytecode classes. These make up the resulting BoogiePL program.
     */
    private List<BPLDeclaration> declarations;

    /**
     * Accumulates a set of comments which are then attached to the next
     * declaration added to the BoogiePL program being generated.
     */
    private List<String> declarationComments = new ArrayList<String>();

    /** The set of value types explicitly supported by the translation. */
    private static final JBaseType[] valueTypes = new JBaseType[] {
        JBaseType.LONG,
        JBaseType.INT,
        JBaseType.SHORT,
        JBaseType.BYTE,
        JBaseType.BOOLEAN,
        JBaseType.CHAR
    };

    public void setTranslationController(TranslationController controller){
        this.tc = controller;
    }
    
    /**
     * Creates a new translator which is configured by the given
     * {@code project}.
     * Once a translator has been created, it can be used to translate different
     * bytecode classes under the same configuration (given by the here provided
     * {@code project}).
     *
     * @param project  The project containing the configurations of the
     *                 translation.
     *
     * @see #translate(JClassType[])
     */
    public Translator(Project project) {
        this.project = project;
    }

    /**
     * Performs the actual translation of the given bytecode classes and returns
     * a BoogiePL program representing it.
     *
     * @param types  The bytecode classes to be translated.
     * @return       The BoogiePL program resulting from the translation of
     *               the given bytecode classes.
     */
    public BPLProgram translate(JClassType... types) {
        context = new Context();
        declarations = new ArrayList<BPLDeclaration>();
        MethodTranslator methodTranslator = new MethodTranslator(project);
        methodTranslator.setTranslationController(tc);
        generateTheory();
        for (JClassType type : types) {
            for (BCMethod method : type.getMethods()) {
                if (!method.isAbstract()
                        && !method.isNative()
                        && !method.isSynthetic()) {
                    addDeclaration(methodTranslator.translate(context, method));
                }
            }
        }
        flushPendingTheory();
        return new BPLProgram(
                declarations.toArray(new BPLDeclaration[declarations.size()]));
    }

    public Map<String, BPLProcedure> translateMethods(JClassType... types) {
        Map<String, BPLProcedure> procedures = new HashMap<String, BPLProcedure>();

        context = new Context();
        declarations = new ArrayList<BPLDeclaration>();
        MethodTranslator methodTranslator = new MethodTranslator(project);
        methodTranslator.setTranslationController(tc);

        BPLProcedure proc;
        

        List<BPLExpression> libTypeExpressions = new ArrayList<BPLExpression>();
        String t = "t";
        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        for (JClassType type : types) {
            for (BCMethod method : type.getMethods()) {
                final String methodName;
                methodName = GLOBAL_VAR_PREFIX+MethodTranslator.getMethodName(method);
                if (!method.isAbstract()
                        && !method.isNative()
                        && !method.isSynthetic()) {

                    proc = methodTranslator.translate(context, method);
                    if(!tc.declaredMethods().contains(methodName)){
                        Logger.getLogger(Translator.class).debug("Adding method "+methodName);
                        addConstants(new BPLVariable(methodName, new BPLTypeName(METHOD_TYPE)));
                        tc.declaredMethods().add(methodName);
                    }
//                    declarations.add(new BPLAxiom(new BPLFunctionApplication(DEFINES_METHOD, typeRef(type), var(methodName))));
                    tc.definesMethod(VALUE_TYPE_PREFIX+type.getName(), methodName);
                    if(method.isStatic()){
                        addAxiom(isStaticMethod(var(methodName)));
                    } else {
                        addAxiom(logicalNot(isStaticMethod(var(methodName))));
                    }
                    if(method.isPublic()){
                        addAxiom(new BPLFunctionApplication(IS_CALLABLE_FUNC, typeRef(type), var(methodName)));
                    } else {
                        addAxiom(logicalNot(new BPLFunctionApplication(IS_CALLABLE_FUNC, typeRef(type), var(methodName))));
                    }
                    if(method.isConstructor() || !method.isVoid()){
                        addAxiom(hasReturnValue(var(methodName)));
                    } else {
                        addAxiom(logicalNot(hasReturnValue(var(methodName))));
                    }
                    procedures.put(method.getQualifiedBoogiePLName(), proc);
                } else if(method.isAbstract()){
                    if(!tc.declaredMethods().contains(methodName)){
                        Logger.getLogger(Translator.class).debug("Adding abstract method "+methodName);
                        addConstants(new BPLVariable(methodName, new BPLTypeName(METHOD_TYPE)));
                        tc.declaredMethods().add(methodName);
                    }
                }
            }
//            declarations.add(new BPLAxiom(libType(typeRef(type))));
            libTypeExpressions.add(isEqual(var(t), typeRef(type)));
            if(!type.isInterface()){
                addAxiom(forall(tVar, isEquiv(classExtends(typeRef(type), var(t)), isEqual(var(t), typeRef(type.getSupertype())))));
            }
            
            if(tc.methodDefinitions().get(VALUE_TYPE_PREFIX+type.getName()) == null){ // if we not added any methods up to now, the class does not implement any
                tc.definesNoMethods(VALUE_TYPE_PREFIX+type.getName());
            }
        }
        if(libTypeExpressions.size() > 0){
            addAxiom(forall(tVar, isEquiv(libType(var(t)), logicalOr(libTypeExpressions.toArray(new BPLExpression[libTypeExpressions.size()])))));
        }

        return procedures;
    }

    public List<BPLDeclaration> getPrelude() {
        context = new Context();
        declarations = new ArrayList<BPLDeclaration>();
        generateTheory();
        flushPendingTheory();
        return declarations;
    }

    public List<BPLDeclaration> getNeededDeclarations() {
        return declarations;
    }

    /**
     * Adds the given {@code comment} to the set of already accumulated comments
     * which will be attach to the next declaration of the BoogiePL program being
     * generated.
     *
     * @param comment  The comment to attach to the next declaration of the
     *                 BoogiePL program.
     */
    private void addComment(String comment) {
        declarationComments.add(comment);
    }

    /**
     * Adds the given {@code declaration} to the global section of the BoogiePL
     * program being generated and attaches all pending comments to it.
     *
     * @param declaration  The declaration to add to the BoogiePL program.
     */
    private void addDeclaration(BPLDeclaration declaration) {
        for (String comment : declarationComments) {
            declaration.addComment(comment);
        }
        declarationComments.clear();

        declarations.add(declaration);
    }

    /**
     * Convenience method for adding the given {@code axiom} to the global
     * declarations of the BoogiePL program being generated.
     *
     * @param axiom  The axiom to add to the BoogiePL program.
     */
    private void addAxiom(BPLExpression axiom) {
        addDeclaration(new BPLAxiom(axiom));
    }

    /**
     * Convenience method for adding a set of {@code constants} to the global
     * declarations of the BoogiePL program being generated.
     *
     * @param constants  The constants to add to the BoogiePL program.
     */
    private void addConstants(BPLVariable... constants) {
        addDeclaration(new BPLConstantDeclaration(constants));
    }

    private void addFunction(String name, BPLType inType, BPLType outType) {
        addFunction(name, new BPLType[] { inType }, outType);
    }

    private void addFunction(
            String name,
            BPLType inType1,
            BPLType inType2,
            BPLType outType) {
        addFunction(name, new BPLType[] { inType1, inType2 }, outType);
    }

    private void addFunction(
            String name,
            BPLType inType1,
            BPLType inType2,
            BPLType inType3,
            BPLType outType) {
        addFunction(name, new BPLType[] { inType1, inType2, inType3 }, outType);
    }

    private void addFunction(
            String name,
            BPLType[] inTypes,
            BPLType outType) {
        BPLFunctionParameter[] inParameters =
                new BPLFunctionParameter[inTypes.length];
        for (int i = 0; i < inParameters.length; i++) {
            inParameters[i] = new BPLFunctionParameter(inTypes[i]);
        }
        addFunction(name, inParameters, new BPLFunctionParameter(outType));
    }

    private void addFunction(
            String name,
            BPLFunctionParameter[] inParameters,
            BPLFunctionParameter outParameter) {
        addDeclaration(new BPLFunction(name, inParameters, outParameter));
    }

    /**
     * Convenience method for adding a set of user-defined types with the given
     * {@code names} to the global declarations of the BoogiePL program being
     * generated.
     *
     * @param names  The names of the user-defined types to add to the BoogiePL
     *               program.
     */
    private void addType(String name, String... params) {
        addDeclaration(new BPLTypeDeclaration(name, params));
    }

    /**
     * Returns the name of a BoogiePL constant to be used to reference the given
     * value {@code type}.
     *
     * @param type  The value type for which to build the constant name.
     * @return      The name of the constant representing the given value
     *              {@code type}.
     */
    private String getValueTypeName(JBaseType type) {
        return VALUE_TYPE_PREFIX + type.getName();
    }

    /**
     * Returns the name of a BoogiePL constant to be used to reference the given
     * class {@code type}.
     *
     * @param type  The class type for which to build the constant name.
     * @return      The name of the constant representing the given class
     *              {@code type}.
     */
    private String getClassTypeName(JClassType type) {
        return GLOBAL_VAR_PREFIX + type.getName();
    }

    /**
     * Returns the smallest integer constant in the value range of the given
     * {@code type}.
     *
     * @return  The smallest integer constant of the given {@code type}.
     */
    private long getMinValue(JBaseType type) {
        if (type.equals(JBaseType.INT)) {
            return Integer.MIN_VALUE;
        } else if (type.equals(JBaseType.SHORT)) {
            return Short.MIN_VALUE;
        } else if (type.equals(JBaseType.BYTE)) {
            return Byte.MIN_VALUE;
        } else if (type.equals(JBaseType.BOOLEAN)) {
            return 0;
        } else if (type.equals(JBaseType.CHAR)) {
            return Character.MIN_VALUE;
        } else if (type.equals(JBaseType.LONG)) {
            return Long.MIN_VALUE;
        }
        throw new IllegalArgumentException("internal error");
    }

    /**
     * Returns the greatest integer constant in the value range of the given
     * {@code type}.
     *
     * @return  The greatest integer constant of the given {@code type}.
     */
    private long getMaxValue(JBaseType type) {
        if (type.equals(JBaseType.INT)) {
            return Integer.MAX_VALUE;
        } else if (type.equals(JBaseType.SHORT)) {
            return Short.MAX_VALUE;
        } else if (type.equals(JBaseType.BYTE)) {
            return Byte.MAX_VALUE;
        } else if (type.equals(JBaseType.BOOLEAN)) {
            return 1;
        } else if (type.equals(JBaseType.CHAR)) {
            return Character.MAX_VALUE;
        } else if (type.equals(JBaseType.LONG)) {
            return Long.MAX_VALUE;
        }
        throw new IllegalArgumentException("internal error");
    }

    /**
     * Returns the name of a BoogiePL constant representing the given
     * {@code literal}.
     * This method is used for integer values which are abstractly represented
     * by constants since their magnitude is considered to be too large to be
     * handled by theorem provers.
     *
     * @param literal  The value for which to build the constant name.
     * @return         The name of the constant representing the given
     *                 {@code literal}.
     */
    private String getSymbolicIntLiteralName(long literal) {
        return INT_LITERAL_PREFIX + String.valueOf(literal).replace('-', 'm');
    }

    /**
     * Convenience method for translating a type reference.
     *
     * @param type  The type reference to translate.
     * @return      A BoogiePL expression representing the given type reference.
     */
    private BPLExpression typeRef(JType type) {
        return context.translateTypeReference(type);
    }

    /**
     * Convenience method for translating an integer constant.
     *
     * @param literal  The integer constant to translate.
     * @return         A BoogiePL expression representing the given integer
     *                 constant.
     */
    private BPLExpression intLiteral(long literal) {
        return context.translateIntLiteral(literal);
    }

    /**
     * Generates the core part of the background theory which is the same for
     * every translation.
     */
    private void generateTheory() {
        //        axiomatizeHeap();
        //        axiomatizeHelperFunctions();
        //        axiomatizeTypeSystem();
        //        axiomatizeArithmetic();
        //        axiomatizeBitwiseInstructions();

        // needed variable declarations
        {
            final String heap1 = TranslationController.HEAP1;
            final String heap2 = TranslationController.HEAP2;
            final String related = RELATED_RELATION;
            final String alloc = ALLOC_FIELD;
            final String exposed = EXPOSED_FIELD;
            final String createdByCtxt = CREATED_BY_CTXT_FIELD;
            final String r = "r";
            BPLVariable refVar = new BPLVariable(r, new BPLTypeName(REF_TYPE));
            final String heap = "heap";
            BPLVariable heapVar = new BPLVariable(heap, new BPLTypeName(HEAP_TYPE));
            final String f = "f";
            BPLVariable fieldRefVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)));
            BPLVariable fieldIntVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.INT));
            BPLVariable fieldBoolVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.BOOL));
            BPLVariable heap1Var = new BPLVariable(heap1, new BPLTypeName(HEAP_TYPE));
            BPLVariable heap2Var = new BPLVariable(heap2, new BPLTypeName(HEAP_TYPE));
            BPLVariable relatedVar = new BPLVariable(related, new BPLTypeName(BIJ_TYPE));
            final String r1 = "r1";
            BPLVariable r1Var = new BPLVariable(r1, new BPLTypeName(REF_TYPE));
            final String r2 = "r2";
            BPLVariable r2Var = new BPLVariable(r2, new BPLTypeName(REF_TYPE));
            final String r3 = "r3";
            BPLVariable r3Var = new BPLVariable(r3, new BPLTypeName(REF_TYPE));
            final String r4 = "r4";
            BPLVariable r4Var = new BPLVariable(r4, new BPLTypeName(REF_TYPE));
            final String a = "a";
            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(NAME_TYPE));
            final String b = "b";
            BPLVariable bVar = new BPLVariable(b, new BPLTypeName(NAME_TYPE));
            final String c = "c";
            BPLVariable cVar = new BPLVariable(c, new BPLTypeName(NAME_TYPE));
            final String o = "o";
            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
            final String t = "t";
            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
            final String u = "u";
            BPLVariable uVar = new BPLVariable(u, new BPLTypeName(NAME_TYPE));
            final String i = "i";
            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
            final String bool = "b";
            BPLVariable boolVar = new BPLVariable(bool, BPLBuiltInType.BOOL);
            final String x = "x";
            BPLVariable xVar = new BPLVariable(x, new BPLTypeName("alpha"));
            final String y = "y";
            BPLVariable yVar = new BPLVariable(y, new BPLTypeName("alpha"));
            final String dynType = DYN_TYPE_FIELD;


            // axiomatization
            addType(NAME_TYPE);
            addType(REAL_TYPE);
            addType(ARRAY_TYPE, "alpha");

            addType(REF_TYPE);
            addConstants(new BPLVariable("null", new BPLTypeName(REF_TYPE)));

            addType(FIELD_TYPE, "_");

            addDeclaration(new BPLTypeAlias(HEAP_TYPE, new BPLParameterizedType(new BPLArrayType(new BPLType[]{new BPLTypeName("Ref"), new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha"))}, new BPLTypeName("alpha")), new BPLTypeName("alpha"))));

            addDeclaration(new BPLVariableDeclaration(new BPLVariable(heap1, new BPLTypeName(HEAP_TYPE), wellformedHeap(var(heap1)))));
            addDeclaration(new BPLVariableDeclaration(new BPLVariable(heap2, new BPLTypeName(HEAP_TYPE), wellformedHeap(var(heap2)))));
            addDeclaration(new BPLVariableDeclaration(new BPLVariable(related, new BPLTypeName(BIJ_TYPE), wellformedCoupling(var(heap1), var(heap2), var(related)))));

            
            addComment("Modified heap, coupling, relation (not original SscBoogie)");
            addFunction(WELLFORMED_HEAP_FUNC, new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heapVar,
                    isEquiv(wellformedHeap(var(heap)),
                            logicalAnd(
                                    new BPLArrayExpression(var(heap), nullLiteral(), var(alloc)),
                                    forall(refVar, fieldRefVar, implies(new BPLArrayExpression(var(heap), var(r), var(alloc)), new BPLArrayExpression(var(heap), new BPLArrayExpression(var(heap), var(r), var(f)), var(alloc)))),
                                    forall(refVar, fieldRefVar, implies(logicalNot(obj(var(heap), var(r))), isEqual(new BPLArrayExpression(var(heap), var(r), var(f)), nullLiteral()))),
                                    forall(refVar, fieldIntVar, implies(logicalNot(new BPLArrayExpression(var(heap), var(r), var(alloc))), isEqual(new BPLArrayExpression(var(heap), var(r), var(f)), intLiteral(0)))),
                                    forall(refVar, fieldRefVar, tVar, implies(isEqual(fieldType(var(f)), var(t)), isOfType(new BPLArrayExpression(var(heap), var(r), var(f)), var(heap), var(t)))),
                                    forall(refVar, fieldIntVar, tVar, implies(isEqual(fieldType(var(f)), var(t)), isInRange(new BPLArrayExpression(var(heap), var(r), var(f)), var(t))))
                                    ))
                    ));

            addFunction(WELLFORMED_COUPLING_FUNC, new BPLTypeName(HEAP_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(BIJ_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heap1Var, heap2Var, relatedVar,
                    isEquiv(wellformedCoupling(var(heap1), var(heap2), var(related)),
                            logicalAnd(
                                    bijective(var(related)),
                                    objectCoupling(var(heap1), var(heap2), var(related)),
                                    forall(r1Var, r2Var, implies(new BPLArrayExpression(var(related), var(r1), var(r2)), logicalAnd(new BPLArrayExpression(var(heap1),  var(r1), var(exposed)), new BPLArrayExpression(var(heap2), var(r2), var(exposed))))),
                                    forall(r1Var, implies(logicalAnd(obj(var(heap1), var(r1)), new BPLArrayExpression(var(heap1), var(r1), var(exposed))), exists(r2Var, new BPLArrayExpression(var(related), var(r1), var(r2))))),
                                    forall(r2Var, implies(logicalAnd(obj(var(heap2), var(r2)), new BPLArrayExpression(var(heap2), var(r2), var(exposed))), exists(r1Var, new BPLArrayExpression(var(related), var(r1), var(r2)))))
                                    ))
                    ));

            addFunction(OBJECT_COUPLING_FUNC, new BPLTypeName(HEAP_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(BIJ_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heap1Var, heap2Var, relatedVar,
                    isEquiv(objectCoupling(var(heap1), var(heap2), var(related)),
                            forall(r1Var, r2Var, implies(new BPLArrayExpression(var(related), var(r1), var(r2)), logicalAnd(obj(var(heap1), var(r1)), obj(var(heap2), var(r2))))))
                    ));


            addFunction(BIJECTIVE_FUNC, new BPLTypeName(BIJ_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    relatedVar, r1Var, r2Var, r3Var, r4Var,
                    isEquiv(CodeGenerator.bijective(var(related)),
                            implies(logicalAnd(new BPLArrayExpression(var(related), var(r1) ,var(r2)),
                                    new BPLArrayExpression(var(related), var(r3) ,var(r4))),
                                    isEquiv(isEqual(var(r1), var(r3)), isEqual(var(r2), var(r4)))))
                    ));

            addConstants(new BPLVariable(alloc, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.BOOL)));
            addConstants(new BPLVariable(exposed, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.BOOL)));
            addConstants(new BPLVariable(createdByCtxt, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.BOOL)));
            addConstants(new BPLVariable(dynType, new BPLTypeName(FIELD_TYPE, new BPLTypeName(NAME_TYPE))));
            
            addComment("end custom part (below: original SscBoogie)");
            
            
            addComment("mapping from class names to their representatives for static access");
            addFunction(CLASS_REPR_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(REF_TYPE));
            addFunction(CLASS_REPR_INV_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(NAME_TYPE));
            addAxiom(forall(cVar, isEqual(classReprInv(classRepr(var(c))), var(c)), new BPLTrigger(classRepr(var(c)))));
            addAxiom(forall(
                    tVar, heapVar,
                    logicalNot(isSubtype(typ(classRepr(var(t)), var(heap)), var(GLOBAL_VAR_PREFIX+"java.lang.Object")))
                    ));
            addAxiom(forall(tVar, notEqual(classRepr(var(t)), var("null"))));

            addFunction(IS_STATIC_FIELD_FUNC+"<alpha>", new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha")), BPLBuiltInType.BOOL);
            addAxiom(logicalNot(isStaticField(var(alloc))));
            addAxiom(logicalNot(isStaticField(var(exposed))));
            addAxiom(logicalNot(isStaticField(var(createdByCtxt))));
            addAxiom(logicalNot(isStaticField(var(dynType))));



            addComment("Encode type information");
            addFunction(TYP_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(NAME_TYPE));
            addAxiom(forall(heapVar, refVar, isEqual(typ(var(r), var(heap)), new BPLArrayExpression(var(heap), var(r), var(dynType)))));

            addFunction(BASE_CLASS_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE));
            addAxiom(forall(tVar, 
                    logicalAnd(isSubtype(var(t), baseClass(var(t))), implies(logicalAnd(logicalNot(isValueType(var(t))), notEqual(var(t), var(GLOBAL_VAR_PREFIX+"java.lang.Object"))), notEqual(var(t), baseClass(var(t))))),
                    new BPLTrigger(baseClass(var(t)))));

            addFunction(AS_DIRECT_SUB_CLASS_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE));
            addFunction(ONE_CLASS_DOWN_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE));
            addAxiom(forall(
                    aVar, bVar, cVar,
                    implies(isSubtype(var(c), asDirectSubClass(var(b), var(a))), isEqual(oneClassDown(var(c), var(a)), var(b))),
                    new BPLTrigger(isSubtype(var(c), asDirectSubClass(var(b), var(a))))
                    ));

            addFunction(IS_VALUE_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    tVar,
                    implies(isValueType(var(t)), logicalAnd(
                            forall(uVar, implies(isSubtype(var(t), var(u)), isEqual(var(t), var(u)))),
                            forall(uVar, implies(isSubtype(var(u), var(t)), isEqual(var(t), var(u))))
                            ))
                    ));

//            addConstants(new BPLVariable(GLOBAL_VAR_PREFIX+"java.lang.Object", new BPLTypeName(NAME_TYPE)));
            addAxiom(forall(
                    tVar, 
                    implies(isSubtype(var(t), var(GLOBAL_VAR_PREFIX+"java.lang.Object")), logicalNot(isValueType(var(t))))
                    ));


            addFunction(IS_OF_TYPE_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    oVar, heapVar, tVar,
                    //TODO use isSubtype or use refOfType here
//                    isEquiv(isOfType(var(o), var(heap), var(t)), logicalOr(isNull(var(o)), refOfType(var(o), var(heap), var(t)))),
                    isEquiv(isOfType(var(o), var(heap), var(t)), logicalOr(isNull(var(o)), isSubtype(typ(var(o), var(heap)), var(t)))),
                    new BPLTrigger(isOfType(var(o), var(heap), var(t)))
                    ));

            addFunction(IS_INSTANCE_OF_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    oVar, heapVar, tVar,
                    isEquiv(isInstanceOf(var(o), var(heap), var(t)), logicalAnd(nonNull(var(o)), isOfType(var(o), var(heap), var(t)))),
                    new BPLTrigger(isInstanceOf(var(o), var(heap), var(t)))
                    ));

            addFunction(IS_ALLOCATED_FUNC+"<alpha>", new BPLTypeName(HEAP_TYPE), new BPLTypeName("alpha"), BPLBuiltInType.BOOL);
            addAxiom(forall(new BPLType[]{new BPLTypeName("alpha")},
                    new BPLVariable[]{heapVar, oVar, new BPLVariable(f, new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha")))},
                    implies(logicalAnd(wellformedHeap(var(heap)), new BPLArrayExpression(var(heap), var(o), var(alloc))), isAllocated(var(heap), new BPLArrayExpression(var(heap), var(o), var(f)))),
                    new BPLTrigger(isAllocated(var(heap), new BPLArrayExpression(var(heap), var(o), var(f))))
                    ));
            addAxiom(forall(
                    heapVar, oVar, fieldRefVar,
                    implies(logicalAnd(wellformedHeap(var(heap)), new BPLArrayExpression(var(heap), var(o), var(alloc))), isAllocated(var(heap), new BPLArrayExpression(var(heap), var(o), var(f)))),
                    new BPLTrigger(new BPLArrayExpression(var(heap), new BPLArrayExpression(var(heap), var(o), var(f)), var(alloc)))
                    ));
            addAxiom(forall(
                    heapVar, oVar,
                    implies(isAllocated(var(heap), var(o)), new BPLArrayExpression(var(heap), var(o), var(alloc))),
                    new BPLTrigger(new BPLArrayExpression(var(heap), var(o), var(alloc)))
                    ));
            addAxiom(forall(
                    heapVar, cVar,
                    implies(wellformedHeap(var(heap)), new BPLArrayExpression(var(heap), classRepr(var(c)), var(alloc))),
                    new BPLTrigger(new BPLArrayExpression(var(heap), classRepr(var(c)), var(alloc)))
                    ));

            addFunction(IS_MEMBERLESS_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    oVar, heapVar,
                    logicalNot(isMemberlessType(typ(var(o), var(heap)))),
                    new BPLTrigger(isMemberlessType(typ(var(o), var(heap))))
                    ));

            // primitive types
            for (JBaseType valueType : valueTypes) {
                addConstants(new BPLVariable(
                        getValueTypeName(valueType),
                        new BPLTypeName(NAME_TYPE)));
            }

            BPLExpression[] vtExprs = new BPLExpression[valueTypes.length];
            for (int j = 0; j < valueTypes.length; j++) {
                vtExprs[j] = isEqual(var(t), typeRef(valueTypes[j]));
            }
            addComment("Defines the set of value types.");
            addAxiom(forall(tVar, isEquiv(isValueType(var(t)), logicalOr(vtExprs)), trigger(isValueType(var(t)))));

            addFunction(IS_IN_RANGE_FUNC, BPLBuiltInType.INT, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.BYTE)), logicalAnd(lessEqual(intLiteral(-128), var(i)), lessEqual(var(i), intLiteral(127))))));
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.SHORT)), logicalAnd(lessEqual(intLiteral(-32768), var(i)), lessEqual(var(i), intLiteral(32767))))));
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.CHAR)), logicalAnd(lessEqual(intLiteral(0), var(i)), lessEqual(var(i), intLiteral(65535))))));
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.INT)), logicalAnd(lessEqual(intLiteral(-2147483648), var(i)), lessEqual(var(i), intLiteral(2147483647))))));
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.LONG)), logicalAnd(lessEqual(intLiteral(-9223372036854775808l), var(i)), lessEqual(var(i), intLiteral(9223372036854775807l))))));
            addAxiom(forall(iVar, isEquiv(isInRange(var(i), typeRef(JBaseType.BOOLEAN)), logicalAnd(lessEqual(intLiteral(0), var(i)), lessEqual(var(i), intLiteral(1))))));

            addComment("Type conversions and sizes");
            addFunction(INT_TO_INT_FUNC, BPLBuiltInType.INT, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.INT);
            addFunction(INT_TO_REAL_FUNC, BPLBuiltInType.INT, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(REAL_TO_INT_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.INT);
            addFunction(REAL_TO_REAL_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(REAL_TYPE));

            addAxiom(forall(
                    iVar, bVar, cVar,
                    implies(isInRange(var(i), var(c)), isEqual(intToInt(var(i), var(b), var(c)), var(i)))
                    ));


            addFunction(IF_THEN_ELSE_FUNC+"<alpha>", BPLBuiltInType.BOOL, new BPLTypeName("alpha"), new BPLTypeName("alpha"), new BPLTypeName("alpha"));
            addAxiom(forall(new BPLType[]{new BPLTypeName("alpha")},
                    new BPLVariable[]{boolVar, xVar, yVar},
                    implies(var(b), isEqual(ifThenElse(var(b), var(x), var(y)), var(x))),
                    new BPLTrigger(ifThenElse(var(b), var(x), var(y)))
                    ));
            addAxiom(forall(new BPLType[]{new BPLTypeName("alpha")},
                    new BPLVariable[]{boolVar, xVar, yVar},
                    implies(logicalNot(var(b)), isEqual(ifThenElse(var(b), var(x), var(y)), var(y))),
                    new BPLTrigger(ifThenElse(var(b), var(x), var(y)))
                    ));
        }

        {
            addComment("Bit-level operators");

            addFunction(NEG_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT);
            addFunction(AND_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT, BPLBuiltInType.INT);
            addFunction(OR_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT, BPLBuiltInType.INT);
            addFunction(XOR_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT, BPLBuiltInType.INT);
            addFunction(SHL_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT, BPLBuiltInType.INT);
            addFunction(SHR_FUNC, BPLBuiltInType.INT, BPLBuiltInType.INT, BPLBuiltInType.INT);

            addFunction(RNEG_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(RADD_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(RSUB_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(RMUL_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(RDIV_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));
            addFunction(RMOD_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE));

            addFunction(RLESS_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);
            addFunction(RLESS_OR_EQUAL_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);
            addFunction(REQ_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);
            addFunction(RNEQ_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);
            addFunction(RGREATER_OR_EQUAL_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);
            addFunction(RGREATER_FUNC, new BPLTypeName(REAL_TYPE), new BPLTypeName(REAL_TYPE), BPLBuiltInType.BOOL);


            addComment("Properties of operators");

            String i = quantVarName("i");
            String j = quantVarName("j");
            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
            BPLVariable jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // i % j == i - i / j * j
            addAxiom(forall(
                    iVar, jVar,
                    isEqual(
                            modulo(var(i), var(j)),
                            sub(var(i), multiply(divide(var(i), var(j)), var(j)))
                            ),
                            trigger(modulo(var(i), var(j)), divide(var(i), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // 0 <= i && 0 < j ==> 0 <= i % j && i % j < j
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(intLiteral(0), var(i)),
                                    less(intLiteral(0), var(j))
                                    ),
                                    logicalAnd(
                                            lessEqual(intLiteral(0), modulo(var(i), var(j))),
                                            less(modulo(var(i), var(j)), var(j)))
                            ),
                            trigger(modulo(var(i), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // 0 <= i && j < 0 ==> 0 <= i % j && i % j < 0 - j
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(intLiteral(0), var(i)),
                                    less(var(j), intLiteral(0))
                                    ),
                                    logicalAnd(
                                            lessEqual(intLiteral(0), modulo(var(i), var(j))),
                                            less(modulo(var(i), var(j)), sub(intLiteral(0), var(j))))
                            ),
                            trigger(modulo(var(i), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // i <= 0 && 0 < j ==> 0 - j < i % j && i % j <= 0
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(var(i), intLiteral(0)),
                                    less(intLiteral(0), var(j))
                                    ),
                                    logicalAnd(
                                            less(sub(intLiteral(0), var(j)), modulo(var(i), var(j))),
                                            lessEqual(modulo(var(i), var(j)), intLiteral(0)))
                            ),
                            trigger(modulo(var(i), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // i <= 0 && j < 0 ==> j < i % j && i % j <= 0
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(var(i), intLiteral(0)),
                                    less(var(j), intLiteral(0))
                                    ),
                                    logicalAnd(
                                            less(var(j), modulo(var(i), var(j))),
                                            lessEqual(modulo(var(i), var(j)), intLiteral(0)))
                            ),
                            trigger(modulo(var(i), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // 0 <= i && 0 < j ==> (i + j) % j == i % j
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(intLiteral(0), var(i)),
                                    less(intLiteral(0), var(j))
                                    ),
                                    isEqual(
                                            modulo(add(var(i), var(j)), var(j)),
                                            modulo(var(i), var(j)))
                            ),
                            trigger(modulo(add(var(i), var(j)), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // 0 <= i && 0 < j ==> (j + i) % j == i % j
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(intLiteral(0), var(i)),
                                    less(intLiteral(0), var(j))
                                    ),
                                    isEqual(
                                            modulo(add(var(j), var(i)), var(j)),
                                            modulo(var(i), var(j)))
                            ),
                            trigger(modulo(add(var(j), var(i)), var(j)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // 0 <= i - j && 0 < j ==> (i - j) % j == i % j
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(
                                    lessEqual(intLiteral(0), sub(var(i), var(j))),
                                    less(intLiteral(0), var(j))
                                    ),
                                    isEqual(
                                            modulo(sub(var(i), var(j)), var(j)),
                                            modulo(var(i), var(j))
                                            )
                            ),
                            trigger(modulo(sub(var(i), var(j)), var(j)))
                    ));

            String a = quantVarName("a");
            String b = quantVarName("b");
            String d = quantVarName("d");
            BPLVariable aVar = new BPLVariable(a, BPLBuiltInType.INT);
            BPLVariable bVar = new BPLVariable(b, BPLBuiltInType.INT);
            BPLVariable dVar = new BPLVariable(d, BPLBuiltInType.INT);
            // 2 <= d && a % d == b % d && a < b ==> a + d <= b
            addAxiom(forall(
                    aVar, bVar, dVar,
                    implies(
                            logicalAnd(
                                    logicalAnd(
                                            lessEqual(intLiteral(2), var(d)),
                                            isEqual(modulo(var(a), var(d)), modulo(var(b), var(d)))),
                                            less(var(a), var(b))),
                                            lessEqual(add(var(a), var(d)), var(b))),
                                            trigger(modulo(var(a), var(d)), modulo(var(b), var(d)))
                    ));
            
            
            //TODO distribution of * and +/-

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall x: int, y: int :: { #and(x,y) }  #and(x,y) == #and(y,x));
            addAxiom(forall(
                    iVar, jVar,
                    isEqual(bitAnd(var(i), var(j)), bitAnd(var(j), var(i))),
                    trigger(bitAnd(var(i), var(j)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall x: int, y: int :: { #or(x,y) }  #or(x,y) == #or(y,x));
            addAxiom(forall(
                    iVar, jVar,
                    isEqual(bitOr(var(i), var(j)), bitOr(var(j), var(i))),
                    trigger(bitOr(var(i), var(j)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall x: int, y: int :: { #and(x,y) }  0 <= x || 0 <= y  ==>  0 <= #and(x,y));
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalOr(lessEqual(intLiteral(0), var(i)), lessEqual(intLiteral(0), var(j))),
                            lessEqual(intLiteral(0), bitAnd(var(i), var(j)))),
                            trigger(bitAnd(var(i), var(j)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall x: int, y: int :: { #or(x,y) }  0 <= x && 0 <= y  ==>  0 <= #or(x,y) && #or(x,y) <= x + y);
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(lessEqual(intLiteral(0), var(i)), lessEqual(intLiteral(0), var(j))),
                            logicalAnd(lessEqual(intLiteral(0), bitOr(var(i), var(j))), lessEqual(bitOr(var(i), var(j)), add(var(i), var(j))))),
                            trigger(bitOr(var(i), var(j)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            // axiom (forall x: int :: { #and(x,-1) }  #and(x,-1) == x);
            addAxiom(forall(
                    iVar,
                    isEqual(bitAnd(var(i), intLiteral(-1)), var(i)),
                            trigger(bitAnd(var(i), intLiteral(-1)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            // axiom (forall x: int :: { #and(x,0) }  #and(x,0) == 0);
            addAxiom(forall(
                    iVar,
                    isEqual(bitAnd(var(i), intLiteral(0)), intLiteral(0)),
                            trigger(bitAnd(var(i), intLiteral(0)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            // axiom (forall x: int :: { #or(x,-1) }  #or(x,-1) == -1);
            addAxiom(forall(
                    iVar,
                    isEqual(bitOr(var(i), intLiteral(-1)), intLiteral(-1)),
                            trigger(bitOr(var(i), intLiteral(-1)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            // axiom (forall x: int :: { #or(x,0) }  #or(x,0) == x);
            addAxiom(forall(
                    iVar,
                    isEqual(bitOr(var(i), intLiteral(0)), var(i)),
                            trigger(bitOr(var(i), intLiteral(0)))
                    ));
            
            
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            // axiom (forall i:int :: {#shl(i,0)} #shl(i,0) == i);
            addAxiom(forall(
                    iVar,
                    isEqual(bitShl(var(i), intLiteral(0)), var(i)),
                            trigger(bitShl(var(i), intLiteral(0)))
                    ));

            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall i:int, j:int :: {#shl(i,j)}  1 <= j ==> #shl(i,j) == #shl(i,j-1) * 2);
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            lessEqual(intLiteral(1), var(j)),
                            isEqual(bitShl(var(i), var(j)), multiply(bitShl(var(i), sub(var(j), intLiteral(1))), intLiteral(2)))),
                            trigger(bitShl(var(i), var(j)))
                    ));
            
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            // axiom (forall i:int, j:int :: {#shl(i,j)} 0 <= i && i < 32768 && 0 <= j && j <= 16  ==>  0 <= #shl(i, j) && #shl(i, j) <= 2147483647);
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            logicalAnd(lessEqual(intLiteral(0), var(i)), less(var(i), intLiteral(32768)), lessEqual(var(j), intLiteral(16))),
                            logicalAnd(lessEqual(intLiteral(0), bitShl(var(i), var(j))), lessEqual(bitShl(var(i), var(j)), intLiteral(2147483647)))),
                            trigger(bitShl(var(i), var(j)))
                    ));
            
            // axiom (forall i:int :: {#shr(i,0)} #shr(i,0) == i);
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            addAxiom(forall(
                    iVar,
                    isEqual(bitShr(var(i), intLiteral(0)), var(i)),
                            trigger(bitShr(var(i), intLiteral(0)))
                    ));

            // axiom (forall i:int, j:int :: {#shr(i,j)} 1 <= j ==> #shr(i,j) == #shr(i,j-1) / 2);
            iVar = new BPLVariable(i, BPLBuiltInType.INT);
            jVar = new BPLVariable(j, BPLBuiltInType.INT);
            addAxiom(forall(
                    iVar, jVar,
                    implies(
                            lessEqual(intLiteral(1), var(j)),
                            isEqual(bitShr(var(i), var(j)), divide(bitShr(var(i), sub(var(j), intLiteral(1))), intLiteral(2)))),
                            trigger(bitShr(var(i), var(j)))
                    ));
            
            //TODO min and max functions
            
        }
        // END PRELUDE SscBoogie


        {
            final String heap1 = TranslationController.HEAP1;
            final String heap2 = TranslationController.HEAP2;
            final String related = RELATED_RELATION;
            final String alloc = ALLOC_FIELD;
            final String exposed = EXPOSED_FIELD;
            final String createdByCtxt = CREATED_BY_CTXT_FIELD;
            final String r = "r";
            BPLVariable refVar = new BPLVariable(r, new BPLTypeName(REF_TYPE));
            final String heap = "heap";
            BPLVariable heapVar = new BPLVariable(heap, new BPLTypeName(HEAP_TYPE));
            final String f = "f";
            BPLVariable fieldRefVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)));
            BPLVariable fieldIntVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.INT));
            BPLVariable fieldBoolVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, BPLBuiltInType.BOOL));
            BPLVariable fieldAlphaVar = new BPLVariable(f, new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha")));
            String vAlpha = "v";
            BPLVariable vAlphaVar = new BPLVariable(vAlpha, new BPLTypeName("alpha"));
            BPLVariable heap1Var = new BPLVariable(heap1, new BPLTypeName(HEAP_TYPE));
            BPLVariable heap2Var = new BPLVariable(heap2, new BPLTypeName(HEAP_TYPE));
            BPLVariable relatedVar = new BPLVariable(related, new BPLTypeName(BIJ_TYPE));
            final String r1 = "r1";
            BPLVariable r1Var = new BPLVariable(r1, new BPLTypeName(REF_TYPE));
            final String r2 = "r2";
            BPLVariable r2Var = new BPLVariable(r2, new BPLTypeName(REF_TYPE));
            final String r3 = "r3";
            BPLVariable r3Var = new BPLVariable(r3, new BPLTypeName(REF_TYPE));
            final String r4 = "r4";
            BPLVariable r4Var = new BPLVariable(r4, new BPLTypeName(REF_TYPE));
            final String a = "a";
            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(NAME_TYPE));
            final String b = "b";
            BPLVariable bVar = new BPLVariable(b, new BPLTypeName(NAME_TYPE));
            final String c = "c";
            BPLVariable cVar = new BPLVariable(c, new BPLTypeName(NAME_TYPE));
            final String o = "o";
            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
            final String t = "t";
            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
            final String u = "u";
            BPLVariable uVar = new BPLVariable(u, new BPLTypeName(NAME_TYPE));
            final String sp1 = TranslationController.SP1;
            final String sp2 = TranslationController.SP2;
            final String stack1 = TranslationController.STACK1;
            final String stack2 = TranslationController.STACK2;
            final String thisName = "this";
            final String stack = "stack";
            BPLVariable stackVar = new BPLVariable(stack, new BPLTypeName(STACK_TYPE));
            final String sp = "sp";
            BPLVariable spVar = new BPLVariable(sp, new BPLTypeName(STACK_PTR_TYPE));
            final String p = "p";
            BPLVariable pVar = new BPLVariable(p, new BPLTypeName(STACK_PTR_TYPE));
            final String v = "v";
            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VAR_TYPE, new BPLTypeName(REF_TYPE)));
            final String i = "i";
            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
            final String bool = "b";
            BPLVariable boolVar = new BPLVariable(bool, BPLBuiltInType.BOOL);
            final String x = "x";
            BPLVariable xVar = new BPLVariable(x, new BPLTypeName("alpha"));
            final String y = "y";
            BPLVariable yVar = new BPLVariable(y, new BPLTypeName("alpha"));
            final String m = "m";
            BPLVariable mVar = new BPLVariable(m, new BPLTypeName(METHOD_TYPE));
            final String c1 = "c1";
            BPLVariable c1Var = new BPLVariable(c1, new BPLTypeName(NAME_TYPE));
            final String c2 = "c2";
            BPLVariable c2Var = new BPLVariable(c2, new BPLTypeName(NAME_TYPE));
            final String c3 = "c3";
            BPLVariable c3Var = new BPLVariable(c3, new BPLTypeName(NAME_TYPE));
            final String dynType = DYN_TYPE_FIELD;
            
            
            addAxiom(forall(tVar, uVar, oVar, heapVar,
                    implies(
                            logicalAnd(
                                    isClassType(var(t)),
                                    isClassType(var(u)),
                                    logicalNot(isSubtype(var(t), var(u))),
                                    logicalNot(isSubtype(var(u), var(t))),
                                    nonNull(var(o)),
                                    isOfType(var(o), var(heap), var(t))
                                    ),
                                    logicalNot(isOfType(var(o), var(heap), var(u)))
                            ),
                    new BPLTrigger(isClassType(var(t)), isClassType(var(u)), isOfType(var(o), var(heap), var(t)))
                    ));
            
//            addAxiom(forall(tVar, oVar, heapVar,
//                    implies(
//                            refOfType(var(o), var(heap), var(t)), 
//                            isOfType(var(o), var(heap), var(t)))
//                    ));
            
            addAxiom(forall(new BPLType[]{new BPLTypeName("alpha")}, new BPLVariable[]{oVar, heapVar, tVar, fieldAlphaVar, vAlphaVar},
                    isEquiv(isOfType(var(o), var(heap), var(t)), isOfType(var(o), new BPLArrayExpression(var(heap), new BPLArrayAssignment(new BPLExpression[]{var(o), var(f)}, var(v))), var(t)))
                    ));
            
            addFunction(IS_CLASS_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            
            addDeclaration(new BPLTypeAlias(BIJ_TYPE, new BPLArrayType(new BPLTypeName(REF_TYPE), new BPLTypeName(REF_TYPE), BPLBuiltInType.BOOL)));


            //            final String updateProcedureName = "Update";
            //            final BPLVariable[] updateInParams = new BPLVariable[]{r1Var, r2Var};
            //            final BPLVariable[] updateOutParams = BPLVariable.EMPTY_ARRAY;
            //            addDeclaration(new BPLProcedure(updateProcedureName, updateInParams, updateOutParams, new BPLSpecification(
            //                    new BPLModifiesClause(var(heap1), var(heap2), var(related))
            //                    ), new BPLImplementation(updateProcedureName, updateInParams, updateOutParams, new BPLImplementationBody(
            //                            new BPLVariableDeclaration[]{
            //                                     
            //                            }, new BPLBasicBlock[]{
            //                                    
            //                            }
            //                            ))));

            addFunction(REL_NULL_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(REF_TYPE), new BPLTypeName(BIJ_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    r1Var, r2Var, relatedVar,
                    isEquiv(relNull(var(r1), var(r2), var(related)),
                    logicalOr(
                            logicalAnd(
                                    isNull(var(r1)),
                                    isNull(var(r2))
                                    )
                                    ,
                            logicalAnd(
                                    nonNull(var(r1)),
                                    nonNull(var(r2)),
                                    new BPLArrayExpression(var(related), var(r1), var(r2))
                                    )
                            ))));

            addFunction(NON_NULL_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)), new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    cVar, fieldRefVar, heapVar,
                    isEquiv(nonNull(var(c), var(f), var(heap)), 
                            forall(refVar, implies(
                                    logicalAnd(obj(var(heap), var(r)), refOfType(var(r), var(heap), var(c))),
                                    notEqual(new BPLArrayExpression(var(heap), var(r), var(f)), nullLiteral())
                                    ))
                            )));

            addFunction(INTERNAL_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)), new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    cVar, fieldRefVar, heapVar,
                    isEquiv(internal(var(c), var(f), var(heap)), 
                            forall(refVar, implies(
                                    logicalAnd(obj(var(heap), var(r)), refOfType(var(r), var(heap), var(c))),
                                    logicalAnd(
                                            logicalNot(new BPLArrayExpression(var(heap), new BPLArrayExpression(var(heap), var(r), var(f)), var(createdByCtxt))),
                                            logicalNot(new BPLArrayExpression(var(heap), new BPLArrayExpression(var(heap), var(r), var(f)), var(exposed)))
                                    )
                                    ))
                            )));

            addFunction(REF_OF_TYPE_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    oVar, heapVar, tVar,
                    isEquiv(refOfType(var(o), var(heap), var(t)),
                            implies(
                                    obj(var(heap), var(o)),
                                    isSubtype(new BPLArrayExpression(var(heap), var(o), var(dynType)), var(t))
                                    )
                            )
                    ));


            
            addFunction(OBJ_FUNC, new BPLTypeName(HEAP_TYPE), new BPLTypeName(REF_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heapVar, refVar,
                    isEquiv(obj(var(heap), var(r)), 
                            logicalAnd(
                                    nonNull(var(r)),
                                    new BPLArrayExpression(var(heap), var(r), var(ALLOC_FIELD)),
                                    logicalOr(
                                            new BPLArrayExpression(var(heap), var(r), var(EXPOSED_FIELD)),
                                            logicalNot(new BPLArrayExpression(var(heap), var(r), var(CREATED_BY_CTXT_FIELD)))
                                    )
                            )
                    )));
            
            addComment("Syntactic sugar for writing coupling invariant");
            addFunction(OBJ_OF_TYPE_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heapVar, refVar, tVar,
                    isEquiv(new BPLFunctionApplication(OBJ_OF_TYPE_FUNC, var(r), var(t), var(heap)), 
                            logicalAnd(
                            		obj(var(heap), var(r)),
                            		refOfType(var(r), var(heap), var(t))
                            )
                    ))); 


            addType(METHOD_TYPE);
            addFunction(FTYPE_FUNC, new BPLType[]{new BPLTypeName(HEAP_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)), new BPLTypeName(NAME_TYPE)}, BPLBuiltInType.BOOL);
            addAxiom(forall(
                    heapVar, cVar, fieldRefVar, tVar,
                    isEquiv(ftype(var(heap), var(c), var(f), var(t)),
                    logicalAnd(
                            forall(oVar, implies(
                                    logicalAnd(
                                            obj(var(heap), var(o)),
                                            isEqual(new BPLArrayExpression(var(heap), var(o), var(dynType)), var(c)) ),
                                            refOfType( 
                                                    new BPLArrayExpression(var(heap), var(o), var(f)),
                                                    var(heap),
                                                    var(t)
                                                    )
                                    ))
                                    ,
                                    forall(oVar, implies(
                                            logicalAnd(
                                                    obj(var(heap), var(o)),
                                                    notEqual(new BPLArrayExpression(var(heap), var(o), var(dynType)), var(c)) ),
                                                    isEqual(new BPLArrayExpression(var(heap), var(o), var(f)), var("null"))
                                            ))
                            ))));

            addFunction(UNIQUE_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(FIELD_TYPE, new BPLTypeName(REF_TYPE)), new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    cVar, fieldRefVar, heapVar,
                    isEquiv(unique(var(c), var(f), var(heap)), 
                            forall(r1Var, r2Var, 
                                    implies(
                                            logicalAnd(
                                                    obj(var(heap), var(r1)),
                                                    obj(var(heap), var(r2)),
                                                    refOfType(var(r1), var(heap), var(c)),
                                                    refOfType(var(r2), var(heap), var(c)),
                                                    notEqual(var(r1), var(r2))
                                                    ),
                                                    notEqual(new BPLArrayExpression(var(heap), var(r1), var(f)), new BPLArrayExpression(var(heap), var(r2), var(f)))
                                            )
                                    )
                            )
                    ));

            if (tc.getConfig().extensionalityEnabled()) { 
            	addComment("Extensionality for simulations");
            	addAxiom(forall(
            			r1Var, r2Var, relatedVar,
            			isEqual(new BPLArrayExpression(var(related), new BPLArrayAssignment(new BPLVariableExpression[]{var(r1), var(r2)}, new BPLArrayExpression(var(related), var(r1), var(r2)))), var(related))
            			));

            	addComment("Extensionality for heaps");
            	addAxiom(forall(new BPLType[]{new BPLTypeName("alpha")},
            			new BPLVariable[]{refVar, fieldAlphaVar, heapVar},
            			isEqual(new BPLArrayExpression(var(heap), new BPLArrayAssignment(new BPLVariableExpression[]{var(r), var(f)}, new BPLArrayExpression(var(heap), var(r), var(f)))), var(heap))
            			));
            }

            addType(VAR_TYPE, "_");
            addDeclaration(new BPLTypeAlias(STACK_PTR_TYPE, BPLBuiltInType.INT));
            addDeclaration(new BPLVariableDeclaration(new BPLVariable(sp1, new BPLTypeName(STACK_PTR_TYPE))));

            addDeclaration(new BPLVariableDeclaration(new BPLVariable(sp2, new BPLTypeName(STACK_PTR_TYPE))));

            addDeclaration(new BPLTypeAlias(STACK_FRAME_TYPE, new BPLParameterizedType(new BPLArrayType(new BPLTypeName(VAR_TYPE, new BPLTypeName("alpha")), new BPLTypeName("alpha")), new BPLTypeName("alpha"))));
            addDeclaration(new BPLTypeAlias(STACK_TYPE, new BPLArrayType(new BPLTypeName(STACK_PTR_TYPE), new BPLTypeName(STACK_FRAME_TYPE))));

            addDeclaration(new BPLVariableDeclaration(new BPLVariable(stack1, new BPLTypeName(STACK_TYPE), wellformedStack(var(stack1), var(sp1), var(heap1)))));
            addDeclaration(new BPLVariableDeclaration(new BPLVariable(stack2, new BPLTypeName(STACK_TYPE), wellformedStack(var(stack2), var(sp2), var(heap2)))));

            addFunction(WELLFORMED_STACK_FUNC, new BPLTypeName(STACK_TYPE), new BPLTypeName(STACK_PTR_TYPE), new BPLTypeName(HEAP_TYPE), BPLBuiltInType.BOOL);
            addAxiom(forall(
                    stackVar, spVar, heapVar,
                    isEquiv(wellformedStack(var(stack), var(sp), var(heap)),
                    logicalAnd(
                            forall(pVar, vVar, implies(logicalAnd(lessEqual(intLiteral(0), var(p)), lessEqual(var(p), var(sp))), new BPLArrayExpression(var(heap), new BPLArrayExpression(new BPLArrayExpression(var(stack), var(p)), var(v)), var(alloc)))),
                            forall(pVar, vVar, implies(logicalOr( less(var(p), intLiteral(0)), greater(var(p), var(sp)) ), isEqual(new BPLArrayExpression(new BPLArrayExpression(var(stack), var(p)), var(v)), nullLiteral()))),
                            forall(pVar, new BPLVariable(v, new BPLTypeName(VAR_TYPE, BPLBuiltInType.INT)), implies(logicalOr( less(var(p), intLiteral(0)), greater(var(p), var(sp)) ), isEqual(new BPLArrayExpression(new BPLArrayExpression(var(stack), var(p)), var(v)), intLiteral(0))))
                            )
                    )
                    ));

            if (tc.getConfig().extensionalityEnabled()) {
            	addComment("Extensionality for stacks");
            	addAxiom(forall(
            			spVar, stackVar,
            			isEqual(new BPLArrayExpression(var(stack), new BPLArrayAssignment(new BPLVariableExpression[]{var(sp)}, new BPLArrayExpression(var(stack), var(sp)))), var(stack))
            			));
            }

            addFunction(FIELD_TYPE_FUNC+"<alpha>", new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha")), new BPLTypeName(NAME_TYPE));
            
            addFunction(IS_PUBLIC_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addFunction(DEFINES_METHOD_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(METHOD_TYPE), BPLBuiltInType.BOOL);
            addFunction(IS_CALLABLE_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(METHOD_TYPE), BPLBuiltInType.BOOL);
            addFunction(HAS_RETURN_VALUE_FUNC, new BPLTypeName(METHOD_TYPE), BPLBuiltInType.BOOL);
            addComment("memberOf(m, t1, t2) <==> m is member of t2 (implementation is in t1)");
            addFunction(MEMBER_OF_FUNC, new BPLTypeName(METHOD_TYPE), new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addFunction(LIB_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            addFunction(CLASS_EXTENDS_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
            
            addAxiom(forall(
                    mVar, c1Var, c2Var,
                    isEquiv(memberOf(var(m), var(c1), var(c2)),
                            logicalOr(
                                    logicalAnd(isEqual(var(c1), var(c2)), definesMethod(var(c2), var(m))),
                                    logicalAnd(logicalNot(definesMethod(var(c2), var(m))), 
//                                            exists(c3Var, implies(isSubtype(var(c2), var(c3)), memberOf(var(m), var(c1), var(c3)))) //TODO do we have to use classExtends-function instead of isSubtype?
                                            forall(c3Var, implies(classExtends(var(c2), var(c3)), memberOf(var(m), var(c1), var(c3)))) 
                                    )
                            )
                    )
                    ));
            addAxiom(forall(
                    mVar, c1Var, c2Var,
                    implies(memberOf(var(m), var(c1), var(c2)),
                            definesMethod(var(c1), var(m))
                    )));
            

            addType(ADDRESS_TYPE);

            addConstants(new BPLVariable(PLACE_VARIABLE, new BPLTypeName(VAR_TYPE, new BPLTypeName(ADDRESS_TYPE))));
            addConstants(new BPLVariable(METH_FIELD, new BPLTypeName(VAR_TYPE, new BPLTypeName(METHOD_TYPE))));

            {
                // A helper function for converting int values to bool values.
                addFunction(INT2BOOL_FUNC, BPLBuiltInType.INT, BPLBuiltInType.BOOL);

                addAxiom(forall(
                        iVar,
                        isEquiv(
                                isEqual(new BPLFunctionApplication(INT2BOOL_FUNC, var(i)), BPLBoolLiteral.FALSE),
                                isEqual(var(i), intLiteral(0))
                                ),
                                trigger(new BPLFunctionApplication(INT2BOOL_FUNC, var(i)))
                        ));

                addAxiom(forall(
                        iVar,
                        isEquiv(
                                isEqual(new BPLFunctionApplication(INT2BOOL_FUNC, var(i)), BPLBoolLiteral.TRUE),
                                notEqual(var(i), intLiteral(0))
                                ),
                                trigger(new BPLFunctionApplication(INT2BOOL_FUNC, var(i)))
                        ));
            }
            
            addDeclaration(new BPLVariableDeclaration(new BPLVariable(ITranslationConstants.USE_HAVOC, new BPLArrayType(new BPLTypeName(ADDRESS_TYPE), BPLBuiltInType.BOOL))));
            
            addFunction(IS_STATIC_METHOD_FUNC, new BPLTypeName(METHOD_TYPE), BPLBuiltInType.BOOL);
            
            addFunction(VALID_HEAP_SUCC_FUNC, new BPLTypeName(HEAP_TYPE), new BPLTypeName(HEAP_TYPE), new BPLTypeName(STACK_TYPE), BPLBuiltInType.BOOL);

            String oldHeap = "oldHeap";
            BPLVariable oldHeapVar = new BPLVariable(oldHeap, new BPLTypeName(HEAP_TYPE));
            String newHeap = "newHeap";
            BPLVariable newHeapVar = new BPLVariable(newHeap, new BPLTypeName(HEAP_TYPE));
            addAxiom(forall(oldHeapVar, newHeapVar, stackVar,
                    isEquiv(validHeapSucc(var(oldHeap), var(newHeap), var(stack)),
                            forall(
                                    spVar, oVar,
                                    logicalAnd(
                                            implies(map(var(oldHeap), var(o), var(EXPOSED_FIELD)), map(var(newHeap), var(o), var(EXPOSED_FIELD))),
                                            isEqual(map(var(oldHeap), var(o), var(CREATED_BY_CTXT_FIELD)), map(var(newHeap), var(o), var(CREATED_BY_CTXT_FIELD))),
                                            implies(map(var(oldHeap), var(o), var(ALLOC_FIELD)), map(var(newHeap), var(o), var(ALLOC_FIELD))),
                                            isEqual(map(var(oldHeap), var(o), var(DYN_TYPE_FIELD)), map(var(newHeap), var(o), var(DYN_TYPE_FIELD)))
                                    )
                            )
                            )
                    ));
            
            
            // relation between classExtends and subtype
            addAxiom(forall(c1Var, c2Var,
                    implies(classExtends(var(c1), var(c2)), isSubtype(var(c1), var(c2)))
                    ));
            addAxiom(forall(c1Var, c2Var, 
                    implies(
                            logicalAnd(
                                    isClassType(var(c1)),
                                    classExtends(var(c1), var(c2))
                            ),
                            isClassType(var(c2))
                    )));
            addAxiom(forall(c1Var, c2Var,
                    implies(
                            logicalAnd(
                                    isSubtype(var(c1), var(c2)),
                                    isClassType(var(c1)),
                                    isClassType(var(c2))
                                    ),
                            logicalOr(
                                    isEqual(var(c1), var(c2)),
                                    classExtends(var(c1), var(c2)),
                                    exists(c3Var,
                                            logicalAnd(
                                                    isClassType(var(c3)),
                                                    classExtends(var(c1), var(c3)),
                                                    isSubtype(var(c3), var(c2))
                                                    )
                                            )
                                    )
                            )
                    ));
            
            flushPendingTheory(); //TODO this is needed at the moment to generate information about the long values (which should be printed into the program code directly)
        }
    }

    /**
     * Axiomatizes some properties of arithmetic operations in order to later
     * support the theorem prover in verifying programs containing arithmetic
     * expressions.
     */
    private void axiomatizeArithmetic() {
        //        String i = quantVarName("i");
        //        String j = quantVarName("j");
        //        BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        BPLVariable jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // i % j == i - i / j * j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                isEqual(
        //                        modulo(var(i), var(j)),
        //                        sub(var(i), multiply(divide(var(i), var(j)), var(j)))
        //                        ),
        //                        trigger(modulo(var(i), var(j)), divide(var(i), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // 0 <= i && 0 < j ==> 0 <= i % j && i % j < j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                less(intLiteral(0), var(j))
        //                                ),
        //                                logicalAnd(
        //                                        lessEqual(intLiteral(0), modulo(var(i), var(j))),
        //                                        less(modulo(var(i), var(j)), var(j)))
        //                        ),
        //                        trigger(modulo(var(i), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // 0 <= i && j < 0 ==> 0 <= i % j && i % j < 0 - j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                less(var(j), intLiteral(0))
        //                                ),
        //                                logicalAnd(
        //                                        lessEqual(intLiteral(0), modulo(var(i), var(j))),
        //                                        less(modulo(var(i), var(j)), sub(intLiteral(0), var(j))))
        //                        ),
        //                        trigger(modulo(var(i), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // i <= 0 && 0 < j ==> 0 - j < i % j && i % j <= 0
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(var(i), intLiteral(0)),
        //                                less(intLiteral(0), var(j))
        //                                ),
        //                                logicalAnd(
        //                                        less(sub(intLiteral(0), var(j)), modulo(var(i), var(j))),
        //                                        lessEqual(modulo(var(i), var(j)), intLiteral(0)))
        //                        ),
        //                        trigger(modulo(var(i), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // i <= 0 && j < 0 ==> j < i % j && i % j <= 0
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(var(i), intLiteral(0)),
        //                                less(var(j), intLiteral(0))
        //                                ),
        //                                logicalAnd(
        //                                        less(var(j), modulo(var(i), var(j))),
        //                                        lessEqual(modulo(var(i), var(j)), intLiteral(0)))
        //                        ),
        //                        trigger(modulo(var(i), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // 0 <= i && 0 < j ==> (i + j) % j == i % j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                less(intLiteral(0), var(j))
        //                                ),
        //                                isEqual(
        //                                        modulo(add(var(i), var(j)), var(j)),
        //                                        modulo(var(i), var(j)))
        //                        ),
        //                        trigger(modulo(add(var(i), var(j)), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // 0 <= i && 0 < j ==> (j + i) % j == i % j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                less(intLiteral(0), var(j))
        //                                ),
        //                                isEqual(
        //                                        modulo(add(var(j), var(i)), var(j)),
        //                                        modulo(var(i), var(j)))
        //                        ),
        //                        trigger(modulo(add(var(j), var(i)), var(j)))
        //                ));
        //
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        // 0 <= i - j && 0 < j ==> (i - j) % j == i % j
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), sub(var(i), var(j))),
        //                                less(intLiteral(0), var(j))
        //                                ),
        //                                isEqual(
        //                                        modulo(sub(var(i), var(j)), var(j)),
        //                                        modulo(var(i), var(j))
        //                                        )
        //                        ),
        //                        trigger(modulo(sub(var(i), var(j)), var(j)))
        //                ));
        //
        //        String a = quantVarName("a");
        //        String b = quantVarName("b");
        //        String d = quantVarName("d");
        //        BPLVariable aVar = new BPLVariable(a, BPLBuiltInType.INT);
        //        BPLVariable bVar = new BPLVariable(b, BPLBuiltInType.INT);
        //        BPLVariable dVar = new BPLVariable(d, BPLBuiltInType.INT);
        //        // 2 <= d && a % d == b % d && a < b ==> a + d <= b
        //        addAxiom(forall(
        //                aVar, bVar, dVar,
        //                implies(
        //                        logicalAnd(
        //                                logicalAnd(
        //                                        lessEqual(intLiteral(2), var(d)),
        //                                        isEqual(modulo(var(a), var(d)), modulo(var(b), var(d)))),
        //                                        less(var(a), var(b))),
        //                                        lessEqual(add(var(a), var(d)), var(b))),
        //                                        trigger(modulo(var(a), var(d)), modulo(var(b), var(d)))
        //                ));
    }

    /**
     * Axiomatizes the semantics of bitwise arithmetic operations which are not
     * directly supported by BoogiePL.
     */
    private void axiomatizeBitwiseInstructions() {
        //        addFunction(
        //                SHL_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //        addFunction(
        //                SHR_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //        addFunction(
        //                USHR_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //        addFunction(
        //                AND_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //        addFunction(
        //                OR_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //        addFunction(
        //                XOR_FUNC,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT,
        //                BPLBuiltInType.INT);
        //
        //        // shift left
        //        String i = quantVarName("i");
        //        BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar,
        //                isEqual(bitShl(var(i), intLiteral(0)), var(i)),
        //                trigger(bitShl(var(i), intLiteral(0)))
        //                ));
        //        String j = quantVarName("j");
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        BPLVariable jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        less(intLiteral(0), var(j)),
        //                        isEqual(
        //                                bitShl(var(i), var(j)),
        //                                multiply(
        //                                        bitShl(var(i), sub(var(j), intLiteral(1))),
        //                                        intLiteral(2)
        //                                        )
        //                                )
        //                        ),
        //                        trigger(bitShl(var(i), var(j)))
        //                ));
        //
        //        // shift right
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar,
        //                isEqual(bitShr(var(i), intLiteral(0)), var(i)),
        //                trigger(bitShr(var(i), intLiteral(0)))
        //                ));
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        less(intLiteral(0), var(j)),
        //                        isEqual(
        //                                bitShr(var(i), var(j)),
        //                                divide(
        //                                        bitShr(var(i), sub(var(j), intLiteral(1))),
        //                                        intLiteral(2)
        //                                        )
        //                                )
        //                        ),
        //                        trigger(bitShr(var(i), var(j)))
        //                ));
        //
        //        // unsigned shift right
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        lessEqual(intLiteral(0), var(i)),
        //                        isEqual(bitUShr(var(i), var(j)), bitShr(var(i), var(j)))
        //                        ),
        //                        trigger(bitUShr(var(i), var(j)))
        //                ));
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        less(intLiteral(0), var(j)),
        //                        lessEqual(intLiteral(0), bitUShr(var(i), var(j)))
        //                        ),
        //                        trigger(bitUShr(var(i), var(j)))
        //                ));
        //
        //        // bitwise and
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                isEquiv(
        //                        logicalOr(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                lessEqual(intLiteral(0), var(j))),
        //                                lessEqual(intLiteral(0), bitAnd(var(i), var(j)))
        //                        ),
        //                        trigger(bitAnd(var(i), var(j)))
        //                ));
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        isEqual(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                lessEqual(intLiteral(0), var(j))
        //                                ),
        //                                logicalAnd(
        //                                        lessEqual(bitAnd(var(i), var(j)), var(i)),
        //                                        lessEqual(bitAnd(var(i), var(j)), var(j))
        //                                        )),
        //                                        trigger(bitAnd(var(i), var(j)))));
        //
        //        // bitwise or
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                isEquiv(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                lessEqual(intLiteral(0), var(j))),
        //                                lessEqual(intLiteral(0), bitOr(var(i), var(j)))
        //                        ),
        //                        trigger(bitOr(var(i), var(j)))
        //                ));
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                implies(
        //                        logicalAnd(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                lessEqual(intLiteral(0), var(j))),
        //                                lessEqual(bitOr(var(i), var(j)), add(var(i), var(j)))
        //                        ),
        //                        trigger(bitOr(var(i), var(j)))
        //                ));
        //
        //        // bitwise xor
        //        iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //        jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //        addAxiom(forall(
        //                iVar, jVar,
        //                isEquiv(
        //                        isEqual(
        //                                lessEqual(intLiteral(0), var(i)),
        //                                lessEqual(intLiteral(0), var(j))),
        //                                lessEqual(intLiteral(0), bitXor(var(i), var(j)))
        //                        ),
        //                        trigger(bitXor(var(i), var(j)))
        //                ));
    }

    /**
     * Adds the heap axiomatization to the background theory.
     */
    private void axiomatizeHeap() {

        //        //
        //        // Muller/Poetzsch Heffter BoogiePL store axiomatization
        //        //
        //        addType(HEAP_TYPE);
        //
        //        // Create global heap variable for entire program
        //        addDeclaration(new BPLVariableDeclaration(new BPLVariable[] { new BPLVariable(tc.getHeap(), new BPLTypeName(HEAP_TYPE)) } ));
        //
        //        //
        //        // Values (objects, primitive values, arrays)
        //        //
        //        addType(VALUE_TYPE);
        //
        //        {
        //            // integer values
        //            addFunction(IVAL_FUNC, BPLBuiltInType.INT, new BPLTypeName(VALUE_TYPE));
        //            String i = quantVarName("i");
        //            String j = quantVarName("j");
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable jVar = new BPLVariable(j, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    iVar, jVar,
        //                    isEquiv(
        //                            isEqual(ival(var(i)), ival(var(j))),
        //                            isEqual(var(i), var(j))
        //                            ),
        //                            trigger(ival(var(i)), ival(var(j)))
        //                    ));
        //            String v = quantVarName("v");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(vVar, isEqual(ival(toint(var(v))), var(v)), trigger(ival(toint(var(v))))));
        //
        //            addFunction(TOINT_FUNC, new BPLTypeName(VALUE_TYPE), BPLBuiltInType.INT);
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(iVar, isEqual(toint(ival(var(i))), var(i)), trigger(toint(ival(var(i))))));
        //        }
        //
        //        {
        //            // reference values
        //            addFunction(RVAL_FUNC, new BPLTypeName(REF_TYPE), new BPLTypeName(VALUE_TYPE));
        //            String o1 = quantVarName("o1");
        //            String o2 = quantVarName("o2");
        //            BPLVariable o1Var = new BPLVariable(o1, new BPLTypeName(REF_TYPE));
        //            BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        //            addAxiom(forall(
        //                    o1Var, o2Var,
        //                    isEquiv(
        //                            isEqual(rval(var(o1)), rval(var(o2))),
        //                            isEqual(var(o1), var(o2))
        //                            ),
        //                            trigger(rval(var(o1)), rval(var(o2)))
        //                    ));
        //            String v = quantVarName("v");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(vVar, isEqual(rval(toref(var(v))), var(v)), trigger(rval(toref(var(v))))));
        //
        //            addFunction(TOREF_FUNC, new BPLTypeName(VALUE_TYPE), new BPLTypeName(REF_TYPE));
        //            String o = quantVarName("o");
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            addAxiom(forall(oVar, isEqual(toref(rval(var(o))), var(o)), trigger(toref(rval(var(o))))));
        //        }
        //
        //        {
        //            // integer and reference values
        //            String i = quantVarName("i");
        //            String o = quantVarName("o");
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            addAxiom(forall(iVar, oVar, notEqual(ival(var(i)), rval(var(o))), trigger(ival(var(i)), rval(var(o)))));
        //        }
        //
        //        {
        //            // type of a value
        //            addFunction(TYP_FUNC, new BPLTypeName(REF_TYPE),  new BPLTypeName(NAME_TYPE));
        //            String i = quantVarName("i");
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(iVar, isValueType(typ(ival(var(i)))), trigger(isValueType(typ(ival(var(i)))))));
        //        }
        //
        //        {
        //            // uninitialized (default) value
        //            addFunction(INIT_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(VALUE_TYPE));
        //
        //            String t = quantVarName("t");
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    tVar,
        //                    implies(
        //                            isValueType(var(t)),
        //                            isEqual(initVal(var(t)), ival(intLiteral(0)))
        //                            ),
        //                            trigger(isValueType(var(t)))
        //                    ));
        //
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    tVar,
        //                    implies(
        //                            isClassType(var(t)),
        //                            isEqual(initVal(var(t)), rval(nullLiteral()))
        //                            ),
        //                            trigger(isClassType(var(t)))
        //                    ));
        //
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    tVar,
        //                    isEqual(
        //                            initVal(arrayType(elementType(var(t)))),
        //                            rval(nullLiteral())
        //                            ),
        //                            trigger(initVal(arrayType(elementType(var(t)))))
        //                    ));
        //        }
        //
        //        {
        //            // static values
        //            addFunction(
        //                    STATIC_FUNC,
        //                    new BPLTypeName(VALUE_TYPE),
        //                    BPLBuiltInType.BOOL);
        //            String v = quantVarName("v");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    vVar,
        //                    isEquiv(
        //                            isStatic(var(v)),
        //                            logicalOr(
        //                                    isValueType(typ(var(v))),
        //                                    isEqual(var(v), rval(nullLiteral()))
        //                                    )
        //                            ),
        //                            trigger(isValueType(typ(var(v))))
        //                    ));
        //        }
        //
        //        {
        //            // array length
        //            addFunction(
        //                    ARRAY_LENGTH_FUNC,
        //                    new BPLTypeName(VALUE_TYPE),
        //                    BPLBuiltInType.INT);
        //            String v = quantVarName("v");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(vVar, lessEqual(intLiteral(0), arrayLength(var(v))), trigger(arrayLength(var(v)))));
        //        }
        //
        //        //
        //        // Locations (fields and array elements)
        //        //
        //        addType(LOCATION_TYPE);
        //
        //        {
        //            // An instance field (use typeObject for static fields)
        //            addFunction(
        //                    FIELD_LOC_FUNC,
        //                    new BPLTypeName(REF_TYPE),
        //                    new BPLTypeName(NAME_TYPE),
        //                    new BPLTypeName(LOCATION_TYPE));
        //
        //            String o1 = quantVarName("o1");
        //            String f1 = quantVarName("f1");
        //            String o2 = quantVarName("o2");
        //            String f2 = quantVarName("f2");
        //            BPLVariable o1Var = new BPLVariable(o1, new BPLTypeName(REF_TYPE));
        //            BPLVariable f1Var = new BPLVariable(f1, new BPLTypeName(NAME_TYPE));
        //            BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        //            BPLVariable f2Var = new BPLVariable(f2, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    o1Var, f1Var, o2Var, f2Var,
        //                    isEquiv(
        //                            isEqual(fieldLoc(var(o1), var(f1)), fieldLoc(var(o2), var(f2))),
        //                            logicalAnd(
        //                                    isEqual(var(o1), var(o2)),
        //                                    isEqual(var(f1), var(f2))
        //                                    )
        //                            ),
        //                            trigger(fieldLoc(var(o1), var(f1)), fieldLoc(var(o2), var(f2)))
        //                    ));
        //        }
        //
        //        {
        //            // An array element
        //            addFunction(
        //                    ARRAY_LOC_FUNC,
        //                    new BPLTypeName(REF_TYPE),
        //                    BPLBuiltInType.INT,
        //                    new BPLTypeName(LOCATION_TYPE));
        //
        //            String o1 = quantVarName("o1");
        //            String i1 = quantVarName("i1");
        //            String o2 = quantVarName("o2");
        //            String i2 = quantVarName("i2");
        //            BPLVariable o1Var = new BPLVariable(o1, new BPLTypeName(REF_TYPE));
        //            BPLVariable i1Var = new BPLVariable(i1, BPLBuiltInType.INT);
        //            BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        //            BPLVariable i2Var = new BPLVariable(i2, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    o1Var, i1Var, o2Var, i2Var,
        //                    isEquiv(
        //                            isEqual(arrayLoc(var(o1), var(i1)), arrayLoc(var(o2), var(i2))),
        //                            logicalAnd(
        //                                    isEqual(var(o1), var(o2)),
        //                                    isEqual(var(i1), var(i2))
        //                                    )
        //                            ),
        //                            trigger(arrayLoc(var(o1), var(i1)), arrayLoc(var(o2), var(i2)))
        //                    ));
        //        }
        //
        //        {
        //            // instance fields and array elements
        //            String o1 = quantVarName("o1");
        //            String f1 = quantVarName("f1");
        //            String o2 = quantVarName("o2");
        //            String i2 = quantVarName("i2");
        //            BPLVariable o1Var = new BPLVariable(o1, new BPLTypeName(REF_TYPE));
        //            BPLVariable f1Var = new BPLVariable(f1, new BPLTypeName(NAME_TYPE));
        //            BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        //            BPLVariable i2Var = new BPLVariable(i2, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    o1Var, f1Var, o2Var, i2Var,
        //                    notEqual(fieldLoc(var(o1), var(f1)), arrayLoc(var(o2), var(i2))),
        //                    trigger(fieldLoc(var(o1), var(f1)), arrayLoc(var(o2), var(i2)))
        //                    ));
        //        } 
        //
        //        {
        //            // The object reference referring to an array element or instance variable
        //            addFunction(OBJ_FUNC, new BPLTypeName(LOCATION_TYPE), new BPLTypeName(REF_TYPE));
        //            String o = quantVarName("o");
        //            String f = quantVarName("f");
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable fVar = new BPLVariable(f, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    oVar, fVar,
        //                    isEqual(obj(fieldLoc(var(o), var(f))), var(o)),
        //                    trigger(obj(fieldLoc(var(o), var(f))))
        //                    ));
        //            String i = quantVarName("i");
        //            oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    oVar, iVar,
        //                    isEqual(obj(arrayLoc(var(o), var(i))), var(o)),
        //                    trigger(obj(arrayLoc(var(o), var(i))))
        //                    ));
        //        }
        //
        //        {
        //            // Type of a location
        //            addFunction(
        //                    LTYP_FUNC,
        //                    new BPLTypeName(LOCATION_TYPE),
        //                    new BPLTypeName(NAME_TYPE));
        //            String o = quantVarName("o");
        //            String f = quantVarName("f");
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable fVar = new BPLVariable(f, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    oVar, fVar,
        //                    isEqual(ltyp(fieldLoc(var(o), var(f))), fieldType(var(f))),
        //                    trigger(ltyp(fieldLoc(var(o), var(f))))
        //                    ));
        //            String i = quantVarName("i");
        //            oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    oVar, iVar,
        //                    isEqual(
        //                            ltyp(arrayLoc(var(o), var(i))),
        //                            elementType(typ(rval(var(o))))
        //                            ),
        //                            trigger(ltyp(arrayLoc(var(o), var(i))))
        //                    ));
        //        }
        //
        //        // Field declaration
        //        addFunction(FIELD_TYPE_FUNC+"<alpha>", new BPLTypeName(FIELD_TYPE, new BPLTypeName("alpha")), new BPLTypeName(NAME_TYPE)); //TODO hack for parameter here
        //
        //        {
        //            // Static fields
        //            addFunction(TYPE_OBJECT_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(REF_TYPE));
        //            String t = quantVarName("t");
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(tVar, nonNull(typeObject(var(t))), trigger(typeObject(var(t)))));
        //            String h = quantVarName("h");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(hVar, tVar, alive(rval(typeObject(var(t))), var(h)), trigger(alive(rval(typeObject(var(t))), var(h)))));
        //        }
        //
        //        //
        //        // An allocation is either an object of a specified class type or an array
        //        // of a specified element type
        //        //
        //        addType(ALLOCATION_TYPE);
        //
        //        addFunction(
        //                OBJECT_ALLOC_FUNC,
        //                new BPLTypeName(NAME_TYPE),
        //                new BPLTypeName(ALLOCATION_TYPE));
        //        addFunction(
        //                ARRAY_ALLOC_FUNC,
        //                new BPLTypeName(NAME_TYPE),
        //                BPLBuiltInType.INT,
        //                new BPLTypeName(ALLOCATION_TYPE));
        //        addFunction(
        //                MULTI_ARRAY_ALLOC_FUNC,
        //                new BPLTypeName(NAME_TYPE),
        //                BPLBuiltInType.INT,
        //                new BPLTypeName(ALLOCATION_TYPE),
        //                new BPLTypeName(ALLOCATION_TYPE));
        //
        //        {
        //            addFunction(
        //                    ALLOC_TYPE_FUNC,
        //                    new BPLTypeName(ALLOCATION_TYPE),
        //                    new BPLTypeName(NAME_TYPE));
        //            String t = quantVarName("t");
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(tVar, isEqual(allocType(objectAlloc(var(t))), var(t)), trigger(allocType(objectAlloc(var(t))))));
        //
        //            String i = quantVarName("i");
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    tVar, iVar,
        //                    isEqual(allocType(arrayAlloc(var(t), var(i))), arrayType(var(t))),
        //                    trigger(allocType(arrayAlloc(var(t), var(i))))
        //                    ));
        //
        //            String a = quantVarName("a");
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    tVar, iVar, aVar,
        //                    isEqual(
        //                            allocType(multiArrayAlloc(var(t), var(i), var(a))),
        //                            arrayType(var(t))
        //                            ),
        //                            trigger(allocType(multiArrayAlloc(var(t), var(i), var(a))))
        //                    ));
        //        }
        //
        //        //
        //        // Heap functions
        //        //
        //
        //        addComment("Returns the heap after storing a value in a location.");
        //        addFunction(
        //                UPDATE_FUNC,
        //                new BPLTypeName(HEAP_TYPE),
        //                new BPLTypeName(LOCATION_TYPE),
        //                new BPLTypeName(VALUE_TYPE),
        //                new BPLTypeName(HEAP_TYPE));
        //
        //        addComment("Returns the heap after an object of the given type"
        //                + " has been allocated.");
        //        addFunction(
        //                ADD_FUNC,
        //                new BPLTypeName(HEAP_TYPE),
        //                new BPLTypeName(ALLOCATION_TYPE),
        //                new BPLTypeName(HEAP_TYPE));
        //
        //        addComment("Returns the value stored in a location.");
        //        addFunction(
        //                GET_FUNC,
        //                new BPLTypeName(HEAP_TYPE),
        //                new BPLTypeName(LOCATION_TYPE),
        //                new BPLTypeName(VALUE_TYPE));
        //
        //        addComment("Returns true if a value is alive in a given heap.");
        //        addFunction(
        //                ALIVE_FUNC,
        //                new BPLTypeName(VALUE_TYPE),
        //                new BPLTypeName(HEAP_TYPE),
        //                BPLBuiltInType.BOOL);
        //
        //        addComment("Returns a newly allocated object of the given type.");
        //        addFunction(
        //                NEW_FUNC,
        //                new BPLTypeName(HEAP_TYPE),
        //                new BPLTypeName(ALLOCATION_TYPE),
        //                new BPLTypeName(VALUE_TYPE));
        //
        //        //
        //        // Heap axioms
        //        //
        //
        //        {
        //            addComment("[SW]: a value is alive on the heap if it is written onto it.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            String v = quantVarName("v");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar, vVar,
        //                    alive(var(v), update(var(h), var(l), var(v)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Field stores do not affect the values stored in other fields.");
        //            String l1 = quantVarName("l1");
        //            String l2 = quantVarName("l2");
        //            String h = quantVarName("h");
        //            String v = quantVarName("v");
        //            BPLVariable l1Var = new BPLVariable(l1, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable l2Var = new BPLVariable(l2, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    l1Var, l2Var, hVar, vVar,
        //                    implies(
        //                            notEqual(var(l1), var(l2)),
        //                            isEqual(
        //                                    get(update(var(h), var(l1), var(v)), var(l2)),
        //                                    get(var(h), var(l2))
        //                                    )
        //                            ),
        //                            trigger(get(update(var(h), var(l1), var(v)), var(l2)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Field stores are persistent.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            String v = quantVarName("v");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar, vVar,
        //                    implies(
        //                            logicalAnd(
        //                                    alive(rval(obj(var(l))), var(h)),
        //                                    alive(var(v), var(h))),
        //                                    isEqual(get(update(var(h), var(l), var(v)), var(l)), var(v))
        //                            ),
        //                            trigger(get(update(var(h), var(l), var(v)), var(l)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Object allocation does not affect the existing heap.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            String a = quantVarName("a");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar, aVar,
        //                    isEqual(get(heapAdd(var(h), var(a)), var(l)), get(var(h), var(l))),
        //                    trigger(get(heapAdd(var(h), var(a)), var(l)))
        //                    ));
        //        }
        //
        //        { /*
        //      addComment("[SW]: Object allocation does not affect existing invariants.");
        //      String o = quantVarName("o");
        //      String t = quantVarName("t");
        //      String h = quantVarName("h");
        //      String a = quantVarName("a");
        //      BPLVariable oVar = new BPLVariable(o, BPLBuiltInType.REF);
        //      BPLVariable tVar = new BPLVariable(t, BPLBuiltInType.NAME);
        //      BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //      BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //      addAxiom(forall(
        //         oVar, tVar, hVar, aVar,
        //         isEqual(inv(var(t), var(o), heapAdd(var(h), var(a))), inv(var(t), var(o), var(h))),
        //         trigger(inv(var(t), var(o), heapAdd(var(h), var(a))))
        //      ));
        //         */ }
        //
        //        {
        //            addComment("Field stores do not affect object liveness.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            String v1 = quantVarName("v1");
        //            String v2 = quantVarName("v2");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable v1Var = new BPLVariable(v1, new BPLTypeName(VALUE_TYPE));
        //            BPLVariable v2Var = new BPLVariable(v2, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar, v1Var, v2Var,
        //                    isEquiv(
        //                            alive(var(v1), update(var(h), var(l), var(v2))),
        //                            alive(var(v1), var(h))
        //                            ),
        //                            trigger(alive(var(v1), update(var(h), var(l), var(v2))))
        //                    ));
        //        }
        //
        //        {
        //            addComment("[SW]: Field stores do not affect the invariants of other fields.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            String o = quantVarName("o");
        //            String t = quantVarName("t");
        //            String v = quantVarName("v");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar, oVar, tVar, vVar,
        //                    implies(
        //                            notEqual(var(o), obj(var(l))),
        //                            isEqual(
        //                                    inv(var(t), var(o), update(var(h), var(l), var(v))),
        //                                    inv(var(t), var(o), var(h))
        //                                    )
        //                            ),
        //                            trigger(inv(var(t), var(o), update(var(h), var(l), var(v))))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Alive objects remain alive when a newly allocated object is"
        //                    + " added to the heap.");
        //            String h = quantVarName("h");
        //            String v = quantVarName("v");
        //            String a = quantVarName("a");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, vVar, aVar,
        //                    implies(
        //                            alive(var(v), var(h)),
        //                            alive(var(v), heapAdd(var(h), var(a)))
        //                            ),
        //                            trigger(alive(var(v), heapAdd(var(h), var(a))))
        //                    ));
        //
        //            addComment("A newly allocated object becomes alive in the heap it is added to.");
        //            hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, aVar,
        //                    alive(heapNew(var(h), var(a)), heapAdd(var(h), var(a))),
        //                    trigger(heapNew(var(h), var(a)), heapAdd(var(h), var(a)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Values reachable from alive objects are themselves alive.");
        //            String l = quantVarName("l");
        //            String h = quantVarName("h");
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            addAxiom(forall(
        //                    lVar, hVar,
        //                    implies(
        //                            alive(rval(obj(var(l))), var(h)),
        //                            alive(get(var(h), var(l)), var(h))),
        //                            trigger(alive(get(var(h), var(l)), var(h)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Static values are always alive.");
        //            String h = quantVarName("h");
        //            String v = quantVarName("v");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    hVar, vVar,
        //                    implies(isStatic(var(v)), alive(var(v), var(h))),
        //                    trigger(alive(var(v), var(h)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("A newly allocated object is not alive"
        //                    + " in the heap it was created in.");
        //            String h = quantVarName("h");
        //            String a = quantVarName("a");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, aVar,
        //                    logicalNot(alive(heapNew(var(h), var(a)), var(h))),
        //                    trigger(alive(heapNew(var(h), var(a)), var(h)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Allocated objects retain their type.");
        //            String h = quantVarName("h");
        //            String a = quantVarName("a");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, aVar,
        //                    isEqual(typ(heapNew(var(h), var(a))), allocType(var(a))),
        //                    trigger(typ(heapNew(var(h), var(a))))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Creating an object of a given type in two heaps yields"
        //                    + " the same result if liveness of all objects of that type"
        //                    + " is identical in both heaps.");
        //            String h1 = quantVarName("h1");
        //            String h2 = quantVarName("h2");
        //            String a = quantVarName("a");
        //            String v = quantVarName("v");
        //            BPLVariable h1Var = new BPLVariable(h1, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable h2Var = new BPLVariable(h2, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            addAxiom(forall(
        //                    h1Var, h2Var, aVar,
        //                    isEquiv(
        //                            isEqual(heapNew(var(h1), var(a)), heapNew(var(h2), var(a))),
        //                            forall(
        //                                    vVar,
        //                                    implies(
        //                                            isEqual(typ(var(v)), allocType(var(a))),
        //                                            isEquiv(
        //                                                    alive(var(v), var(h1)),
        //                                                    alive(var(v), var(h2))
        //                                                    )
        //                                            ),
        //                                            trigger(alive(var(v), var(h1)), alive(var(v), var(h2)), allocType(var(a)))
        //                                    )
        //                            ),
        //                            trigger(heapNew(var(h1), var(a)), heapNew(var(h2), var(a)))
        //                    ));
        //        }
        //
        //        {
        //            addComment("Two heaps are equal if they are indistinguishable"
        //                    + " by the alive and get functions.");
        //            String h1 = quantVarName("h1");
        //            String h2 = quantVarName("h2");
        //            String v = quantVarName("v");
        //            String l = quantVarName("l");
        //            BPLVariable h1Var = new BPLVariable(h1, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable h2Var = new BPLVariable(h2, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            addAxiom(forall(
        //                    h1Var, h2Var,
        //                    implies(
        //                            logicalAnd(
        //                                    forall(
        //                                            vVar,
        //                                            isEquiv(alive(var(v), var(h1)), alive(var(v), var(h2))),
        //                                            trigger(alive(var(v), var(h1)), alive(var(v), var(h2)))
        //                                            ),
        //                                            forall(
        //                                                    lVar,
        //                                                    isEqual(get(var(h1), var(l)), get(var(h2), var(l))),
        //                                                    trigger(get(var(h1), var(l)), get(var(h2), var(l)))
        //                                                    )
        //                                    ),
        //                                    isEqual(var(h1), var(h2))
        //                            )
        //                    ));
        //        }
        //
        //        {
        //            addComment("[SW]: object allocations preserve existing invariants");
        //            String o = quantVarName("o");
        //            String t = quantVarName("t");
        //            String o2 = quantVarName("o2");
        //            String t2 = quantVarName("t2");
        //            String pre_h = quantVarName("pre_h");
        //            String new_h = quantVarName("new_h");
        //            BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLVariable o2Var = new BPLVariable(o2, new BPLTypeName(REF_TYPE));
        //            BPLVariable t2Var = new BPLVariable(t2, new BPLTypeName(NAME_TYPE));
        //            BPLVariable pre_hVar = new BPLVariable(pre_h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable new_hVar = new BPLVariable(new_h, new BPLTypeName(HEAP_TYPE));
        //            addAxiom(forall(
        //                    oVar, tVar, pre_hVar, new_hVar,
        //                    implies(
        //                            logicalAnd(
        //                                    isEqual(heapNew(var(pre_h), objectAlloc(var(t))), rval(var(o))),
        //                                    isEqual(var(new_h), heapAdd(var(pre_h), objectAlloc(var(t))))
        //                                    ),
        //                                    logicalAnd(
        //                                            logicalNot(inv(var(t), var(o), var(new_h))),
        //                                            forall(
        //                                                    o2Var, t2Var,
        //                                                    implies(
        //                                                            logicalOr(
        //                                                                    notEqual(var(t2), var(t)),
        //                                                                    notEqual(var(o2), var(o))
        //                                                                    ),
        //                                                                    isEqual(
        //                                                                            inv(var(t2), var(o2), var(new_h)),
        //                                                                            inv(var(t2), var(o2), var(pre_h))
        //                                                                            )
        //                                                            )
        //                                                    )
        //                                            )
        //                            )
        //                    ));
        //        }
        //
        //        {
        //            addComment("Get always returns either null or a value whose type"
        //                    + " is a subtype of the (static) location type.");
        //            String h = quantVarName("h");
        //            String l = quantVarName("l");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
        //            addAxiom(forall(hVar, lVar, isOfType(get(var(h), var(l)), ltyp(var(l))),trigger(isOfType(get(var(h), var(l)), ltyp(var(l))))));
        //        }
        //
        //        {
        //            addComment("New arrays have the allocated length.");
        //            String h = quantVarName("h");
        //            String t = quantVarName("t");
        //            String i = quantVarName("i");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    hVar, tVar, iVar,
        //                    isEqual(
        //                            arrayLength(heapNew(var(h), arrayAlloc(var(t), var(i)))),
        //                            var(i)
        //                            ),
        //                            trigger(arrayLength(heapNew(var(h), arrayAlloc(var(t), var(i)))))
        //                    ));
        //
        //            String a = quantVarName("a");
        //            hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, tVar, iVar, aVar,
        //                    isEqual(
        //                            arrayLength(heapNew(var(h), multiArrayAlloc(var(t), var(i), var(a)))),
        //                            var(i)
        //                            ),
        //                            trigger(arrayLength(heapNew(var(h), multiArrayAlloc(var(t), var(i), var(a)))))
        //                    ));
        //        }
        //
        //        {
        //            // Multi-dimensional arrays
        //            addFunction(
        //                    IS_NEW_MULTI_ARRAY_FUNC,
        //                    new BPLTypeName(VALUE_TYPE),
        //                    new BPLTypeName(HEAP_TYPE),
        //                    new BPLTypeName(ALLOCATION_TYPE),
        //                    BPLBuiltInType.BOOL);
        //            addFunction(
        //                    MULTI_ARRAY_PARENT_FUNC,
        //                    new BPLTypeName(VALUE_TYPE),
        //                    new BPLTypeName(VALUE_TYPE));
        //            addFunction(
        //                    MULTI_ARRAY_POSITION_FUNC,
        //                    new BPLTypeName(VALUE_TYPE),
        //                    BPLBuiltInType.INT);
        //
        //            String h = quantVarName("h");
        //            String t = quantVarName("t");
        //            String i = quantVarName("i");
        //            String a = quantVarName("a");
        //            BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            addAxiom(forall(
        //                    hVar, tVar, iVar, aVar,
        //                    isNewMultiArray(
        //                            heapNew(var(h), multiArrayAlloc(var(t), var(i), var(a))),
        //                            var(h),
        //                            multiArrayAlloc(var(t), var(i), var(a))
        //                            ),
        //                            trigger(
        //                                    isNewMultiArray(
        //                                            heapNew(var(h), multiArrayAlloc(var(t), var(i), var(a))),
        //                                            var(h),
        //                                            multiArrayAlloc(var(t), var(i), var(a))
        //                                            )
        //                                    )
        //                    ));
        //
        //            String v = quantVarName("v");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    vVar, hVar, tVar, iVar,
        //                    isEquiv(
        //                            isNewMultiArray(var(v), var(h), arrayAlloc(var(t), var(i))),
        //                            logicalAnd(
        //                                    logicalNot(alive(var(v), var(h))),
        //                                    isEqual(typ(var(v)), arrayType(var(t))),
        //                                    isEqual(arrayLength(var(v)), var(i))
        //                                    )
        //                            ),
        //                            trigger(isNewMultiArray(var(v), var(h), arrayAlloc(var(t), var(i))))
        //                    ));
        //
        //            String e = quantVarName("e");
        //            vVar = new BPLVariable(v, new BPLTypeName(VALUE_TYPE));
        //            hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            aVar = new BPLVariable(a, new BPLTypeName(ALLOCATION_TYPE));
        //            BPLVariable eVar = new BPLVariable(e, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    vVar, hVar, tVar, iVar, aVar,
        //                    isEquiv(
        //                            isNewMultiArray(var(v), var(h), multiArrayAlloc(var(t), var(i), var(a))),
        //                            logicalAnd(
        //                                    logicalNot(alive(var(v), var(h))),
        //                                    isEqual(typ(var(v)), arrayType(var(t))),
        //                                    isEqual(arrayLength(var(v)), var(i)),
        //                                    forall(
        //                                            eVar,
        //                                            logicalAnd(
        //                                                    isNewMultiArray(get(var(h), arrayLoc(toref(var(v)), var(e))), var(h), var(a)),
        //                                                    isEqual(
        //                                                            multiArrayParent(get(var(h), arrayLoc(toref(var(v)), var(e)))),
        //                                                            var(v)
        //                                                            ),
        //                                                            isEqual(
        //                                                                    multiArrayPosition(get(var(h), arrayLoc(toref(var(v)), var(e)))),
        //                                                                    var(e)
        //                                                                    )
        //                                                    ),
        //                                                    trigger(isNewMultiArray(get(var(h), arrayLoc(toref(var(v)), var(e))), var(h), var(a)))
        //                                            )
        //                                    )
        //                            ),
        //                            trigger(isNewMultiArray(var(v), var(h), multiArrayAlloc(var(t), var(i), var(a))))
        //                    ));
        //        }
    }

    /**
     * Axiomatizes some aspects of the JVM type system.
     */
    private void axiomatizeTypeSystem() {
        //        //
        //        // Types
        //        //
        //        addFunction(IS_CLASS_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
        //        addFunction(IS_VALUE_TYPE_FUNC, new BPLTypeName(NAME_TYPE), BPLBuiltInType.BOOL);
        //
        //        {
        //            // primitive types
        //            for (JBaseType valueType : valueTypes) {
        //                addConstants(new BPLVariable(
        //                        getValueTypeName(valueType),
        //                        new BPLTypeName(NAME_TYPE)));
        //            }
        //
        //            String t = quantVarName("t");
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            BPLExpression[] vtExprs = new BPLExpression[valueTypes.length];
        //            for (int i = 0; i < valueTypes.length; i++) {
        //                vtExprs[i] = isEqual(var(t), typeRef(valueTypes[i]));
        //            }
        //            addComment("Defines the set of value types.");
        //            addAxiom(forall(tVar, isEquiv(isValueType(var(t)), logicalOr(vtExprs)), trigger(isValueType(var(t)))));
        //        }
        //
        //        {
        //            addComment("Returns whether an integer constant is in the range of a given value type.");
        //            addFunction(
        //                    IS_IN_RANGE_FUNC,
        //                    BPLBuiltInType.INT,
        //                    new BPLTypeName(NAME_TYPE),
        //                    BPLBuiltInType.BOOL);
        //
        //            for (JBaseType valueType : valueTypes) {
        //                String i = quantVarName("i");
        //                BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //                addAxiom(forall(
        //                        iVar,
        //                        isEquiv(
        //                                isInRange(var(i), typeRef(valueType)),
        //                                logicalAnd(
        //                                        lessEqual(intLiteral(getMinValue(valueType)), var(i)),
        //                                        lessEqual(var(i), intLiteral(getMaxValue(valueType)))
        //                                        )
        //                                ),
        //                                trigger(isInRange(var(i), typeRef(valueType)))
        //                        ));
        //            }
        //
        ////            String i = quantVarName("i");
        ////            String t = quantVarName("t");
        ////            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        ////            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        ////            addComment("Associate the types of integer values to their corresponding value ranges.");
        ////            addAxiom(forall(
        ////                    iVar, tVar,
        ////                    isEquiv(
        ////                            isSubtype(typ(var(i)), var(t)),
        ////                            isInRange(var(i), var(t))
        ////                            ),
        ////                            trigger(isInRange(var(i), var(t)))
        ////                    ));
        //        }
        //
        //        {
        //            // casting of value types
        //            addFunction(
        //                    ICAST_FUNC,
        //                    BPLBuiltInType.INT,
        //                    new BPLTypeName(NAME_TYPE),
        //                    BPLBuiltInType.INT);
        //
        //            addComment("A cast value is in the value range of the target type.");
        //            String i = quantVarName("i");
        //            String t = quantVarName("t");
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    iVar, tVar,
        //                    isInRange(icast(var(i), var(t)), var(t)),
        //                    trigger(isInRange(icast(var(i), var(t)), var(t)))
        //                    ));
        //
        //            addComment("Values which already are in the target value range are"
        //                    + " not affected by a cast.");
        //            iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            addAxiom(forall(
        //                    iVar, tVar,
        //                    implies(
        //                            isInRange(var(i), var(t)),
        //                            isEqual(icast(var(i), var(t)), var(i))
        //                            ),
        //                            trigger(isInRange(var(i), var(t)))
        //                    ));
        //        }
        //
        ////        {
        ////            // array types
        ////            addFunction(ARRAY_TYPE_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE));
        ////
        ////            addFunction(ELEMENT_TYPE_FUNC, new BPLTypeName(NAME_TYPE), new BPLTypeName(NAME_TYPE));
        ////            String t = quantVarName("t");
        ////            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        ////            addAxiom(forall(tVar, isEqual(elementType(arrayType(var(t))), var(t)), trigger(elementType(arrayType(var(t))))));
        ////        }
        //
        ////        {
        ////            JType object = TypeLoader.getClassType("java.lang.Object");
        ////            JType cloneable = TypeLoader.getClassType("java.lang.Cloneable");
        ////            JType throwable = TypeLoader.getClassType("java.lang.Throwable");
        ////            JType serializable = TypeLoader.getClassType("java.io.Serializable");
        ////
        ////            String t = quantVarName("t");
        ////            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        ////            addAxiom(forall(
        ////                    tVar,
        ////                    logicalAnd(
        ////                            isSubtype(arrayType(var(t)), typeRef(object)),
        ////                            isSubtype(arrayType(var(t)), typeRef(cloneable)),
        ////                            isSubtype(arrayType(var(t)), typeRef(serializable)),
        ////                            isSubtype(arrayType(var(t)), typeRef(throwable))
        ////                            )
        ////                    ));
        ////
        ////            String t1 = quantVarName("t1");
        ////            String t2 = quantVarName("t2");
        ////            BPLVariable t1Var = new BPLVariable(t1, new BPLTypeName(NAME_TYPE));
        ////            BPLVariable t2Var = new BPLVariable(t2, new BPLTypeName(NAME_TYPE));
        ////            addAxiom(forall(
        ////                    t1Var, t2Var,
        ////                    implies(
        ////                            isSubtype(var(t1), var(t2)),
        ////                            isSubtype(arrayType(var(t1)), arrayType(var(t2)))
        ////                            )
        ////                    ));
        ////
        ////            t1Var = new BPLVariable(t1, new BPLTypeName(NAME_TYPE));
        ////            t2Var = new BPLVariable(t2, new BPLTypeName(NAME_TYPE));
        ////            addAxiom(forall(
        ////                    t1Var, t2Var,
        ////                    implies(
        ////                            isSubtype(var(t1), arrayType(var(t2))),
        ////                            logicalAnd(
        ////                                    isEqual(var(t1), arrayType(elementType(var(t1)))),
        ////                                    isSubtype(elementType(var(t1)), var(t2)))
        ////                            ),
        ////                            trigger(isSubtype(var(t1), arrayType(var(t2))))
        ////                    ));
        ////        }
        //
        //        {
        //            // Method calls (exception handling)
        //            addComment("Exception handling");
        //
        //            addType(RETURN_STATE_TYPE);
        //
        //            String n = quantVarName(NORMAL_RETURN_STATE);
        //            String ex = quantVarName(EXCEPTIONAL_RETURN_STATE);
        //
        //            String s = quantVarName("s");
        //
        //            BPLType returnState = new BPLTypeName(RETURN_STATE_TYPE);
        //            BPLVariable normal = new BPLVariable(n, returnState);
        //            BPLVariable exceptional = new BPLVariable(ex, returnState);
        //            BPLVariable sVar = new BPLVariable(s, returnState);
        //            //TODO does not work when adding as addConstants(normal, exceptional);
        //            addConstants(normal);
        //            addConstants(exceptional);
        //
        //            addFunction(IS_NORMAL_RETURN_STATE_FUNC, returnState, BPLBuiltInType.BOOL);
        //            addFunction(IS_EXCEPTIONAL_RETURN_STATE_FUNC, returnState, BPLBuiltInType.BOOL);
        //            addAxiom(forall(sVar, isEquiv(notEqual(var(s), var(n)), logicalNot(isNormalReturnState(var(s)))), trigger(isNormalReturnState(var(s)))));
        //            addAxiom(forall(sVar, isEquiv(notEqual(var(s), var(ex)), logicalNot(isExceptionalReturnState(var(s)))), trigger(isExceptionalReturnState(var(s)))));
        //        }

        //        {
        //            // FIXME[sw]: Temporary partial axiomatization of the Java type system.
        //            //            Should later be replaced with either an on-the-fly compilation
        //            //            of (BML annotated) Java Runtime Libraries or a
        //            //            precompiled BoogiePL version.
        //            typeRef(TypeLoader.getClassType("java.lang.Exception"));
        //            declarations.add(axiomatizeHelperProcedure("java.lang.Object..init", "$java.lang.Object"));
        //            declarations.add(axiomatizeHelperProcedure("java.lang.Throwable..init", "$java.lang.Throwable"));
        //            declarations.add(axiomatizeHelperProcedure("java.lang.Exception..init", "$java.lang.Exception")); // $java.lang.Throwable is sufficient
        //            declarations.add(axiomatizeHelperProcedure("java.io.PrintStream.println", null));
        //        }

        {
            // Class fields which appear in one or more modifies clauses
            // SpecificationTranslator translator = SpecificationTranslator.forModifiesClause(tc.getHeap(), parameters);
            // return translator.translateModifiesStoreRefs(context, project.getSpecificationDesugarer().getModifiesStoreRefs(method));
        }
    }

    //    private BPLProcedure axiomatizeHelperProcedure(String name, String type) {
    //        String l = quantVarName("l");
    //        String o = quantVarName("o");
    //        String t = quantVarName("t");
    //        String this_var_name = quantVarName("param0");
    //        BPLVariable lVar = new BPLVariable(l, new BPLTypeName(LOCATION_TYPE));
    //        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
    //        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
    //        BPLVariable this_var = new BPLVariable(this_var_name, new BPLTypeName(REF_TYPE));
    //
    //        boolean hasReturnType = (type != null);
    //
    //        return new BPLProcedure(
    //                name,
    //                new BPLVariable[] { this_var },
    //                new BPLVariable[] {
    //                        new BPLVariable(RETURN_STATE_PARAM, new BPLTypeName(RETURN_STATE_TYPE)),
    //                        new BPLVariable(RESULT_PARAM + REF_TYPE_ABBREV, new BPLTypeName(REF_TYPE)),
    //                        new BPLVariable(EXCEPTION_PARAM, new BPLTypeName(REF_TYPE))
    //                },   
    //
    //                new BPLSpecification(new BPLSpecificationClause[] {
    //
    //                        new BPLRequiresClause(
    //                                // All invariants are required to hold prior to a constructor call.
    //                                forall(
    //                                        oVar, tVar,
    //                                        implies(
    //                                                logicalAnd(
    //                                                        alive(rval(var(o)), var(tc.getHeap())),
    //                                                        isSubtype(var(t), typ(rval(var(o)))),
    //                                                        notEqual(var(o), var(this_var_name))
    //                                                        ),
    //                                                        inv(var(t), var(o), var(tc.getHeap()))
    //                                                )
    //                                        )
    //                                )
    //
    //                        ,
    //
    //                        hasReturnType ?
    //
    //                                new BPLEnsuresClause(logicalAnd(
    //                                        // postcondition of helper procedure (usually constructor)
    //                                        isEqual(var(RESULT_PARAM + REF_TYPE_ABBREV), var(this_var_name)),
    //                                        notEqual(var(RESULT_PARAM + REF_TYPE_ABBREV), BPLNullLiteral.NULL),
    //                                        alive(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), var(tc.getHeap())),
    //                                        isInstanceOf(rval(var(RESULT_PARAM + REF_TYPE_ABBREV)), var(type))//,
    ////                                        forall(lVar,
    ////                                                implies(
    ////                                                        // alive(rval(var(o)), old(var(tc.getHeap()))),
    ////                                                        alive(rval(obj(var(l))), old(var(tc.getHeap()))),
    ////                                                        logicalAnd(
    ////                                                                isEqual(
    ////                                                                        get(var(tc.getHeap()), var(l)),
    ////                                                                        get(old(var(tc.getHeap())), var(l))
    ////                                                                        ),
    ////                                                                        //alive(rval(var(o)), var(tc.getHeap()))
    ////                                                                        alive(rval(obj(var(l))), var(tc.getHeap()))
    ////                                                                )
    ////                                                        )
    ////                                                )
    //                                        ))
    //
    //                        :
    //
    //                            new BPLEnsuresClause(
    //                                    // postcondition of helper procedure (usually constructor)
    //                                    forall(lVar,
    //                                            implies(
    //                                                    // alive(rval(var(o)), old(var(tc.getHeap()))),
    //                                                    alive(rval(obj(var(l))), old(var(tc.getHeap()))),
    //                                                    logicalAnd(
    //                                                            isEqual(
    //                                                                    get(var(tc.getHeap()), var(l)),
    //                                                                    get(old(var(tc.getHeap())), var(l))
    //                                                                    ),
    //                                                                    //alive(rval(var(o)), var(tc.getHeap()))
    //                                                                    alive(rval(obj(var(l))), var(tc.getHeap()))
    //                                                            )
    //                                                    )
    //                                            )
    //                                    )
    //
    //                                ,
    //
    //                                new BPLEnsuresClause(
    //                                        // All invariants are required to hold after a constructor call.
    //                                        forall(
    //                                                oVar, tVar,
    //                                                implies(
    //                                                        // BPLBoolLiteral.FALSE,
    //                                                        isEqual(var(o), var(this_var_name)),
    //                                                        inv(var(t), var(o), var(tc.getHeap()))
    //                                                        )
    //                                                )
    //                                        )
    //
    //                })
    //                );
    //    }

    /**
     * Defines and axiomatizes some simple helper functions.
     */
    private void axiomatizeHelperFunctions() {
        //        {
        //            // A helper function for converting bool values to int values.
        //            addFunction(BOOL2INT_FUNC, BPLBuiltInType.BOOL, BPLBuiltInType.INT);
        //
        //            String b = quantVarName("b");
        //            BPLVariable bVar = new BPLVariable(b, BPLBuiltInType.BOOL);
        //            addAxiom(forall(
        //                    bVar,
        //                    isEquiv(
        //                            isEqual(bool2int(var(b)), intLiteral(0)),
        //                            isEqual(var(b), BPLBoolLiteral.FALSE)
        //                            ),
        //                            trigger(bool2int(var(b)))
        //                    ));
        //
        //            bVar = new BPLVariable(b, BPLBuiltInType.BOOL);
        //            addAxiom(forall(
        //                    bVar,
        //                    isEquiv(
        //                            notEqual(bool2int(var(b)), intLiteral(0)),
        //                            isEqual(var(b), BPLBoolLiteral.TRUE)
        //                            ),
        //                            trigger(bool2int(var(b)))
        //                    ));
        //        }
        //
        //        {
        //            // A helper function for converting int values to bool values.
        //            addFunction(INT2BOOL_FUNC, BPLBuiltInType.INT, BPLBuiltInType.BOOL);
        //
        //            String i = quantVarName("i");
        //            BPLVariable iVar = new BPLVariable(i, BPLBuiltInType.INT);
        //            addAxiom(forall(
        //                    iVar,
        //                    isEquiv(
        //                            isEqual(int2bool(var(i)), BPLBoolLiteral.FALSE),
        //                            isEqual(var(i), intLiteral(0))
        //                            ),
        //                            trigger(int2bool(var(i)))
        //                    ));
        //
        //            addAxiom(forall(
        //                    iVar,
        //                    isEquiv(
        //                            isEqual(int2bool(var(i)), BPLBoolLiteral.TRUE),
        //                            notEqual(var(i), intLiteral(0))
        //                            ),
        //                            trigger(int2bool(var(i)))
        //                    ));
        //        }
        //
        //        {
        //            addFunction(
        //                    IS_OF_TYPE_FUNC,
        //                    new BPLTypeName(REF_TYPE),
        //                    new BPLTypeName(NAME_TYPE),
        //                    BPLBuiltInType.BOOL);
        //            String v = quantVarName("v");
        //            String t = quantVarName("t");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(REF_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            // A value is of a given type if and only if it is the null value or if
        //            // its type is a subtype of the given type.
        //            addAxiom(forall(
        //                    vVar, tVar,
        //                    isEquiv(
        //                            isOfType(var(v), var(t)),
        //                            logicalOr(
        //                                    isEqual(var(v), nullLiteral()),
        //                                    isSubtype(typ(var(v)), var(t))
        //                                    )
        //                            ),
        //                            trigger(isOfType(var(v), var(t)))
        //                    ));
        //        }
        //
        ////        {
        //        //TODO check that the definition of isInstanceOf is ok
        //            addFunction(
        //                    IS_INSTANCE_OF_FUNC,
        //                    new BPLTypeName(REF_TYPE),
        //                    new BPLTypeName(NAME_TYPE),
        //                    BPLBuiltInType.BOOL);
        //            String v = quantVarName("v");
        //            String t = quantVarName("t");
        //            BPLVariable vVar = new BPLVariable(v, new BPLTypeName(REF_TYPE));
        //            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
        //            // A value is an instance of a given type if and only if it is not the
        //            // null value and if its type is a subtype of the given type.
        //            addAxiom(forall(
        //                    vVar, tVar,
        //                    isEquiv(
        //                            isInstanceOf(var(v), var(t)),
        //                            logicalAnd(
        //                                    notEqual(var(v), nullLiteral()),
        //                                    isSubtype(typ(var(v)), var(t))
        //                                    )
        //                            ),
        //                            trigger(isInstanceOf(var(v), var(t)))
        //                    ));
        ////
        ////            // [SW] inserted: if a value is an instance of a given type t,
        ////            // the value is of type t.
        ////            addAxiom(forall(
        ////                    vVar, tVar,
        ////                    implies(
        ////                            isInstanceOf(var(v), var(t)),
        ////                            isOfType(var(v), var(t))
        ////                            ),
        ////                            trigger(isInstanceOf(var(v), var(t)))
        ////                    ));   
        ////        }
        //
        ////        addComment("The function used for the declaration of object invariants.");
        ////        addFunction(
        ////                INV_FUNC,
        ////                new BPLTypeName(NAME_TYPE),
        ////                new BPLTypeName(REF_TYPE),
        ////                new BPLTypeName(HEAP_TYPE),
        ////                BPLBuiltInType.BOOL);
        //
        //        {
        //            // if-then-else function
        //            addFunction(
        //                    IF_THEN_ELSE_FUNC,
        //                    BPLBuiltInType.BOOL,
        //                    new BPLTypeName(ANY_TYPE),
        //                    new BPLTypeName(ANY_TYPE),
        //                    new BPLTypeName(ANY_TYPE));
        //
        //            String b = quantVarName("b");
        //            String x = quantVarName("x");
        //            String y = quantVarName("y");
        //            BPLVariable bVar = new BPLVariable(b, BPLBuiltInType.BOOL);
        //            BPLVariable xVar = new BPLVariable(x, new BPLTypeName(ANY_TYPE));
        //            BPLVariable yVar = new BPLVariable(y, new BPLTypeName(ANY_TYPE));
        //            addAxiom(forall(
        //                    bVar, xVar, yVar,
        //                    implies(
        //                            var(b),
        //                            isEqual(ifThenElse(var(b), var(x), var(y)), var(x))
        //                            ),
        //                            trigger(ifThenElse(var(b), var(x), var(y)))
        //                    ));
        //
        //            bVar = new BPLVariable(b, BPLBuiltInType.BOOL);
        //            xVar = new BPLVariable(x, new BPLTypeName(ANY_TYPE));
        //            yVar = new BPLVariable(y, new BPLTypeName(ANY_TYPE));
        //            addAxiom(forall(
        //                    bVar, xVar, yVar,
        //                    implies(
        //                            logicalNot(var(b)),
        //                            isEqual(ifThenElse(var(b), var(x), var(y)), var(y))
        //                            ),
        //                            trigger(ifThenElse(var(b), var(x), var(y)))
        //                    ));
        //        }
    }

    /**
     * Method in which all the pending background theory for which information
     * has been collected during the actual translation of the bytecode methods
     * can be added to the BoogiePL program. This method is thought for those
     * parts of the background theory which can only be generated once all the
     * bytecode methods have been translated. Therefore, it should be invoked
     * right before assembling the BoogiePL program after the translation of
     * all bytecode methods.
     */
    private void flushPendingTheory() {
        // If we have generated symbolic constants representing large integer
        // values, we axiomatize their relative order in order maintain as much
        // information as possible.
        if (context.symbolicIntLiterals.size() > 0) {
            // The requested iterator gives us the integers in ascending order.
            Iterator<Long> intConstants = context.symbolicIntLiterals.iterator();
            long current = intConstants.next();
            long maxConstant = project.getMaxIntConstant();
            // Handle the negative values.
            while ((current < 0) && intConstants.hasNext()) {
                long next = intConstants.next();
                if (next < 0) {
                    // If the next integer is still negative, we simply state that the
                    // current integer is less than the next one.
                    addAxiom(less(intLiteral(current), intLiteral(next)));
                } else {
                    // If the next integer is positive, we state that the current integer
                    // is less than the lowest integer value explicitly represented in the
                    // BoogiePL program but we do not relate the current negative
                    // integer to the next one which is positive.
                    addAxiom(less(intLiteral(current), intLiteral(-maxConstant)));
                }
                current = next;
            }
            if (current < 0) {
                // If the current integer is still negative, the above loop guard tells
                // us that there are no more integers to process but we must still
                // relate the current negative integer to the lowest integer value
                // explicitly represented in the BoogiePL program.
                addAxiom(less(intLiteral(current), intLiteral(-maxConstant)));
            } else {
                // If the current integer is positive, we relate it to the largest
                // integer value explicitly represented in the BoogiePL program.
                addAxiom(less(intLiteral(maxConstant), intLiteral(current)));
            }
            while (intConstants.hasNext()) {
                // Likewise, axiomatize the relative order of the remaining integers
                // which must be all positive.
                long next = intConstants.next();
                addAxiom(less(intLiteral(current), intLiteral(next)));
                current = next;
            }
        }
    }

    /**
     * Adds an axiom representing the {@code type}'s object invariant.
     *
     * @param type  The class type whose object invariant should be translated.
     */
    private void addInvariantDeclaration(JClassType type) {
        // Get the actual invariant predicate as declared in the given class
        // (not including the invariants declared in superclasses).
//        BMLExpression invariant = project.getSpecificationDesugarer().getObjectInvariant(type, true);
//
//        String o = quantVarName("o");
//        String h = quantVarName("h");
//        String t = quantVarName("t");

//        SpecificationTranslator translator =
//                SpecificationTranslator.forInvariant(h, o);

//        BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
//        BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
//        BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));

        // An invariant hold for a given object if and only if the object is an
        // instance of the class in which the invariant is declared and if the
        // actual invariant predicate holds in for the given object in the given
        // heap.
        /*
    addAxiom(forall(
        oVar, hVar,
        isEquiv(
            inv(typeRef(type), var(o), var(h)),
            implies(
                isInstanceOf(rval(var(o)), typeRef(type)),
                translator.translate(context, invariant)
            )
        ),
        trigger(inv(typeRef(type), var(o), var(h)))
    ));
         */

        // Extended version:
        //TODO do we need something like this in our implementation?
        //        addAxiom(forall(
        //                oVar, hVar, tVar,
        //                implies(
        //                        isSubtype(var(t), typeRef(type)),
        //                        isEquiv(
        //                                inv(var(t), var(o), var(h)),
        //                                implies(
        //                                        isInstanceOf(var(o), var(t)),
        //                                        translator.translate(context, invariant)
        //                                        )
        //                                )
        //                        )  
        //                ));

        //     axiom (forall o: ref, h: Store, t: name :: t <: $test4.A ==> inv(t, o, h) <==> isInstanceOf(rval(o), t) ==> toint(get(h, fieldLoc(o, test4.A.value))) >= 0); // inserted

    }

    /**
     * Implementation of the {@link ITranslationContext} interfTwo heaps are equal if they are indistinguishable by the alive and get functions.ace which handles
     * the translation of different kinds of references.
     *
     * @author Ovidio Mallo, Samuel Willimann
     */
    private final class Context implements ITranslationContext {

        /** The types referenced during the translation. */
        private HashSet<JClassType> typeReferences;

        /** The fields referenced during the translation. */
        private HashSet<BCField> fieldReferences;

        /**
         * The integers encountered during the translation which are not represented
         * as such in the generated BoogiePL program but by symbolic constants
         * instead.
         */
        private TreeSet<Long> symbolicIntLiterals;

        /** The string literals encountered during the translation. */
        private HashMap<String, String> stringLiterals;

        /** The class literals encountered during the translation. */
        private HashSet<JType> classLiterals;

        /**
         * Initializes the internal datastructures.
         */
        public Context() {
            typeReferences = tc.referencedTypes();
            fieldReferences = tc.referencedFields();
            symbolicIntLiterals = new TreeSet<Long>();
            stringLiterals = new HashMap<String, String>();
            classLiterals = new HashSet<JType>();
        }

        /**
         * Generates an axiom for the given {@code type} which rules out
         * "wrong supertypes" of that {@code type} by contradiction. This
         * makes it possible for the program verifier to not only show whether some
         * type indeed is a supertype of the given {@code type} but also
         * whether it is <i>not</i> a supertype.
         *
         * @param type  The class type to axiomatize.
         */
        private void translateSubtyping(JClassType type) {
            // Recursively state that if some type t is a supertype of the given type,
            // then t must be the type itself, or else, it is a supertype of one of
            // the type's direct supertypes.
            String t = quantVarName("t");
            BPLExpression axiom = isEqual(var(t), translateTypeReference(type));
            JClassType supertype = type.getSupertype();
            if (supertype != null) {
                addAxiom(isSubtype(typeRef(type), typeRef(supertype)));
                axiom = logicalOr(
                        axiom,
                        isSubtype(translateTypeReference(supertype), var(t)));
            }
            for (JClassType iface : type.getInterfaces()) {
                addAxiom(isSubtype(typeRef(type), typeRef(supertype)));
                axiom = logicalOr(
                        axiom,
                        isSubtype(translateTypeReference(iface), var(t)));
            }
            BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
            addAxiom(forall(
                    tVar,
                    implies(isSubtype(translateTypeReference(type), var(t)), axiom),
                    trigger(isSubtype(translateTypeReference(type), var(t)))));
        }

        /**
         * Translates the given {@code type} reference. The translation of a
         * new type reference thereby triggers the generation of the following
         * declarations in the BoogiePL program:
         * <ul>
         *   <li>
         *     A <i>name</i> constant representing the given {@code type} is
         *     declared.
         *   </li>
         *   <li>
         *     If the given {@code type} is final, an appropriate axiom
         *     expressing this fact is generated.
         *   </li>
         *   <li>
         *     A set of axioms defining the supertype hierarchy of the given
         *     {@code type} is generated.
         *   </li>
         *   <li>
         *     An axiom expressing the {@code type}'s object invariant is
         *     generated.
         *   </li>
         * </ul>
         * The returned {@code BPLExpression} is guaranteed to be of type
         * <i>name</i>.
         */
        public BPLExpression translateTypeReference(JType type) {
            // Only class types trigger the translation of constants representing
            // them.
            if (type.isClassType() && !typeReferences.contains(type)) {
                JClassType classType = (JClassType) type;
                typeReferences.add(classType);

                // Declare the constant representing the given class type.
                addConstants(new BPLVariable(
                        getClassTypeName(classType),
                        new BPLTypeName(NAME_TYPE)));
                
                // State that the type indeed is a class type.
                if(!classType.isInterface()){
                    addAxiom(isClassType(typeRef(classType)));
                } else {
                    addAxiom(logicalNot(isClassType(typeRef(classType)))); // interfaces are no classes
                    addAxiom(isMemberlessType(typeRef(classType)));
                }

                // Eventually axiomatize the fact that the type is final.
                if (classType.isFinal()) {
                    String t = quantVarName("t");
                    BPLVariable tVar = new BPLVariable(t, new BPLTypeName(NAME_TYPE));
                    // Every eventual subtype must be the type itself.
                    addAxiom(forall(
                            tVar,
                            implies(
                                    isSubtype(var(t), typeRef(classType)),
                                    isEqual(var(t), typeRef(classType))
                                    ),
                                    trigger(isSubtype(var(t), typeRef(classType)))
                            ));
                }

                if(classType.isPublic()){
                    addAxiom(new BPLFunctionApplication(IS_PUBLIC_FUNC, typeRef(classType)));
                } else {
                    addAxiom(logicalNot(new BPLFunctionApplication(IS_PUBLIC_FUNC, typeRef(classType))));
                }

                // Generate axioms for ruling out "wrong supertypes".
                translateSubtyping(classType);

                // For every referenced class type, we generate an axiom representing
                // its object invariant.
                addInvariantDeclaration(classType);
            }

            // Now, do the actual translation of the type reference to be used in the
            // BoogiePL program.
            if (type.isBaseType()) {
                return var(getValueTypeName((JBaseType) type));
            } else if (type.isClassType()) {
                return var(getClassTypeName((JClassType) type));
            } else {
                //TODO implement array access
//                // We must have an array type.
//                JArrayType arrayType = (JArrayType) type;
//                BPLExpression typeExpr =
//                        translateTypeReference(arrayType.getComponentType());
//                for (int i = 0; i < arrayType.getDimension(); i++) {
//                    typeExpr = arrayType(typeExpr);
//                }
//                return typeExpr;
                return nullLiteral();
            }
        }

        /**
         * Translates the given {@code field} reference. The translation of a
         * new field reference thereby triggers the generation of the following
         * declarations in the BoogiePL program:
         * <ul>
         *   <li>
         *     A <i>name</i> constant representing the given {@code type} is
         *     declared.
         *   </li>
         *   <li>
         *     An axiom defining the {@code field}'s declared type is
         *     generated.
         *   </li>
         *   <li>
         *     The {@code field}'s owner type is translated.
         *   </li>
         * </ul>
         * The returned {@code BPLExpression} is guaranteed to be of type
         * <i>name</i>.
         */
        public BPLExpression translateFieldReference(BCField field) {
            String fieldName = GLOBAL_VAR_PREFIX+field.getQualifiedName(); //TODO add type information to make field name unambiguous?
            if (!fieldReferences.contains(field)) {
                fieldReferences.add(field);

                // Declare the constant representing the given field.
                addConstants(new BPLVariable(fieldName, new BPLTypeName(FIELD_TYPE, CodeGenerator.type(field.getType()))));

                // Define the field's declared type.
                addAxiom(isEqual(
                        fieldType(var(fieldName)),
                        translateTypeReference(field.getType())));

                String o = quantVarName("o");
                String h = quantVarName("h");
                BPLVariable oVar = new BPLVariable(o, new BPLTypeName(REF_TYPE));
                BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));

                if(!field.getType().isBaseType()){
                    addComment("[SW]: Define field type");
                    addAxiom(forall(
                            oVar, hVar,
                            isSubtype(
                                    typ(new BPLArrayExpression(var(h), var(o), var(fieldName)), var(h)),
                                    translateTypeReference(field.getType())
                                    )
                            ));
                }

                // For every field referenced, we also translate its owner type.
                translateTypeReference(field.getOwner());
            }
            return var(fieldName);
        }

        /**
         * Translates the given integer {@code literal}. If the integer's
         * magnitude is less or equal to the value returned by the
         * {@link Project#getMaxIntConstant()} method of the current project,
         * the integer is translated as is to BoogiePL. Otherwise, it is replaced
         * by a special constant representing its value.
         * The returned {@code BPLExpression} is guaranteed to be of type
         * <i>int</i>.
         */
        public BPLExpression translateIntLiteral(long literal) {
            // If the integer value is in the desired range, the literal is translated
            // as is.
            if ((-project.getMaxIntConstant() <= literal)
                    && (literal <= project.getMaxIntConstant())) {
                return new BPLIntLiteral((int) literal);
            }

            // If the integer's magnitude is too large, we represent it by a symbolic
            // constant.
            if (symbolicIntLiterals.add(literal)) {
                addConstants(new BPLVariable(
                        getSymbolicIntLiteralName(literal),
                        BPLBuiltInType.INT));
            }
            return var(getSymbolicIntLiteralName(literal));
        }

        /**
         * Translates the given string {@code literal}.
         * The returned {@code BPLExpression} is guaranteed to be of type
         * <i>ref</i>.
         */
        public BPLExpression translateStringLiteral(String literal) {
            if (stringLiterals.get(literal) == null) {
                String name = STRING_LITERAL_PREFIX + stringLiterals.size();
                stringLiterals.put(literal, name);

                // Declare the constant representing the given field.
                addConstants(new BPLVariable(name, new BPLTypeName(REF_TYPE)));

                // State that the object representing the literal is of type String and
                // that it is alive in any heap.
                JType string = TypeLoader.getClassType("java.lang.String");
                String h = quantVarName("h");
                BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
                //                addAxiom(forall(
                //                        hVar,
                //                        logicalAnd(
                //                                isInstanceOf(rval(var(name)), typeRef(string)),
                //                                alive(rval(var(name)), var(h))
                //                                ),
                //                                trigger(alive(rval(var(name)), var(h)))
                //                        ));
            }
            return var(stringLiterals.get(literal));
        }

        /**
         * Translates the given class {@code literal}.
         * The returned {@code BPLExpression} is guaranteed to be of type
         * <i>ref</i>.
         */
        public BPLExpression translateClassLiteral(JType literal) {
            String name =
                    GLOBAL_VAR_PREFIX + literal.getName() + CLASS_LITERAL_SUFFIX;
            if (classLiterals.add(literal)) {
                // Declare the constant representing the given field.
                addConstants(new BPLVariable(name, new BPLTypeName(REF_TYPE)));

                // State that the object representing the literal is of type Class and
                // that it is alive in any heap.
                JType clazz = TypeLoader.getClassType("java.lang.Class");
                String h = quantVarName("h");
                BPLVariable hVar = new BPLVariable(h, new BPLTypeName(HEAP_TYPE));
                //                addAxiom(forall(
                //                        hVar,
                //                        logicalAnd(
                //                                isInstanceOf(rval(var(name)), typeRef(clazz)),
                //                                alive(rval(var(name)), var(h))
                //                                ),
                //                                trigger(alive(rval(var(name)), var(h)))
                //                        ));
            }
            return var(name);
        }
    }
}

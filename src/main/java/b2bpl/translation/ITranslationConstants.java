package b2bpl.translation;


public interface ITranslationConstants {

  String UNDERSCORE = "_";
  
  String CONSTRUCTOR_NAME = ".init";

  String CLASS_INITIALIZER_NAME = ".clinit";

  String GLOBAL_VAR_PREFIX = "$";

  String VALUE_TYPE_PREFIX = "$";

  String FUNC_PREFIX = "";

  String REAL_TYPE = "customreal";
 
  String ARRAY_TYPE = "Elements";
  
  String ACTIVITY_TYPE = "ActivityType";
  
  String REF_TYPE = "Ref";
  
//  String ANY_TYPE = "Any";
  
  String NAME_TYPE = "TName";
  
  String METHOD_TYPE = "Method";

  String FIELD_TYPE = "Field";
  
  String VAR_TYPE = "Var";
  
  String WELLFORMED_HEAP_FUNC = "WellformedHeap";
  
  String WELLFORMED_COUPLING_FUNC = "WellformedCoupling";
  
  String OBJECT_COUPLING_FUNC = "ObjectCoupling";
  
  String VALUE_RELATION_FUNC = "ValueRelation";
  
  String BIJECTIVE_FUNC = "Bijective";
  
  String SYNTHETIC_FIELD_FUNC = "SyntheticField";
  
  String LIBRARY_FIELD_FUNC = "LibraryField";
  
  String CLASS_REPR_FUNC = "ClassRepr";
  
  String CLASS_REPR_INV_FUNC = "ClassReprInv";
  
  String UTTER_FUNC = "Utter";
  
  String IS_STATIC_FIELD_FUNC = "IsStaticField";
  
  String BASE_CLASS_FUNC = "BaseClass";
  
  String AS_DIRECT_SUB_CLASS_FUNC = "AsDirectSubClass";
  
  String ONE_CLASS_DOWN_FUNC = "OneClassDown";
  
  String AS_TYPE_FUNC = "AsType";
  
  String IS_ALLOCATED_FUNC = "IsAllocated";
  
  String DECL_TYPE_FUNC = "DeclType";
  
  String AS_REF_FIELD_FUNC = "AsRefField";
  
  String AS_RANGE_FIELD_FUNC = "AsRangeField";
  
  String IS_MEMBERLESS_TYPE_FUNC = "IsMemberlessType";
  
  String AS_INTERFACE_FUNC = "AsInterface";
  
  String INT_TO_INT_FUNC = "IntToInt";
  
  String INT_TO_REAL_FUNC = "IntToReal";
  
  String REAL_TO_INT_FUNC = "RealToInt";
  
  String REAL_TO_REAL_FUNC = "RealToReal";
  
  String SIZE_IS_FUNC = "SizeIs";
  
  String REL_NULL_FUNC = "RelNull";
  
  String NON_NULL_FUNC = "NonNull";
  
  String REF_OF_TYPE_FUNC = "RefOfType";
  
  String INTERNAL_FUNC = "Internal";
  
  String FTYPE_FUNC = "FType";
  
  String UNIQUE_FUNC = "Unique";
  
  String WELLFORMED_STACK_FUNC = "WellformedStack";
  
  String EMPTY_INTERACTION_FRAME_FUNC = "EmptyInteractionFrame";
  
  String EMPTY_STACK_FRAME_FUNC = "EmptyStackFrame";
  
  String IS_PUBLIC_FUNC = "isPublic";
  
  String DEFINES_METHOD_FUNC = "definesMethod";
  
  String HAS_RETURN_VALUE_FUNC = "hasReturnValue";
  
  String CLASS_EXTENDS_FUNC = "classExtends";
  
  String MEMBER_OF_FUNC = "memberOf";
  
  String LIB_TYPE_FUNC = "libType";
  
  String CTXT_TYPE_FUNC = "ctxtType";
  
  String BEING_CONSTRUCTED_CONST = GLOBAL_VAR_PREFIX+"BeingConstructed";

  String STACK_PTR_TYPE = "StackPtr";

  String STACK_FRAME_TYPE = "StackFrame";
  
  String INTERACTION_FRAME_TYPE = "InteractionFrame";
  
  String OLD_SP_MAP1_VAR = "old_spmap1";
  String OLD_SP_MAP2_VAR = "old_spmap2";
  
  String SP_MAP1_VAR = "spmap1";
  
  String SP_MAP2_VAR = "spmap2";
  
  String IP1_VAR = "ip1";
  
  String IP2_VAR = "ip2";

  String STACK_TYPE = "Stack";
  
  String HEAP_TYPE =         "Heap";
  
  String BINREL_TYPE = "BinRel";

//  String LOCATION_TYPE =     "Location";

//  String ALLOCATION_TYPE =   "Allocation";
  
//  String RETURN_STATE_TYPE = "ReturnState";

//  String HEAP_VAR     = "heap";

//  String OLD_HEAP_VAR = "old_heap";

  String PRE_HEAP_VAR = "pre_heap";

  String LOOP_HEAP_VAR_PREFIX = "loop_heap";

  String LOOP_VARIANT_VAR_PREFIX = "loop_variant";

//  String VALUE_TYPE = "Value";

  String RESULT_VAR = "result";

  String INT_TYPE_ABBREV = UNDERSCORE + "i";

  String REF_TYPE_ABBREV = UNDERSCORE + "r";

  //String PARAM_VAR_PREFIX = "param";

  String LOCAL_VAR_PREFIX = "reg";

  String STACK_VAR_PREFIX = "stack";

//  String CALL_RESULT_VAR_PREFIX = "callResult";
  
  // String THIS_VAR = "this";
  
//  String INT_CALL_RESULT_VAR = CALL_RESULT_VAR_PREFIX + INT_TYPE_ABBREV;
//
//  String REF_CALL_RESULT_VAR = CALL_RESULT_VAR_PREFIX + REF_TYPE_ABBREV;
  
//  String NORMAL_RETURN_STATE =      VALUE_TYPE_PREFIX + "normal";
//  
//  String EXCEPTIONAL_RETURN_STATE = VALUE_TYPE_PREFIX + "exceptional";

//  String RETURN_STATE_VAR = "rs";
  
  String RETURN_VALUE_VAR = "rv";
  
  String EXCEPTION_VAR    = "ex";
  
//  String RETURN_HEAP_PARAM =  "retheap";
//  
//  String RETURN_STATE_PARAM = "retstate";
//  
  String RESULT_PARAM = "result";
  
  String EXCEPTION_PARAM    = "exception";

  // String INT_CALL_RESULT_VAR = /* CALL_RESULT_VAR_PREFIX */ RETURN_VALUE_VAR + INT_TYPE_ABBREV;

  // String REF_CALL_RESULT_VAR = /* CALL_RESULT_VAR_PREFIX */ RETURN_VALUE_VAR + REF_TYPE_ABBREV;

  String SWAP_VAR_PREFIX = "swap";

  String INT_SWAP_VAR = SWAP_VAR_PREFIX + INT_TYPE_ABBREV;

  String REF_SWAP_VAR = SWAP_VAR_PREFIX + REF_TYPE_ABBREV;

  String BOOL2INT_FUNC = FUNC_PREFIX + "bool2int";

  String INT2BOOL_FUNC = FUNC_PREFIX + "int2bool";

  String SHL_FUNC = FUNC_PREFIX +  "#shl";

  String SHR_FUNC = FUNC_PREFIX +  "#shr";

  String USHR_FUNC = FUNC_PREFIX + "#ushr";

  String NEG_FUNC = FUNC_PREFIX + "#neg";
  
  String AND_FUNC = FUNC_PREFIX +  "#and";

  String OR_FUNC = FUNC_PREFIX +   "#or";

  String XOR_FUNC = FUNC_PREFIX +  "#xor";


  String RNEG_FUNC =  FUNC_PREFIX + "#rneg";
  
  String RADD_FUNC = FUNC_PREFIX +  "#radd";
  
  String RSUB_FUNC = FUNC_PREFIX +  "#rsub";
  
  String RMUL_FUNC = FUNC_PREFIX +  "#rmul";
  
  String RDIV_FUNC = FUNC_PREFIX +  "#rdiv";
  
  String RMOD_FUNC = FUNC_PREFIX +  "#rmod";
  
  String RLESS_FUNC = FUNC_PREFIX +  "#rLess";
  
  String RLESS_OR_EQUAL_FUNC = FUNC_PREFIX +  "#rLeq";
  
  String REQ_FUNC = FUNC_PREFIX +  "#rEq";
  
  String RNEQ_FUNC = FUNC_PREFIX +  "#rNeq";
  
  String RGREATER_OR_EQUAL_FUNC = FUNC_PREFIX +  "#rGeq";
  
  String RGREATER_FUNC = FUNC_PREFIX +  "#rGreater";

  
  String AND_OP = "&&";
  
  String OR_OP =  "||";

  String IS_CLASS_TYPE_FUNC = FUNC_PREFIX + "isClassType";

  String IS_VALUE_TYPE_FUNC = FUNC_PREFIX + "isValueType";

//  String IS_ARRAY_TYPE_FUNC = FUNC_PREFIX + "isArrayType";
  
//  String IS_NORMAL_RETURN_STATE_FUNC = FUNC_PREFIX + "isNormalReturnState";
  
//  String IS_EXCEPTIONAL_RETURN_STATE_FUNC = FUNC_PREFIX + "isExceptionalReturnState";

  String INV_FUNC = FUNC_PREFIX + "inv";

  String FIELD_TYPE_FUNC = FUNC_PREFIX + "fieldType";

//  String FIELD_LOC_FUNC = FUNC_PREFIX + "fieldLoc";

//  String ARRAY_LOC_FUNC = FUNC_PREFIX + "arrayLoc";

//  String OBJ_FUNC = FUNC_PREFIX + "obj";

  String ARRAY_LENGTH_FUNC = FUNC_PREFIX + "arrayLength";

//  String ARRAY_TYPE_FUNC = FUNC_PREFIX + "arrayType";

//  String ELEMENT_TYPE_FUNC = FUNC_PREFIX + "elementType";

//  String TYPE_OBJECT_FUNC = FUNC_PREFIX + "typeObject";

//  String OBJECT_ALLOC_FUNC = FUNC_PREFIX + "objectAlloc";

//  String ARRAY_ALLOC_FUNC = FUNC_PREFIX + "arrayAlloc";

//  String MULTI_ARRAY_ALLOC_FUNC = FUNC_PREFIX + "multiArrayAlloc";

//  String IS_NEW_MULTI_ARRAY_FUNC = FUNC_PREFIX + "isNewMultiArray";

//  String MULTI_ARRAY_PARENT_FUNC = FUNC_PREFIX + "multiArrayParent";

//  String MULTI_ARRAY_POSITION_FUNC = FUNC_PREFIX + "multiArrayPosition";

//  String GET_FUNC = FUNC_PREFIX + "get";

//  String UPDATE_FUNC = FUNC_PREFIX + "update";

//  String ALIVE_FUNC = FUNC_PREFIX + "alive";

//  String NEW_FUNC = FUNC_PREFIX + "new";

//  String ADD_FUNC = FUNC_PREFIX + "add";

//  String TOINT_FUNC = FUNC_PREFIX + "toint";

//  String TOREF_FUNC = FUNC_PREFIX + "toref";

//  String IVAL_FUNC = FUNC_PREFIX + "ival";
//
//  String RVAL_FUNC = FUNC_PREFIX + "rval";
//
//  String INIT_FUNC = FUNC_PREFIX + "init";
//
//  String STATIC_FUNC = FUNC_PREFIX + "static";

  String TYP_FUNC = FUNC_PREFIX + "typ";

//  String LTYP_FUNC = FUNC_PREFIX + "ltyp";
//
//  String ALLOC_TYPE_FUNC = FUNC_PREFIX + "allocType";

  String IS_OF_TYPE_FUNC = FUNC_PREFIX + "isOfType";

  String IS_INSTANCE_OF_FUNC = FUNC_PREFIX + "isInstanceOf";

  String IS_IN_RANGE_FUNC = FUNC_PREFIX + "isInRange";

//  String ICAST_FUNC = FUNC_PREFIX + "icast";

  String IF_THEN_ELSE_FUNC = FUNC_PREFIX + "ifThenElse";

  String BLOCK_LABEL_PREFIX = "block_";

  String INIT_BLOCK_LABEL = "init";

  String PRE_BLOCK_LABEL = "pre";

  String POST_BLOCK_LABEL= "post";
  
  String EXCEPTION_HANDLERS_LABEL = "exception_handlers";

  String POSTX_BLOCK_LABEL_PREFIX = "postX_";

  String EXIT_BLOCK_LABEL = "exit";

  String LOOP_BLOCK_LABEL_SUFFIX = "_Loop";

  String TRUE_BLOCK_LABEL_SUFFIX = "_True";

  String FALSE_BLOCK_LABEL_SUFFIX = "_False";

  String CASE_BLOCK_LABEL_SUFFIX = "_Case";

  String DEFAULT_BLOCK_LABEL_SUFFIX = "_Default";

  String NO_EXCEPTION_BLOCK_LABEL_SUFFIX = "_Normal";

  String EXCEPTION_BLOCK_LABEL_SUFFIX = "_X_#";

  String RUNTIME_EXCEPTION_TRUE_BLOCK_LABEL_SUFFIX = "_RT_X_True_#";

  String RUNTIME_EXCEPTION_FALSE_BLOCK_LABEL_SUFFIX = "_RT_X_False_#";

  String HANDLER_BLOCK_LABEL_SUFFIX = "_Handler_#";

  String STRING_LITERAL_PREFIX = GLOBAL_VAR_PREFIX + "stringLiteral";

  String INT_LITERAL_PREFIX = GLOBAL_VAR_PREFIX + "int#";

  String CLASS_LITERAL_SUFFIX = ".class";

  String ADDRESS_TYPE = "Address";

  String OBJ_FUNC = "Obj";
  
  String OBJ_OF_TYPE_FUNC = "ObjOfType";
  
  String EMPTY_REL_FUNC = "EmptyRelation";
  
  String IS_CALLABLE_FUNC = "isCallable";

  String CALLTABLE_LABEL = "calltable";
 
  String RETTABLE_LABEL = "rettable";
  
  String LOCAL_PLACES_TABLE_LABEL = "localplacestable";
  
  String CONSTRUCTOR_TABLE_LABEL = "constructortable";

public static final String CHECK_BOUNDARY_RETURN_LABEL = "check_boundary_return";

public static final String CHECK_BOUNDARY_CALL_LABEL = "check_boundary_call";

public static final String CHECK_LOCAL_LABEL = "check_local";

public static final String METH_FIELD = "meth";

public static final String EXPOSED_FIELD = "exposed";

public static final String RELATED_RELATION = "related";

public static final String PLACE_VARIABLE = "place";

public static final String STACK1 = "stack1";

public static final String STACK2 = "stack2";

public static final String HEAP1 = "heap1";

public static final String HEAP2 = "heap2";

public static final String OLD_HEAP1 = "old_heap1";

public static final String OLD_HEAP2 = "old_heap2";

public static final String OLD_STACK1 = "old_stack1";

public static final String OLD_STACK2 = "old_stack2";

public static final String STALL1 = "stall1";

public static final String STALL2 = "stall2";

public static final String PRECONDITIONS_LABEL = "preconditions";

public static final String PRECONDITIONS_CALL_LABEL = "preconditions_call";

public static final String PRECONDITIONS_RETURN_LABEL = "preconditions_return";

public static final String PRECONDITIONS_CONSTRUCTOR_LABEL = "preconditions_constructor";

public static final String PRECONDITIONS_LOCAL_LABEL = "preconditions_local";

public static final String INITIAL_CONFIGS_INV_LABEL = "initialConfigsCoupled";

public static final String STEPS_IN_CONTEXT_PRESERVED_LABEL = "stepsInContextPreserved";

public static final String CREATED_BY_CTXT_FIELD = "createdByCtxt";

public static final String CHECK_LIBRARIES_PROCEDURE_NAME = "checkLibraries";

public static final String INIT_LABEL_POSTFIX = "_init";

public static final String INTERN_LABEL_POSTFIX = "_intern";

public static final String BOUNDARY_LABEL_POSTFIX = "_boundary";

static final String VALID_HEAP_SUCC_FUNC = "ValidHeapSucc";

public static final String IS_STATIC_METHOD_FUNC = "IsStaticMethod";

public static final String IS_FINAL_METHOD_FUNC = "IsFinalMethod";

public static final String NUM_PARAMS_FUNC = "numParams";

public static final String PLACE_DEFINED_IN_TYPE = "placeDefinedInType";

public static final String PLACE_DEFINED_IN_METHOD = "placeDefinedInMethod";

static final String USE_HAVOC = "useHavoc";

public static final String DYN_TYPE_FIELD = "dynType";

public static final String ALLOC_FIELD = "alloc";

public static final String IS_LOCAL_PLACE_FUNC = "isLocalPlace";

public static final String MEASURE = "measure";

public static final String OLD_MEASURE = "old_measure";


public static final String OLD_PLACE1 = "old_place1";
public static final String OLD_PLACE2 = "old_place2";

public static final String STACK_FRAME_TEMP = "sftmp";
public static final String INTERACTION_FRAME_TEMP = "iftmp";
public static final String SOME_OBJ_TEMP = "someObj" + REF_TYPE_ABBREV;
public static final String SOME_VAL_R_TEMP = "someVal" + REF_TYPE_ABBREV;
public static final String SOME_VAL_I_TEMP = "someVal" + INT_TYPE_ABBREV;
public static final String SOME_FIELD_R_TEMP = "someField" + REF_TYPE_ABBREV;
public static final String SOME_FIELD_I_TEMP = "someField"+ INT_TYPE_ABBREV;

public static final String LIBRARY_IMPL_TYPE = "Library";
public static final String IMPL1 = "lib1";
public static final String IMPL2 = "lib2";

}
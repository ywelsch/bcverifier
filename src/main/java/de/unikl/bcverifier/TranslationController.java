package de.unikl.bcverifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.JClassType;

public class TranslationController {
    private static boolean isActive = false;
    private static int round;
    private static final String HEAP1 = "heap1";
    private static final String HEAP2 = "heap2";
    private static final String STACK1 = "stack1";
    private static final String STACK2 = "stack2";
    private static final String SP1 = "sp1";
    private static final String SP2 = "sp2";
    private static final String LABEL_PREFIX1 = "lib1_";
    private static final String LABEL_PREFIX2 = "lib2_";
    private static final String DISPATCH_LABEL1 = "dispatch1";
    private static final String DISPATCH_LABEL2 = "dispatch2";
    private static final String VERIFY_LABEL = "check";
    private static final String CHECK_LABEL1 = DISPATCH_LABEL2;
    private static final String CHECK_LABEL2 = VERIFY_LABEL;
    
    private static Set<String> declaredMethods = new HashSet<String>();
    private static Map<String, BPLVariable> usedVariables = new HashMap<String, BPLVariable>();
    private static Map<String, BPLVariable> stackVariables = new HashMap<String, BPLVariable>();
    private static HashSet<JClassType> referencedTypes = new HashSet<JClassType>();
    private static HashSet<BCField> referencedFields = new HashSet<BCField>();
    
    public static Set<String> declaredMethods() {
        return declaredMethods;
    }
    
    public static Map<String, BPLVariable> usedVariables(){
        return usedVariables;
    }
    
    public static Map<String, BPLVariable> stackVariables() {
        return stackVariables;
    }
    
    public static HashSet<JClassType> referencedTypes() {
        return referencedTypes;
    }
    
    public static HashSet<BCField> referencedFields() {
        return referencedFields;
    }
    
    public static void activate() {
        isActive = true;
        declaredMethods.clear();
        usedVariables.clear();
        stackVariables.clear();
        referencedTypes.clear();
        referencedFields.clear();
    }
    
    public static void deactivate() {
        isActive = false;
    }
    
    public static boolean isActive() {
        return isActive;
    }
    
    public static void enterRound1() {
        round = 1;
    }
    
    public static void enterRound2() {
        round = 2;
    }
    
    public static String getHeap() {
        switch(round) {
        case 1:
            return HEAP1;
        case 2:
            return HEAP2;
        default:
            return "heap";
        }
    }
    
    public static String getStack(){
        switch(round){
        case 1:
            return STACK1;
        case 2:
            return STACK2;
        default:
            return "stack";
        }
    }
    
    public static String getStackPointer() {
        switch(round){
        case 1:
            return SP1;
        case 2:
            return SP2;
        default:
            return "sp";
        }
    }
    
    public static String prefix(String label) {
        switch(round){
        case 1:
            return LABEL_PREFIX1+label;
        case 2:
            return LABEL_PREFIX2+label;
        default:
            return label;
        }
    }
    
    public static String getDispatchLabel() {
        switch(round){
        case 1:
            return DISPATCH_LABEL1;
        case 2:
            return DISPATCH_LABEL2;
        default:
            return "dispatch";
        }
    }
    
    public static String getCheckLabel() {
        switch(round){
        case 1:
            return CHECK_LABEL1;
        case 2:
            return CHECK_LABEL2;
        default:
            return "check";
        }
    }
}

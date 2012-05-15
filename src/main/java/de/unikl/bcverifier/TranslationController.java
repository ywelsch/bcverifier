package de.unikl.bcverifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.JClassType;

public class TranslationController {
    private static boolean isActive = false;
    private static int round;
    public static final String HEAP1 = "heap1";
    public static final String HEAP2 = "heap2";
    public static final String STACK1 = "stack1";
    public static final String STACK2 = "stack2";
    public static final String SP1 = "sp1";
    public static final String SP2 = "sp2";
    public static final String LABEL_PREFIX1 = "lib1_";
    public static final String LABEL_PREFIX2 = "lib2_";
    public static final String DISPATCH_LABEL1 = "dispatch1";
    public static final String DISPATCH_LABEL2 = "dispatch2";
    public static final String VERIFY_LABEL = "check";
    public static final String CHECK_LABEL1 = DISPATCH_LABEL2;
    public static final String CHECK_LABEL2 = VERIFY_LABEL;
    
    private static Set<String> declaredMethods = new HashSet<String>();
    private static Map<String, BPLVariable> usedVariables = new HashMap<String, BPLVariable>();
    private static Map<String, BPLVariable> stackVariables = new HashMap<String, BPLVariable>();
    private static HashSet<JClassType> referencedTypes = new HashSet<JClassType>();
    private static HashSet<BCField> referencedFields = new HashSet<BCField>();
    private static HashSet<String> places = new HashSet<String>();
    private static HashMap<String, Set<String>> methodDefinitions = new HashMap<String, Set<String>>();
    private static Set<String> returnLabels = new HashSet<String>();
    
    private static String lastPlace = null;
    private static String nextLabel = null;
    
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
    
    public static HashMap<String, Set<String>> methodDefinitions() {
        return methodDefinitions;
    }
    
    public static Set<String> returnLabels() {
        return returnLabels;
    }
    
    public static void resetReturnLabels() {
        returnLabels.clear();
    }
    
    public static void definesMethod(String className, String methodName) {
        Set<String> definedMethods = methodDefinitions.get(className);
        if(definedMethods==null){
            definedMethods = new HashSet<String>();
            methodDefinitions.put(className, definedMethods);
        }
        definedMethods.add(methodName);
    }
    
    public static HashSet<String> places() {
        return places;
    }
    
    public static String nextLabel() {
        return nextLabel;
    }
    
    public static void activate() {
        isActive = true;
        declaredMethods.clear();
        usedVariables.clear();
        stackVariables.clear();
        referencedTypes.clear();
        referencedFields.clear();
        methodDefinitions.clear();
        places.clear();
        returnLabels.clear();
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
    
    /**
     * returns the place name for a place inside a method
     * @param methodName the name of the method we are currently in
     * @param atBegin states if we are currently at the begin of the method or 
     * if we should be a place for some loop or other usable location in the code
     * @return
     */
    public static String buildPlace(String methodName, boolean atBegin){
        String placeName;
        if(atBegin){
            placeName = prefix(methodName+"_begin");
            lastPlace = placeName;
            places.add(placeName);
            return placeName;
        } else {
            // we have a loop or some other place inside the method, no method call
            for(int i = 0; i<Integer.MAX_VALUE; i++){
                placeName = prefix(methodName+i);
                if(!places.contains(placeName)){
                    places.add(placeName);
                    lastPlace = placeName;
                    return placeName;
                }
            }
            throw new RuntimeException("No possible places left for method "+methodName);
        }
    }
    
    public static String buildPlace(String methodName, String invocedMethod) {
        String placeName;
        for(int i=0; i<Integer.MAX_VALUE; i++){
            placeName = prefix(methodName + "_" + invocedMethod+i);
            if(!places.contains(placeName)){
                lastPlace = placeName;
                places.add(placeName);
                nextLabel = invocedMethod+i;
                returnLabels.add(prefix(methodName + "_" + nextLabel));
                return placeName;
            }
        }
        throw new RuntimeException("No possible places left for method invocation of "+ invocedMethod+" in method "+methodName);
    }
}

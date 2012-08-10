package de.unikl.bcverifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unikl.bcverifier.specification.LocalPlaceDefinitions;
import de.unikl.bcverifier.specification.Place;

import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.JClassType;
import b2bpl.translation.ITranslationConstants;

public class TranslationController implements ITranslationConstants {
    public static final String LABEL_PREFIX1 = "lib1_";
    public static final String LABEL_PREFIX2 = "lib2_";
    public static final String DISPATCH_LABEL1 = "dispatch1";
    public static final String DISPATCH_LABEL2 = "dispatch2";
    public static final String VERIFY_LABEL = "check";
    public static final String CHECK_LABEL1 = DISPATCH_LABEL2;
    public static final String CHECK_LABEL2 = VERIFY_LABEL;
    public static final String CONSTRUCTOR_LABEL1 = LABEL_PREFIX2 + CONSTRUCTOR_TABLE_LABEL;
    public static final String CONSTRUCTOR_LABEL2 = VERIFY_LABEL;
    
    private boolean isActive = false;
    private Configuration config;
    private int round;

    private Set<String> declaredMethods = new HashSet<String>();
    private Map<String, BPLVariable> usedVariables = new HashMap<String, BPLVariable>();
    private Map<String, BPLVariable> stackVariables = new HashMap<String, BPLVariable>();
    private HashSet<JClassType> referencedTypes = new HashSet<JClassType>();
    private HashSet<BCField> referencedFields = new HashSet<BCField>();
    private HashSet<String> places = new HashSet<String>();
    private HashMap<String, Set<String>> methodDefinitions = new HashMap<String, Set<String>>();
    private Set<String> returnLabels = new HashSet<String>();
    public int maxLocals;
    public int maxStack;
    
    private String lastPlace = null;
    private String nextLabel = null;
    private LocalPlaceDefinitions localPlaceDefinitions;
    private Set<String> localPlaces = new HashSet<String>();
    
    public Set<String> declaredMethods() {
        return declaredMethods;
    }
    
    public Map<String, BPLVariable> usedVariables(){
        return usedVariables;
    }
    
    public Map<String, BPLVariable> stackVariables() {
        return stackVariables;
    }
    
    public HashSet<JClassType> referencedTypes() {
        return referencedTypes;
    }
    
    public HashSet<BCField> referencedFields() {
        return referencedFields;
    }
    
    public HashMap<String, Set<String>> methodDefinitions() {
        return methodDefinitions;
    }
    
    public Set<String> returnLabels() {
        return returnLabels;
    }
    
    public void resetReturnLabels() {
        returnLabels.clear();
    }
    
    public void resetLocalPlaces() {
        localPlaces.clear();
    }
    
    public void definesMethod(String className, String methodName) {
        Set<String> definedMethods = methodDefinitions.get(className);
        if(definedMethods==null){
            definedMethods = new HashSet<String>();
            methodDefinitions.put(className, definedMethods);
        }
        definedMethods.add(methodName);
    }
    
    public void definesNoMethods(String className){
        methodDefinitions.put(className, Collections.<String> emptySet());
    }
    
    public HashSet<String> places() {
        return places;
    }
    
    public String nextLabel() {
        return nextLabel;
    }
    
    public void activate() {
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
    
    public void deactivate() {
        isActive = false;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void enterRound1() {
        round = 1;
    }
    
    public void enterRound2() {
        round = 2;
    }
    
    public void setConfig(Configuration c) {
        config = c;
    }
    
    public Configuration getConfig() {
        return config;
    }
    
    public String getHeap() {
        switch(round) {
        case 1:
            return HEAP1;
        case 2:
            return HEAP2;
        default:
            return "heap";
        }
    }
    
    public String getStack(){
        switch(round){
        case 1:
            return STACK1;
        case 2:
            return STACK2;
        default:
            return "stack";
        }
    }
    
    public String getStackPointerMap() {
        switch(round){
        case 1:
            return SP_MAP1_VAR;
        case 2:
            return SP_MAP2_VAR;
        default:
            return "spmap";
        }
    }
    
    public String getInteractionFramePointer() {
        switch(round){
        case 1:
            return IP1_VAR;
        case 2:
            return IP2_VAR;
        default:
            return "ip";
        }
    }
    
    public String prefix(String label) {
        switch(round){
        case 1:
            return LABEL_PREFIX1+label;
        case 2:
            return LABEL_PREFIX2+label;
        default:
            return label;
        }
    }
    
    public String getDispatchLabel() {
        switch(round){
        case 1:
            return DISPATCH_LABEL1;
        case 2:
            return DISPATCH_LABEL2;
        default:
            return "dispatch";
        }
    }
    
    public String getCheckLabel() {
        switch(round){
        case 1:
            return CHECK_LABEL1;
        case 2:
            return CHECK_LABEL2;
        default:
            return "check";
        }
    }
    
    public String getNextConstructorLabel(){
        switch(round){
        case 1:
            return CONSTRUCTOR_LABEL1;
        case 2:
            return CONSTRUCTOR_LABEL2;
        default:
            return CONSTRUCTOR_TABLE_LABEL;
        }
    }
    
    public String getStallMap() {
        switch(round) {
        case 1:
            return STALL1;
        case 2:
            return STALL2;
        default:
            return "stall";
        }
    }
    
    public String getOldPlaceVar() {
        switch(round) {
        case 1:
            return OLD_PLACE1;
        case 2:
            return OLD_PLACE2;
        default:
            return "old_place";
        }
    }
    
    public boolean isRound2() {
        return round == 2;
    }
    
    public List<Place> getLocalPlacesBetween(int line1, int line2){
        if(localPlaceDefinitions == null)
            return null;
        
        switch(round){
        case 1:
            return localPlaceDefinitions.getPlaceInOld(line1, line2);
        case 2:
            return localPlaceDefinitions.getPlaceInNew(line1, line2);
        default:
            return null;
        }
    }
    
    public void addLocalPlace(String localPlace){
        localPlaces.add(localPlace);
    }
    
    public Set<String> getLocalPlaces(){
        return localPlaces;
    }
    
    /**
     * returns the place name for a place inside a method
     * @param methodName the name of the method we are currently in
     * @param atBegin states if we are currently at the begin of the method or 
     * if we should be a place for some loop or other usable location in the code
     * @return
     */
    public String buildPlace(String methodName, boolean atBegin){
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
    
    public String buildPlace(String methodName, String invocedMethod) {
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

    public void setLocalPlaces(LocalPlaceDefinitions localPlaces) {
        this.localPlaceDefinitions = localPlaces;
    }
}

package de.unikl.bcverifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unikl.bcverifier.TranslationController.BoogiePlace;
import de.unikl.bcverifier.specification.LocalPlaceDefinitions;
import de.unikl.bcverifier.specification.Place;

import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bytecode.BCField;
import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.JClassType;
import b2bpl.translation.ITranslationConstants;
import b2bpl.translation.MethodTranslator;

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
    private HashSet<JClassType> globalReferencedTypes = new HashSet<JClassType>();
    private HashSet<BCField> globalReferencedFields = new HashSet<BCField>();
    private HashSet<BoogiePlace> places = new HashSet<BoogiePlace>();
    private HashMap<String, Set<String>> methodDefinitions = new HashMap<String, Set<String>>();
    private Set<String> returnLabels = new HashSet<String>();
    public int maxLocals;
	public int maxStack;
    public int maxParams;
    
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
    
    public HashSet<JClassType> localReferencedTypes() {
        return referencedTypes;
    }
    
    public HashSet<BCField> localReferencedFields() {
        return referencedFields;
    }
    
    public HashSet<JClassType> globalReferencedTypes() {
        return globalReferencedTypes;
    }
    
    public HashSet<BCField> globalReferencedFields() {
        return globalReferencedFields;
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
    
    public HashSet<BoogiePlace> places() {
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
    
    public String getImpl() {
        switch(round) {
        case 1:
            return IMPL1;
        case 2:
            return IMPL2;
        default:
            return "lib1";
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
    public String buildPlace(BCMethod method, boolean atBegin){
        String placeName;
        String methodName = MethodTranslator.getProcedureName(method);
        if(atBegin){
            placeName = prefix(methodName+"_begin");
            places.add(new BoogiePlace(placeName, method));
            return placeName;
        } else {
            // we have a loop or some other place inside the method, no method call
            for(int i = 0; i<Integer.MAX_VALUE; i++){
                placeName = prefix(methodName+"_"+i);
                BoogiePlace bp = new BoogiePlace(placeName, method);
                if(!places.contains(bp)){
                    places.add(bp);
                    return placeName;
                }
            }
            throw new RuntimeException("No possible places left for method "+methodName);
        }
    }
    
    public String buildPlace(BCMethod method, String invocedMethod) {
        String placeName;
        String methodName = MethodTranslator.getProcedureName(method);
        for(int i=0; i<Integer.MAX_VALUE; i++){
            placeName = prefix(methodName + "_" + invocedMethod+"_"+i);
            BoogiePlace bp = new BoogiePlace(placeName, method);
            if(!places.contains(bp)){
                places.add(bp);
                nextLabel = invocedMethod+"_"+i;
                returnLabels.add(prefix(methodName + "_" + nextLabel));
                return placeName;
            }
        }
        throw new RuntimeException("No possible places left for method invocation of "+ invocedMethod+" in method "+methodName);
    }

    public void setLocalPlaces(LocalPlaceDefinitions localPlaces) {
        this.localPlaceDefinitions = localPlaces;
    }
    
    public LocalPlaceDefinitions getLocalPlaceDefinitions() {
        return localPlaceDefinitions;
    }

	public String boogieFieldName(BCField field) {
		return GLOBAL_VAR_PREFIX+field.getQualifiedName(); //TODO add type information to make field name unambiguous?
	}
	
	
	public static class BoogiePlace {
		public BoogiePlace(String name, BCMethod method) {
			super();
			this.name = name;
			this.method = method;
		}
		public BoogiePlace(String name, BCMethod method, boolean b) {
			this(name, method);
			setLocal(b);
		}
		private String name;
		private boolean local;
		private BCMethod method;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public BCMethod getMethod() {
			return method;
		}
		public void setMethod(BCMethod method) {
			this.method = method;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BoogiePlace other = (BoogiePlace) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		public boolean isLocal() {
			return local;
		}
		public void setLocal(boolean local) {
			this.local = local;
		}
		
	}


	public void addPlace(BoogiePlace bp) {
		places.add(bp);
	}
}

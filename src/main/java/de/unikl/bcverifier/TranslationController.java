package de.unikl.bcverifier;

public class TranslationController {
    private static int round;
    private static final String HEAP1 = "heap1";
    private static final String HEAP2 = "heap2";
    private static final String STACK1 = "stack1";
    private static final String STACK2 = "stack2";
    private static final String SP1 = "sp1";
    private static final String SP2 = "sp2";
    private static final String LABEL_PREFIX1 = "round1_";
    private static final String LABEL_PREFIX2 = "round2_";
    
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
}

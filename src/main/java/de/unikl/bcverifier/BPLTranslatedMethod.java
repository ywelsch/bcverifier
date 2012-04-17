package de.unikl.bcverifier;

import java.util.List;

import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLProcedure;

public class BPLTranslatedMethod {
    private BPLProcedure procedure;
    private List<BPLDeclaration> neededDeclarations;
    
    public BPLTranslatedMethod(BPLProcedure procedure,
            List<BPLDeclaration> neededDeclarations) {
        super();
        this.procedure = procedure;
        this.neededDeclarations = neededDeclarations;
    }
    
    public BPLProcedure getProcedure() {
        return procedure;
    }
    
    public List<BPLDeclaration> getNeededDeclarations() {
        return neededDeclarations;
    }
}

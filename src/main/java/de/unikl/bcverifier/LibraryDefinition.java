package de.unikl.bcverifier;

import java.util.List;

import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLDeclaration;

public class LibraryDefinition {
    List<BPLDeclaration> neededDeclarations;
    List<BPLBasicBlock> methodBlocks;
    
    public LibraryDefinition(List<BPLDeclaration> neededDeclarations,
            List<BPLBasicBlock> methodBlocks) {
        super();
        this.neededDeclarations = neededDeclarations;
        this.methodBlocks = methodBlocks;
    }

    public List<BPLDeclaration> getNeededDeclarations() {
        return neededDeclarations;
    }

    public List<BPLBasicBlock> getMethodBlocks() {
        return methodBlocks;
    }
}

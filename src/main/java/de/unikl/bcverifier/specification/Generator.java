package de.unikl.bcverifier.specification;

import java.util.List;

import b2bpl.bpl.ast.BPLVariableDeclaration;


public interface Generator {
    public List<SpecExpr> generateInvariant() throws GenerationException;
    public List<SpecExpr> generateLocalInvariant() throws GenerationException;
    public List<String> generatePreconditions() throws GenerationException;
    public LocalPlaceDefinitions generateLocalPlaces() throws GenerationException;
    public List<String> generatePreludeAddition() throws GenerationException;
    public List<VariableDef> generateVars() throws GenerationException;
    public List<String> generateGlobalAssignments() throws GenerationException;
    public List<String> generateInitialAssignments() throws GenerationException;
    public List<BPLVariableDeclaration> generateGlobalVariables() throws GenerationException;
}

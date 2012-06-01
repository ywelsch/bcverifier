package de.unikl.bcverifier.bpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b2bpl.bpl.EmptyBPLVisitor;
import b2bpl.bpl.ast.BPLArrayAssignment;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssignmentCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLAxiom;
import b2bpl.bpl.ast.BPLBasicBlock;
import b2bpl.bpl.ast.BPLBinaryArithmeticExpression;
import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLDeclaration;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLFunctionApplication;
import b2bpl.bpl.ast.BPLHavocCommand;
import b2bpl.bpl.ast.BPLImplementation;
import b2bpl.bpl.ast.BPLImplementationBody;
import b2bpl.bpl.ast.BPLLogicalNotExpression;
import b2bpl.bpl.ast.BPLOldExpression;
import b2bpl.bpl.ast.BPLPartialOrderExpression;
import b2bpl.bpl.ast.BPLProcedure;
import b2bpl.bpl.ast.BPLProgram;
import b2bpl.bpl.ast.BPLQuantifierExpression;
import b2bpl.bpl.ast.BPLRawCommand;
import b2bpl.bpl.ast.BPLTrigger;
import b2bpl.bpl.ast.BPLUnaryMinusExpression;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableExpression;

public class UsedVariableFinder extends EmptyBPLVisitor<Object> {
    private Map<String, BPLVariable> usedVariables;
    private Map<String, BPLVariable> variablesToBeFound;
    
    public Map<String, BPLVariable> getUsedVariables() {
        return usedVariables;
    }

    public UsedVariableFinder(Map<String, BPLVariable> variablesToBeFound) {
        this.variablesToBeFound = variablesToBeFound;
        usedVariables = new HashMap<String, BPLVariable>();
    }
    
    public Map<String, BPLVariable> findUsedVariables(List<BPLBasicBlock> blocks, List<BPLDeclaration> programDecls){
        for (BPLBasicBlock block : blocks) {
            block.accept(this);
        }
        for (BPLDeclaration decl : programDecls) {
            decl.accept(this);
        }
        return usedVariables;
    }
    
    @Override
    public Object visitProgram(BPLProgram program) {
        for (BPLDeclaration decl : program.getDeclarations()) {
            decl.accept(this);
        }
        return super.visitProgram(program);
    }
    
    @Override
    public Object visitArrayAssignment(BPLArrayAssignment bplArrayAssignment) {
        for (BPLExpression index : bplArrayAssignment.getIndices()) {
            index.accept(this);
        }
        bplArrayAssignment.getRight().accept(this);
        return super.visitArrayAssignment(bplArrayAssignment);
    }
    
    @Override
    public Object visitArrayExpression(BPLArrayExpression expr) {
        expr.getPrefix().accept(this);
        for (BPLExpression accessor : expr.getAccessors()) {
            accessor.accept(this);
        }
        return super.visitArrayExpression(expr);
    }
    
    @Override
    public Object visitAssertCommand(BPLAssertCommand command) {
        command.getExpression().accept(this);
        return super.visitAssertCommand(command);
    }
    
    @Override
    public Object visitAssignmentCommand(BPLAssignmentCommand command) {
        command.getLeft().accept(this);
        command.getRight().accept(this);
        return super.visitAssignmentCommand(command);
    }
    
    @Override
    public Object visitAssumeCommand(BPLAssumeCommand command) {
        command.getExpression().accept(this);
        return super.visitAssumeCommand(command);
    }
    
    @Override
    public Object visitAxiom(BPLAxiom axiom) {
        axiom.getExpression().accept(this);
        return super.visitAxiom(axiom);
    }
    
    @Override
    public Object visitBasicBlock(BPLBasicBlock block) {
        for (BPLCommand cmd : block.getCommands()) {
            cmd.accept(this);
        }
        return super.visitBasicBlock(block);
    }
    
    @Override
    public Object visitBinaryArithmeticExpression(
            BPLBinaryArithmeticExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return super.visitBinaryArithmeticExpression(expr);
    }
    
    @Override
    public Object visitBinaryLogicalExpression(BPLBinaryLogicalExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return super.visitBinaryLogicalExpression(expr);
    }
    
    @Override
    public Object visitEqualityExpression(BPLEqualityExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return super.visitEqualityExpression(expr);
    }
    
    @Override
    public Object visitFunctionApplication(BPLFunctionApplication expr) {
        for (BPLExpression arg : expr.getArguments()) {
            arg.accept(this);
        }
        return super.visitFunctionApplication(expr);
    }
    
    @Override
    public Object visitHavocCommand(BPLHavocCommand command) {
        //maybe not needed
        for (BPLVariableExpression var : command.getVariables()) {
            var.accept(this);
        }
        return super.visitHavocCommand(command);
    }
    
    @Override
    public Object visitImplementation(BPLImplementation implementation) {
        implementation.getBody().accept(this);
        return super.visitImplementation(implementation);
    }
    
    @Override
    public Object visitImplementationBody(BPLImplementationBody body) {
        for (BPLBasicBlock bb : body.getBasicBlocks()) {
            bb.accept(this);
        }
        return super.visitImplementationBody(body);
    }
    
    @Override
    public Object visitLogicalNotExpression(BPLLogicalNotExpression expr) {
        expr.getExpression().accept(this);
        return super.visitLogicalNotExpression(expr);
    }
    
    @Override
    public Object visitOldExpression(BPLOldExpression expr) {
        //maybe not needed
        expr.getExpression().accept(this);
        return super.visitOldExpression(expr);
    }
    
    @Override
    public Object visitPartialOrderExpression(BPLPartialOrderExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return super.visitPartialOrderExpression(expr);
    }
    
    @Override
    public Object visitProcedure(BPLProcedure procedure) {
        procedure.getImplementation().accept(this);
        return super.visitProcedure(procedure);
    }
    
    @Override
    public Object visitQuantifierExpression(BPLQuantifierExpression expr) {
        expr.getExpression().accept(this);
        for (BPLTrigger trigger : expr.getTriggers()) {
            if(trigger != null)
                trigger.accept(this);
        }
        return super.visitQuantifierExpression(expr);
    }
    
    @Override
    public Object visitRawCommand(BPLRawCommand bplRawCommand) {
        //TODO implement with regexp
        return super.visitRawCommand(bplRawCommand);
    }
    
    @Override
    public Object visitTrigger(BPLTrigger trigger) {
        for (BPLExpression exp : trigger.getExpressions()) {
            exp.accept(this);
        }
        return super.visitTrigger(trigger);
    }
    
    @Override
    public Object visitVariableExpression(BPLVariableExpression expr) {
        String varName = expr.getIdentifier();
        if(variablesToBeFound.containsKey(varName)){
            usedVariables.put(varName, variablesToBeFound.get(varName));
        }
        return super.visitVariableExpression(expr);
    }
    
    @Override
    public Object visitUnaryMinusExpression(BPLUnaryMinusExpression expr) {
        expr.getExpression().accept(this);
        return super.visitUnaryMinusExpression(expr);
    }
}

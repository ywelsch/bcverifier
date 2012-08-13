package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public abstract class StatementVisitor extends ASTVisitor {

	abstract boolean visitStatement(Statement s);
	
	@Override
	public boolean visit(AssertStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(Block s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(BreakStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ConstructorInvocation s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ContinueStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(DoStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(EmptyStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(EnhancedForStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ExpressionStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ForStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(IfStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(LabeledStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ReturnStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(SuperConstructorInvocation s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(SwitchCase s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(SynchronizedStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(ThrowStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(TryStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(TypeDeclarationStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(VariableDeclarationStatement s) {
		return visitStatement(s);
	}
	@Override
	public boolean visit(WhileStatement s) {
		return visitStatement(s);
	}
}

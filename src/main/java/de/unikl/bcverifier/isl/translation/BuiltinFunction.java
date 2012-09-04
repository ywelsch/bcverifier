package de.unikl.bcverifier.isl.translation;

import java.util.ArrayList;
import java.util.List;

import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLExpression;

import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.checking.types.ExprType;


public abstract class BuiltinFunction extends Def {

	private final ExprType returnType;
	private final List<ExprType> parameterTypes;
	private final String name;
	
	public BuiltinFunction(String name, ExprType returnType, List<ExprType> parameterTypes) {
		this.name = name;
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}
	
	public BuiltinFunction(String name, ExprType returnType, ExprType ... parameterTypes) {
		this.name = name;
		this.returnType = returnType;
		this.parameterTypes = new ArrayList<ExprType>();
		for (ExprType p : parameterTypes) {
			this.parameterTypes.add(p);
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public String attrName() {
		return name;
	}

	@Override
	public ExprType attrType() {
		return returnType;
	}
	
	public ExprType exactType(de.unikl.bcverifier.isl.ast.List<Expr> arguments) {
		return attrType();
	}

	public List<ExprType> getParameterTypes() {
		return parameterTypes;
	}

	public abstract BPLExpression translateCall(de.unikl.bcverifier.isl.ast.List<Expr> arguments);
	
	public BPLExpression translateWelldefinedness(de.unikl.bcverifier.isl.ast.List<Expr> arguments) {
		return BPLBoolLiteral.TRUE;
	}

}

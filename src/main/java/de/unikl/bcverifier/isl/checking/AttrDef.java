package de.unikl.bcverifier.isl.checking;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.UnknownDef;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;

public class AttrDef {

	public static Def getVarDef(VarAccess varAccess) {
		String varName = varAccess.getName().getName();
		Collection<Def> defs = varAccess.lookup(varName);
		if (defs.isEmpty()) {
			varAccess.addError("Could not find variable " + varName + ".");
		} else if (defs.size() > 1) {
			varAccess.addError("Access to variable " + varName + " is ambiguous.");
		}
		for (Def def : defs) {
			return def;
		}
		return new UnknownDef();
	}
	
	public static Def getFuncDef(FuncCall funcCall) {
		String funcName = funcCall.getFuncName().getName();
		Collection<Def> defs = funcCall.lookup(funcName);
		BuiltinFunction result = null;
		ArrayList<ExprType> argumentTypes = Lists.newArrayList();
		for (Expr arg : funcCall.getArguments()) {
			argumentTypes.add(arg.attrType());
		}
		for (Def def : defs) {
			if (def instanceof BuiltinFunction) {
				BuiltinFunction f = (BuiltinFunction) def;
				if (isBetterOverload(argumentTypes, f, result)) { 
					result = f;
				}
			}
		}
		if (result == null) {
			funcCall.addError("Could not find function with name " + funcName + ".");
			return new UnknownDef();
		}
		return result;
	}
	
	/**
	 * checks whether f is better suited for the given argument types than g  
	 */
	private static boolean isBetterOverload(ArrayList<ExprType> argTypes, BuiltinFunction f, BuiltinFunction g) {
		if (g == null) {
			return true;
		}
		if (Math.abs(argTypes.size() - f.getParameterTypes().size()) < Math.abs(argTypes.size() - g.getParameterTypes().size())) {
			// number of parameters matches better
			return true;
		}
		if (countArgParamMatches(argTypes, f.getParameterTypes()) > countArgParamMatches(argTypes, g.getParameterTypes())) {
			// parameter types match better
			return true;
		}
		// ... could also check which function is more specific in terms of subtypes
		return false;
	}
	
	private static int countArgParamMatches(ArrayList<ExprType> argTypes, java.util.List<ExprType> parameterTypes) {
		int count = 0;
		for (int i=0; i<Math.min(argTypes.size(), parameterTypes.size()); i++) {
			if (argTypes.get(i).isSubtypeOf(parameterTypes.get(i))) {
				count++;
			}
		}
		return count;
	}
}

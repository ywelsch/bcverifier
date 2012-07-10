package de.unikl.bcverifier.isl.ast.translation;

import java.util.HashMap;
import java.util.Map;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;

import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;

public class BuiltinFunctions {
	
	private static Map<String, BuiltinFunction> funcs = null;
	
	public static BuiltinFunction get(String name) {
		if (funcs == null) {
			initFuncs();
		}
		return funcs.get(name);
	}

	private static void initFuncs() {
		funcs = new HashMap<String, BuiltinFunction>();
		addFunc(new BuiltinFunction("exposed", ExprTypeBool.instance(), JavaType.object()) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "exposed");
			}

			
			
		});
		addFunc(new BuiltinFunction("createdByCtxt", ExprTypeBool.instance(), JavaType.object()) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "createdByCtxt");
			}
			
		});
	}

	private static void addFunc(BuiltinFunction f) {
		funcs.put(f.getName(), f);
	}
	
	private static BPLExpression heapProperty(Expr obj, String property) {
		JavaType t = (JavaType) obj.attrType();
		return new BPLArrayExpression(
				ExprTranslation.getHeap(t.getVersion()), 
				obj.translateExpr(), 
				new BPLVariableExpression(property));
	}
}

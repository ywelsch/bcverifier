package de.unikl.bcverifier.isl.ast.translation;

import java.util.HashMap;
import java.util.Map;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.JavaType;

public class BuiltinFunctions {
	
	private Map<String, BuiltinFunction> funcs = null;
	private TwoLibraryModel twoLibraryModel;
	
	public BuiltinFunctions(TwoLibraryModel twoLibraryModel) {
		this.twoLibraryModel = twoLibraryModel;
	}
	
	public BuiltinFunction get(String name) {
		if (funcs == null) {
			initFuncs();
		}
		return funcs.get(name);
	}

	private void initFuncs() {
		funcs = new HashMap<String, BuiltinFunction>();
		addFunc(new BuiltinFunction("exposed", ExprTypeBool.instance(), JavaType.object(twoLibraryModel)) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "exposed");
			}

			
			
		});
		addFunc(new BuiltinFunction("createdByCtxt", ExprTypeBool.instance(), JavaType.object(twoLibraryModel)) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "createdByCtxt");
			}
			
		});
	}

	private void addFunc(BuiltinFunction f) {
		funcs.put(f.getName(), f);
	}
	
	private BPLExpression heapProperty(Expr obj, String property) {
		JavaType t = (JavaType) obj.attrType();
		return new BPLArrayExpression(
				ExprTranslation.getHeap(t.getVersion()), 
				obj.translateExpr(), 
				new BPLVariableExpression(property));
	}
}

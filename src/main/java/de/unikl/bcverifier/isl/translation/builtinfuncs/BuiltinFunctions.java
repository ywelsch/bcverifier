package de.unikl.bcverifier.isl.translation.builtinfuncs;

import java.util.HashMap;
import java.util.Map;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAny;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.isl.translation.ExprTranslation;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class BuiltinFunctions {
	
	Map<String, BuiltinFunction> funcs = null;
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
		// bool exposed(Object o)
		addFunc(new BuiltinFuncExposed(twoLibraryModel));
		// bool createdByCtxt(Object o)
		addFunc(new BuiltinFuncCreatedByCtxt(twoLibraryModel));
		// bool at(Place p, int stackPointer)
		addFunc(new BuiltinFuncAt());
		// T stack(Place p, int stackPointer, T expr)
		addFunc(new BuiltinFuncStack(this));
		// int sp1()
		addFunc(new BuiltinFuncSp1());
		// int sp2()
		addFunc(new BuiltinFuncSp2());
	}

	private void addFunc(BuiltinFunction f) {
		funcs.put(f.getName(), f);
	}
	
	static BPLExpression heapProperty(Expr obj, String property) {
		JavaType t = (JavaType) obj.attrType();
		return new BPLArrayExpression(
				ExprTranslation.getHeap(t.getVersion()), 
				obj.translateExpr(), 
				new BPLVariableExpression(property));
	}
	
	public static BPLExpression stackProperty(Version version,
			BPLExpression stackPointer,
			BPLExpression property) {
		// stack1[ip][stackPointer][property]
		BPLExpression stack;
		BPLExpression ip;
		if (version == Version.OLD) {
			stack = new BPLVariableExpression("stack1");
			ip = new BPLVariableExpression("ip1");
		} else {
			stack = new BPLVariableExpression("stack2");
			ip = new BPLVariableExpression("ip2");
		}
		// ((stack[ip])[sp])[property]
		return new BPLArrayExpression(
				new BPLArrayExpression(
					new BPLArrayExpression(stack, ip)
						, stackPointer)
							, property);
	}
}

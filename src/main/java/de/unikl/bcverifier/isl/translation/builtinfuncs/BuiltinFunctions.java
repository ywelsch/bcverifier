package de.unikl.bcverifier.isl.translation.builtinfuncs;

import java.util.Collection;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.translation.ExprTranslation;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class BuiltinFunctions {
	
	public static final BuiltinFunction FUNC_SP1 = new BuiltinFuncSp1();
	public static final BuiltinFunction FUNC_SP2 = new BuiltinFuncSp2();

	public final BuiltinFunction FUNC_AT_place_sp = new BuiltinFuncAt_place_sp();
	public final BuiltinFunction FUNC_AT_place = new BuiltinFuncAt_place();;
	
	Multimap<String, BuiltinFunction> funcs = null;
	private TwoLibraryModel twoLibraryModel;
	
	
	public BuiltinFunctions(TwoLibraryModel twoLibraryModel) {
		this.twoLibraryModel = twoLibraryModel;
	}
	
	public Collection<BuiltinFunction> get(String name) {
		if (funcs == null) {
			initFuncs();
		}
		return funcs.get(name);
	}

	private void initFuncs() {
		funcs = ArrayListMultimap.create();
		// bool exposed(Object o)
		addFunc(new BuiltinFuncExposed(twoLibraryModel));
		// bool createdByCtxt(Object o)
		addFunc(new BuiltinFuncCreatedByCtxt(twoLibraryModel));
		// bool at(Place p, int stackPointer)
		addFunc(FUNC_AT_place_sp);
		// bool at(Place p)
		addFunc(FUNC_AT_place);
		// T stack(Place p, int stackPointer, T expr)
		addFunc(new BuiltinFuncStack_place_sp(this));
		// T stack(Place p, T expr)
		addFunc(new BuiltinFuncStack_place(this));
		// int sp1()
		addFunc(FUNC_SP1);
		// int sp2()
		addFunc(FUNC_SP2);
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
	
	public static BPLExpression stackProperty(boolean isGlobalInv, Version version,
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
		if (isGlobalInv) {
			ip = new BPLVariableExpression("iframe");
		}
		// ((stack[ip])[sp])[property]
		return new BPLArrayExpression(
				new BPLArrayExpression(
					new BPLArrayExpression(stack, ip)
						, stackPointer)
							, property);
	}

	public static BPLArrayExpression getCurrentSp(boolean isGlobalInv, Version version) {
		String spmap;
		String ip;
		if (version == Version.OLD) {
			spmap = "spmap1";
			ip = "ip1";
			return new BPLArrayExpression(new BPLVariableExpression("spmap1"), new BPLVariableExpression("ip1"));
		} else {
			spmap = "spmap2";
			ip = "ip2";
		}
		if (isGlobalInv) {
			// use "iframe" in global invariant
			ip = "iframe";
		}		
		return new BPLArrayExpression(new BPLVariableExpression(spmap), new BPLVariableExpression(ip));
	}
}

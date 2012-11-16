package de.unikl.bcverifier.isl.translation.builtinfuncs;

import java.util.Collection;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.translation.ExprTranslation;
import de.unikl.bcverifier.isl.translation.Phase;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class BuiltinFunctions {
	
	public static final BuiltinFunction FUNC_STACKINDEX = new BuiltinFuncStackIndex();

	public final BuiltinFunction FUNC_AT_place_sp = new BuiltinFuncAt_place_sp();
	public final BuiltinFunction FUNC_AT_place = new BuiltinFuncAt_place();
	
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
		addFunc(new BuiltinFuncCreatedByLibrary(twoLibraryModel));
		// bool at(Place p, int stackPointer)
		addFunc(FUNC_AT_place_sp);
		// bool at(Place p)
		addFunc(FUNC_AT_place);
		// T stack(Place p, int stackPointer, T expr)
		addFunc(new BuiltinFuncEval_place_sp(this));
		// T stack(Place p, T expr)
		addFunc(new BuiltinFuncEval_place(this));
		// int stackIndex(version v)
		addFunc(FUNC_STACKINDEX);
		// bool related(Bijection b, Object o1, Object o2)
		addFunc(new BuiltinFuncRelated(twoLibraryModel));
	}

	private void addFunc(BuiltinFunction f) {
		funcs.put(f.getName(), f);
	}
	
	static BPLExpression heapProperty(Expr obj, String property) {
		JavaType t = (JavaType) obj.attrType();
		return new BPLArrayExpression(
				ExprTranslation.getHeap(t.getVersion(), obj.attrCompilationUnit().getPhase()), 
				obj.translateExpr(), 
				new BPLVariableExpression(property));
	}
	
	public static BPLExpression stackProperty(boolean isGlobalInv, Version version, Phase phase,
			BPLExpression stackPointer,
			BPLExpression property) {
		// stack1[ip][stackPointer][property]
		BPLExpression stack;
		BPLExpression ip;
		if (version == Version.OLD) {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.STACK1);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_STACK1);
			ip = new BPLVariableExpression(ITranslationConstants.IP1_VAR);
		} else {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.STACK2);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_STACK2);
			ip = new BPLVariableExpression(ITranslationConstants.IP2_VAR);
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

	public static BPLArrayExpression getCurrentSp(boolean isGlobalInv, Version version, Phase phase) {
		String spmap;
		String ip;
		if (version == Version.OLD) {
			if (phase == Phase.POST) 
				spmap = ITranslationConstants.SP_MAP1_VAR;
			else
				spmap = ITranslationConstants.OLD_SP_MAP1_VAR;
			ip = ITranslationConstants.IP1_VAR;
		} else {
			if (phase == Phase.POST)
				spmap = ITranslationConstants.SP_MAP2_VAR;
			else
				spmap = ITranslationConstants.OLD_SP_MAP2_VAR;
			ip = ITranslationConstants.IP2_VAR;
		}
		if (isGlobalInv) {
			// use "iframe" in global invariant
			ip = "iframe";
		}
		return new BPLArrayExpression(new BPLVariableExpression(spmap), new BPLVariableExpression(ip));
	}
}

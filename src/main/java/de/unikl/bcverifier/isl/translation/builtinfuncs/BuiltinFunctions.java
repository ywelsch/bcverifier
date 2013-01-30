package de.unikl.bcverifier.isl.translation.builtinfuncs;

import java.util.Collection;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.translation.ExprTranslation;
import de.unikl.bcverifier.isl.translation.Phase;
import de.unikl.bcverifier.isl.translation.TranslationHelper;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class BuiltinFunctions {
	
	public static final BuiltinFunction FUNC_STACKINDEX = new BuiltinFuncTopFrame1();

	public final BuiltinFunction FUNC_AT3 = new BuiltinFuncAt3();
	public final BuiltinFunction FUNC_AT2 = new BuiltinFuncAt2();
	public final BuiltinFunction FUNC_AT1 = new BuiltinFuncAt1();
	
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
		// bool at(Place p, int slice, int frame)
		addFunc(FUNC_AT3);
		// bool at(Place p, int slice)
		addFunc(FUNC_AT2);
		// bool at(Place p)
		addFunc(FUNC_AT1);
		// T stack(Place p, int slice, int frame, T expr)
		addFunc(new BuiltinFuncEval3(this));
		// T stack(Place p, int slice, T expr)
		addFunc(new BuiltinFuncEval2(this));
		// T stack(Place p, T expr)
		addFunc(new BuiltinFuncEval1(this));
		// int stackIndex(version v)
		addFunc(FUNC_STACKINDEX);
		// bool bijective(binrelation b)
		addFunc(new BuiltinFuncBijective());
		// binrelation empty()
		addFunc(new BuiltinFuncEmpty());
		// binrelation add(binrelation b, Object o1, Object o2)
		addFunc(new BuiltinFuncTertiaryBij(BuiltinFuncTertiaryBij.Name.ADD, twoLibraryModel));
		// binrelation remove(binrelation b, Object o1, Object o2)
		addFunc(new BuiltinFuncTertiaryBij(BuiltinFuncTertiaryBij.Name.REMOVE, twoLibraryModel));
		// bool related(binrelation b, Object o1, Object o2)
		addFunc(new BuiltinFuncTertiaryBij(BuiltinFuncTertiaryBij.Name.RELATED, twoLibraryModel));
		// int topFrame(version)
		addFunc(new BuiltinFuncTopFrame1());
		// int topFrame(version, int slice)
		addFunc(new BuiltinFuncTopFrame2());
		// int topSlice()
		addFunc(new BuiltinFuncTopSlice1());
		// int topSlice(version)
		addFunc(new BuiltinFuncTopSlice2());
		// bool librarySlice(int slice)
		addFunc(new BuiltinFuncLibrarySlice1());
		// bool librarySlice(version, int slice)
		addFunc(new BuiltinFuncLibrarySlice2());
	}

	private void addFunc(BuiltinFunction f) {
		funcs.put(f.getName(), f);
	}
	
	static BPLExpression heapProperty(Expr obj, String property) {
		ExprTypeJavaType t = (ExprTypeJavaType) obj.attrType();
		return new BPLArrayExpression(
				ExprTranslation.getHeap(t.getVersion(), obj.attrCompilationUnit().getPhase()), 
				obj.translateExpr(), 
				new BPLVariableExpression(property));
	}
	
	public static BPLExpression stackPropertyTopSlice(Version version, Phase phase,
			BPLExpression frame,
			BPLExpression property) {
		return stackProperty(version, phase, BuiltinFuncTopSlice2.getIpVar(version), frame, property);
	}
	
	public static BPLExpression stackProperty(Version version, Phase phase,
			BPLExpression slice,
			BPLExpression frame,
			BPLExpression property) {
		// stack1[slice][frame][property]
		BPLExpression stack = getStack(version, phase);
		// ((stack[slice])[frame])[property]
		return new BPLArrayExpression(
				new BPLArrayExpression(
					new BPLArrayExpression(stack, slice)
						, frame)
							, property);
	}

	public static BPLExpression getStack(Version version, Phase phase) {
		BPLExpression stack;
		if (version == Version.OLD) {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.STACK1);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_STACK1);
		} else {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.STACK2);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_STACK2);
		}
		return stack;
	}

	public static BPLArrayExpression getCurrentSpTopSlice(Version version, Phase phase) {
		return getCurrentSp(version, phase, BuiltinFuncTopSlice2.getIpVar(version));
	}
	
	public static BPLArrayExpression getCurrentSp(Version version, Phase phase, BPLExpression slice) {
		BPLExpression stack = getStack(version, phase);
		if (version == Version.OLD) {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.SP_MAP1_VAR);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_SP_MAP1_VAR);
		} else {
			if (phase == Phase.POST)
				stack = new BPLVariableExpression(ITranslationConstants.SP_MAP2_VAR);
			else
				stack = new BPLVariableExpression(ITranslationConstants.OLD_SP_MAP2_VAR);
		}
		return new BPLArrayExpression(stack, slice);
	}

	public static BPLExpression isValidFrame(Version version, Phase phase,
			BPLExpression slice, BPLExpression frame) {
		return TranslationHelper.conjunction(
				// 0 <= frame
				new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
						new BPLIntLiteral(0), frame),
				// && frame <= currentSp(splice)
				new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
				frame, 
				getCurrentSp(version, phase, slice)));
	}
}

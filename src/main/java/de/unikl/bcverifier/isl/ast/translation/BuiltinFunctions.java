package de.unikl.bcverifier.isl.ast.translation;

import java.util.HashMap;
import java.util.Map;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAny;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.PlaceType;

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
		// bool exposed(Object o)
		addFunc(new BuiltinFunction("exposed", ExprTypeBool.instance(), JavaType.object(twoLibraryModel)) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "exposed");
			}

			
			
		});
		// bool createdByCtxt(Object o)
		addFunc(new BuiltinFunction("createdByCtxt", ExprTypeBool.instance(), JavaType.object(twoLibraryModel)) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return heapProperty(arguments.getChild(0), "createdByCtxt");
			}
			
		});
		// bool at(Place p, int stackPointer)
		addFunc(new BuiltinFunction("at", ExprTypeBool.instance(), PlaceType.instance(), ExprTypeInt.instance()) {

			@Override
			public BPLExpression translateWelldefinedness(List<Expr> arguments) {
				Expr p = arguments.getChild(0);
				Expr stackPointer = arguments.getChild(1);
				BPLArrayExpression currentStackpointer;
				if (((PlaceType)p.attrType()).getVersion() == Version.OLD) {
					currentStackpointer= new BPLArrayExpression(new BPLVariableExpression("spmap1"), new BPLVariableExpression("ip1"));
				} else {
					currentStackpointer= new BPLArrayExpression(new BPLVariableExpression("spmap2"), new BPLVariableExpression("ip2"));
				}
				return ExprWellDefinedness.conjunction(
							new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
									new BPLIntLiteral(0), stackPointer.translateExpr()),
							new BPLRelationalExpression(BPLRelationalExpression.Operator.LESS_EQUAL, 
									stackPointer.translateExpr(), 
									currentStackpointer)
							 
						);
			}
			
			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				Expr p = arguments.getChild(0);
				Expr stackPointer = arguments.getChild(1);
				PlaceType placeType = (PlaceType) p.attrType();
				// stack1[libip(ip1)][stackPointer][place] == p
				return new BPLEqualityExpression(BPLEqualityExpression.Operator.EQUALS, 
						stackProperty(placeType.getVersion(), stackPointer.translateExpr(), new BPLVariableExpression("place"))
						, p.translateExpr()
						);
			}
		});
		
		// T stack(Place p, int stackPointer, T expr)
		addFunc(new BuiltinFunction("stack", ExprTypeAny.instance(), PlaceType.instance(), ExprTypeInt.instance(), ExprTypeAny.instance()) {

			@Override
			public BPLExpression translateWelldefinedness(List<Expr> arguments) {
				return ExprWellDefinedness.conjunction(
						funcs.get("at").translateWelldefinedness(arguments),
						funcs.get("at").translateCall(arguments));
			}
			
			@Override
			public ExprType exactType(List<Expr> arguments) {
				return arguments.getChild(2).attrType();
			}
			
			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				Expr p = arguments.getChild(0);
				Expr stackPointer = arguments.getChild(1);
				Expr exp = arguments.getChild(2);
				PlaceType placeType = (PlaceType) p.attrType();
				// stack1[libip(ip1)][stackPointer][v]
				return stackProperty(placeType.getVersion(), stackPointer.translateExpr(), 
						exp.translateExpr());
			}
		});
		
		
		// int sp1()
		addFunc(new BuiltinFunction("sp1", ExprTypeInt.instance()) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return new BPLArrayExpression(
						new BPLVariableExpression("spmap1"), 
						new BPLVariableExpression("ip1")) ;
			}
		});
		
		// int sp2()
		addFunc(new BuiltinFunction("sp2", ExprTypeInt.instance()) {

			@Override
			public BPLExpression translateCall(List<Expr> arguments) {
				return new BPLArrayExpression(
						new BPLVariableExpression("spmap2"), 
						new BPLVariableExpression("ip2")) ;
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
	
	private BPLExpression stackProperty(Version version,
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

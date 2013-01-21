package de.unikl.bcverifier.isl.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import b2bpl.bytecode.JClassType;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.isl.ast.Assign;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.CallProgramPoint;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.ExprTypeRef;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.GlobVarDef;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.InstanceofOperation;
import de.unikl.bcverifier.isl.ast.LineNrProgramPoint;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NamedTypeDef;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.ProgramPoint;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.UnknownDef;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.VersionConst;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAtLineProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeCallProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeHasMembers;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaPackageRef;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaTypeRef;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.checking.types.ExprTypeProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeUnknown;
import de.unikl.bcverifier.isl.checking.types.ExprTypeVersion;
import de.unikl.bcverifier.isl.translation.Translation;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFuncEval_place;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class TypeHelper {

	public static ExprType attrType(NamedTypeDef t) {
		Version version = t.getVersion();
		String qualifiedName = getQualifiedName(t.getNames());
		if (version.equals(Version.BOTH) && t.getNameList().getNumChild() == 1) {
			String typeName = t.getName(0).getName();
			if (typeName.equals(BinRelationType.instance().toString())) {
				return BinRelationType.instance();
			} else if (typeName.equals(ExprTypeInt.instance().toString())) {
				return ExprTypeInt.instance();
			} else if (typeName.equals(ExprTypeBool.instance().toString())) {
				return ExprTypeBool.instance();
			}
		}
		return ExprTypeJavaType.create(t, version, qualifiedName);
	}

	private static String getQualifiedName(List<Ident> names) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Ident i : names) {
			if (!first) {
				sb.append(".");
			}
			first = false;
			sb.append(i.getName());
		}
		return sb.toString();
	}

	public static ExprType attrType(BinaryOperation bo) {
		TwoLibraryModel env = bo.attrCompilationUnit().getTwoLibraryModel();

		Expr left = bo.getLeft();
		Expr right = bo.getRight();
		switch (bo.getOperator()) {
		case AND:
		case OR:
		case IMPLIES:
		case IFF:
			checkIfSubtype(left, ExprTypeBool.instance());
			checkIfSubtype(right, ExprTypeBool.instance());
			return ExprTypeBool.instance();
		case RELATED:
			checkIfSubtype(left, ExprTypeJavaType.getJavaLangObject(env, Version.OLD));
			checkIfSubtype(right, ExprTypeJavaType.getJavaLangObject(env, Version.NEW));
			return ExprTypeBool.instance();
		case UNEQUALS:
		case EQUALS:
			if (!left.attrType().getClass().equals(right.attrType().getClass())) {
				bo.addError("Cannot compare types " + left.attrType() + " and "
						+ right.attrType());
			} else {
				if (left.attrType() instanceof ExprTypeJavaType) {
					ExprTypeJavaType lt = (ExprTypeJavaType) left.attrType();
					ExprTypeJavaType rt = (ExprTypeJavaType) right.attrType();
					if (lt.getVersion() != rt.getVersion()) {
						bo.addError("Cannot compare objects from different library versions.");
					}
				}
			}
			return ExprTypeBool.instance();

		case DIV:
		case MINUS:
		case MULT:
		case PLUS:
		case MOD:
			checkIfSubtype(left, ExprTypeInt.instance());
			checkIfSubtype(right, ExprTypeInt.instance());
			return ExprTypeInt.instance();
		case GT:
		case GTEQ:
		case LT:
		case LTEQ:
			checkIfSubtype(left, ExprTypeInt.instance());
			checkIfSubtype(right, ExprTypeInt.instance());
			return ExprTypeBool.instance();
		}
		bo.addError("Operator not implemented: " + bo.getOperator());
		return ExprTypeUnknown.instance();
	}

	private static void checkIfSubtype(Expr e, ExprType expected) {
		if (!(e.attrType().isSubtypeOf(expected))) {
			e.addError(e, "Expected expression of type " + expected
					+ " but found " + e.attrType());
		}
	}

	public static ExprType attrType(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		String name = e.getRight().getName();
		if (leftType instanceof ExprTypeHasMembers) {
			ExprTypeHasMembers leftType2 = (ExprTypeHasMembers) leftType;
			return leftType2.typeOfMemberAccess(e, name);
		}
		e.addError("Left hand side of member acces is of type " + leftType
				+ " (expected Java type).");
		return ExprTypeUnknown.instance();
	}

	public static boolean isStatic(IVariableBinding field) {
		return (field.getModifiers() & Modifier.STATIC) > 0;
	}

	public static ExprType attrType(IfThenElse e) {
		checkIfSubtype(e.getCond(), ExprTypeBool.instance());
		ExprType thenType = e.getThenExpr().attrType();
		ExprType elseType = e.getElseExpr().attrType();
		if (!thenType.equals(elseType)) {
			e.addError("Types " + thenType + " and " + elseType
					+ " are not equal");
		}
		// could try to find common supertype if types are different
		return thenType;
	}

	public static IVariableBinding attrField(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		String fieldName = e.getRight().getName();
		if (leftType instanceof ExprTypeHasMembers) {
			ExprTypeHasMembers leftT = (ExprTypeHasMembers) leftType;
			IBinding typeBinding = leftT.findMember(fieldName);
			if (typeBinding instanceof IVariableBinding) {
				return (IVariableBinding) typeBinding;
			}
		}
		return null;
	}

	public static ExprType attrType(UnaryOperation e) {
		switch (e.getOperator()) {
		case NOT:
			checkIfSubtype(e.getExpr(), ExprTypeBool.instance());
			return ExprTypeBool.instance();
		case UMINUS:
			checkIfSubtype(e.getExpr(), ExprTypeInt.instance());
			return ExprTypeInt.instance();
		}
		throw new Error("not implemented: " + e.getOperator());
	}

	public static ExprType attrType(FuncCall e) {
		Def def = e.attrDef();
		if (!(def instanceof BuiltinFunction)) {
			e.addError(def.attrName() + " is not a function.");
		} else {
			BuiltinFunction f = (BuiltinFunction) def;
			boolean hasErrors = false;
			if (e.getNumArgument() > f.getParameterTypes().size()) {
				e.addError("Too many arguments.");
				hasErrors = true;
			} else if (e.getNumArgument() < f.getParameterTypes().size()) {
				e.addError("Missing arguments.");
				hasErrors = true;
			} else {
				for (int i = 0; i < e.getNumArgument(); i++) {
					ExprType argType = e.getArgument(i).attrType();
					ExprType expectedType = f.getParameterTypes().get(i);
					if (!argType.isSubtypeOf(expectedType)) {
						e.getArgument(i).addError(
								"found " + argType + " but expected "
										+ expectedType);
						hasErrors = true;
					}
				}
			}
			if (!hasErrors) {
				return f.exactType(e);
			}
		}
		return def.attrType();
	}

	public static ExprType attrType(VarAccess e) {
		Def def = e.attrDef();
		return def.attrType();
	}

	public static ExprType attrType(NullConst e) {
		return ExprTypeJavaType.nullType();
	}

	public static ExprType placeDefType(PlaceDef placeDef) {
		ExprType pptype1 = placeDef.getProgramPoint().attrType();
		if (!(pptype1 instanceof ExprTypeProgramPoint)) {
			return ExprTypeUnknown.instance();
		}
		ExprTypeProgramPoint pptype = (ExprTypeProgramPoint)pptype1;
		if (placeDef.isPredefinedPlace()) {
			if (placeDef.hasCondition()) {
				placeDef.addError("Observable places must not have a condition.");
			}
			if (placeDef.hasStallCondition()) {
				placeDef.addError("Observable places must not have a stall condition.");
			}
			if (pptype instanceof ExprTypeAtLineProgramPoint) {
				placeDef.addError("Observable places can not be defined within the library implementation");
			}
		}
		if (placeDef.isLocalPlace()) {
			if (pptype instanceof ExprTypeCallProgramPoint && !placeDef.hasPlaceOption(Translation.PLACE_OPTION_NOSYNC)) {
				placeDef.addError("sync support for local places defined at a call statement is not available yet");
			}
		}
		if (placeDef.hasCondition()) {
			checkIfSubtype(placeDef.getCondition(), ExprTypeBool.instance());
		}
		if (placeDef.hasStallCondition()) {
			checkIfSubtype(placeDef.getStallCondition().getCondition(), ExprTypeBool.instance());
			if (placeDef.getStallCondition().hasMeasure()) {
				checkIfSubtype(placeDef.getStallCondition().getMeasure(), ExprTypeInt.instance());
			}
		}
		return new ExprTypePlace(placeDef.isLocalPlace(), pptype);
		/*
		if (placeDef.isLocalPlace()) {
			//ExprTypeProgramPoint programPoint = (ExprTypeProgramPoint) pptype;
			/*if (!(placeDef.getPlaceModifier() instanceof PlaceModifierLocal)) {
				placeDef.addError("Invalid place definition. This should be a 'local place'.");
			}*/
			
		//} else {
			//ExprTypeCallProgramPoint programPoint = (ExprTypeCallProgramPoint) pptype;
			/*if (!(placeDef.getPlaceModifier() instanceof PlaceModifierPredefined)) {
				placeDef.addError("Invalid place definition. This should be a 'predefined place'.");
			}*/
			//return new ExprTypePredefinedPlace(pptype); 
		//}
		/*else {
			placeDef.getProgramPoint().addError(
					"Expected program point but found " + pptype);
			return UnknownType.instance();
		}*/

	}

	private static Statement getStatementInLine(final TwoLibraryModel model,
			ASTNode parent, final int lineNr) {
		final Statement[] result = new Statement[1];
		parent.accept(new StatementVisitor() {

			@Override
			boolean visitStatement(Statement s) {
				if (model.getLineNr(s) == lineNr) {
					result[0] = s;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	public static Collection<Def> lookup(PlaceDef placeDef, String name) {
		ExprType progPoint = placeDef.getProgramPoint().attrType();
		if (progPoint instanceof ExprTypeAtLineProgramPoint) {
			ExprTypePlace placeType = new ExprTypePlace(placeDef.isLocalPlace(), (ExprTypeAtLineProgramPoint) progPoint);
			TwoLibraryModel model = placeDef.attrCompilationUnit().getTwoLibraryModel();
	
			Def r = lookupJava(model, placeType, name, new StackpointerExpr(placeType.getVersion(), !placeType.isLocalPlace()), placeDef);
			if (r != null) {
				return Collections.singletonList(r);
			}
		}
		return placeDef.getParent().lookup(name);
	}
	
	public static Collection<Def> lookup(Expr expr, String name) {
		for (Def d : expr.attrDefinedVars()) {
			if (d.attrName().equals(name)) {
				return Collections.singletonList(d);
			}
		}

		// special lookup rule for stack function
		if (expr.getParent().getParent() instanceof FuncCall) {
			FuncCall funcCall = (FuncCall) expr.getParent().getParent();
			if (funcCall.getFuncName().getName().equals(BuiltinFuncEval_place.name)) { 
				if (funcCall.getNumArgument() == 3
						&& funcCall.getArgument(2) == expr
						&& funcCall.getArgument(0).attrType() instanceof ExprTypePlace) { 
					// stack(place, sp, expr)
					ExprTypePlace placeType = (ExprTypePlace) funcCall.getArgument(0).attrType();
					Expr stackPointerExpr = funcCall.getArgument(1);
					TwoLibraryModel model = expr.attrCompilationUnit().getTwoLibraryModel();

					Def r = lookupJava(model, placeType, name, stackPointerExpr, expr);
					if (r != null) {
						return Collections.singletonList(r);
					}
				} else if (funcCall.getNumArgument() == 2
						&& funcCall.getArgument(1) == expr
						&& funcCall.getArgument(0).attrType() instanceof ExprTypePlace) {
					// stack(place, expr)
					ExprTypePlace placeType = (ExprTypePlace) funcCall.getArgument(0).attrType();
					Expr stackPointerExpr = new StackpointerExpr(placeType.getVersion(), !placeType.isLocalPlace());
					
					TwoLibraryModel model = expr.attrCompilationUnit().getTwoLibraryModel();

					Def r = lookupJava(model, placeType, name, stackPointerExpr, expr);
					if (r != null) {
						return Collections.singletonList(r);
					}
				}
			}
		}

		return expr.getParent().lookup(name);
	}

	private static Def lookupJava(TwoLibraryModel model, ExprTypePlace placeType,
			String name, Expr stackPointerExpr, de.unikl.bcverifier.isl.ast.ASTNode<?> loc) {
		ASTNode s = placeType.getASTNode();

		if (name.equals("this")) {
			ITypeBinding nearestClass = findNearestClass(s);
			return new JavaThis(model, placeType, stackPointerExpr, nearestClass, loc);
		}
		
		IVariableBinding binding = lookupJavaVar(s, name);
		if (binding == null) {
			return null;
		}
		return new JavaVariableDef(model, placeType, stackPointerExpr, binding);
	}

	private static ITypeBinding findNearestClass(ASTNode s) {
		while (s != null) {
			if (s instanceof TypeDeclaration) {
				TypeDeclaration td = (TypeDeclaration) s;
				return td.resolveBinding(); 
			}
			s = s.getParent();
		}
		return null;
	}

	private static IVariableBinding lookupJavaVar(ASTNode s, final String name) {
		final IVariableBinding[] result = new IVariableBinding[1];

		// TODO more precise scoping ForStatement, EnhancedForStatement und
		// Block

		for (ASTNode node = s; node != null && result[0] == null; node = node.getParent()) {
			node.accept(new ASTVisitor() {
				@Override
				public boolean visit(VariableDeclarationFragment node) {
					IVariableBinding b = node.resolveBinding();
					if (b.getName().equals(name)) {
						result[0] = b;
						return false;
					}
					return true;
				}

				@Override
				public boolean visit(SingleVariableDeclaration node) {
					IVariableBinding b = node.resolveBinding();
					if (b.getName().equals(name)) {
						result[0] = b;
						return false;
					}
					return true;
				}
			});
		}
		return result[0];
	}
	
	public static void checkGlobVarDef(GlobVarDef vDef) {
		ExprType vartype = vDef.getVar().attrType();
		ExprType exptype = vDef.getInitialValue().attrType();
		// TODO: restrict scope of initialization expression to previous vars
		if (!exptype.isSubtypeOf(vartype)) {
			vDef.addError("Type of initialization expression (" + exptype +  ") must be a subtype of declared type " + vartype + ".");
		}
	}
	
	public static void checkAssign(Assign a) {
		ExprType vartype = a.getVar().attrType();
		ExprType exptype = a.getExpr().attrType();
		if (!exptype.isSubtypeOf(vartype)) {
			a.addError("Type of assigned expression (" + exptype +  ") must be a subtype of type " + vartype + " of the variable + " + a.attrName() + ".");
		}
		if (!(a.getVar().attrDef() instanceof GlobVarDef)) {
			a.addError("Only ghost variables can be assigned.");
		}
	}

	public static void checkPlaceDef(PlaceDef placeDef) {
		TwoLibraryModel tlm = placeDef.attrCompilationUnit()
				.getTwoLibraryModel();
		ExprType type = placeDef.attrType();
		if (type instanceof ExprTypePlace) {
			ExprTypePlace placeType = (ExprTypePlace) type;
			Version version = placeType.getVersion();
			if (placeType.getASTNode() == null) {
				placeDef.addError("Place " + placeDef.attrName()
						+ " has no statement.");
				return;
			}

			ITypeBinding enclosingClassType = placeType.getEnclosingClassType();
			if (enclosingClassType == null) {
				placeDef.addError("Place " + placeDef.attrName()
						+ " is not in a class.");
				return;
			}
			if (placeDef.hasStallCondition() && placeDef.getStallCondition().hasMeasure()) {
				if (version == Version.NEW) {
					placeDef.addError("Stalled place " + placeDef.attrName()
							+ " in new implementation does not need a termination measure.");
				}
			}
			if (placeDef.hasStallCondition() && !placeDef.getStallCondition().hasMeasure()) {
				if (version == Version.OLD) {
					placeDef.addError("Stalled place " + placeDef.attrName()
							+ " in old implementation must have a termination measure.");
				}
			}
			// TODO check if line is valid
			
			if (placeDef.isLocalPlace() && placeDef.hasPlaceOption(Translation.PLACE_OPTION_NOSPLIT)) {
				placeDef.addError("The " + Translation.PLACE_OPTION_NOSPLIT + " option is not supported for local places.");
			}
			if (placeDef.isPredefinedPlace() && placeDef.hasPlaceOption(Translation.PLACE_OPTION_NOSYNC)) {
				placeDef.addError("The " + Translation.PLACE_OPTION_NOSYNC + " option is not supported for local places.");
			}
			if (placeDef.hasStallCondition() && placeDef.hasPlaceOption(Translation.PLACE_OPTION_NOSYNC)) {
				placeDef.addError("The " + Translation.PLACE_OPTION_NOSYNC + " option is not supported for places with stall condition.");
			}
		}
		
		for (Assign a : placeDef.getAssignmentsList()) {
			a.typecheck();
		}
		
		// check place options
		for (Ident i : placeDef.getPlaceOptions()) {
			if (!i.getName().equals(Translation.PLACE_OPTION_NOSPLIT) && !i.getName().equals(Translation.PLACE_OPTION_NOSYNC)) {
				i.addError("Unsupported place option: " + i.getName());
			}
		}
	}

	public static ExprType attrType(LineNrProgramPoint p) {
		ExprType type = p.getTypeDef().attrType();
		if (!(type instanceof ExprTypeJavaType)) {
			p.getTypeDef().addError("Program point must refer to a Java type.");
			return ExprTypeUnknown.instance();
		}
		ExprTypeJavaType jt = (ExprTypeJavaType) type;
		final TwoLibraryModel model = p.attrCompilationUnit()
				.getTwoLibraryModel();
		ASTNode node = model.getSrc(jt.getVersion()).findDeclaringNode(jt.getTypeBinding());

		Statement s = getStatementInLine(model, node, p.getProgramLineNr());
		if (s == null) {
			p.addError("No statement found in line " + p.getProgramLineNr() +".");
			return ExprTypeUnknown.instance();
		}
		// TODO check if there is exactly one statement in the line ...
		return new ExprTypeAtLineProgramPoint(jt.getVersion(), p.getProgramLineNr(),
				s);
	}
	
	public static ExprType attrType(CallProgramPoint p) {
		ExprType type = p.getTypeDef().attrType();
		if (!(type instanceof ExprTypeJavaType)) {
			p.getTypeDef().addError("Program point must refer to a Java type.");
			return ExprTypeUnknown.instance();
		}
		ExprTypeJavaType jt = (ExprTypeJavaType) type;
		final TwoLibraryModel model = p.attrCompilationUnit()
				.getTwoLibraryModel();
		ASTNode node = model.getSrc(jt.getVersion()).findDeclaringNode(jt.getTypeBinding());

		MethodInvocation inv = getMethodInvocationInLine(p.getFunctionName().getName(), model, node, p.getProgramLineNr());
		if (inv == null) {
			p.addError("No method call found in this line.");
			return ExprTypeUnknown.instance();
		}
		
		return new ExprTypeCallProgramPoint(model, jt.getVersion(), p.getProgramLineNr(), inv);
	}
	

	private static MethodInvocation getMethodInvocationInLine(final String funcname,
			final TwoLibraryModel model, ASTNode node, final int programLineNr) {
		final MethodInvocation[] result = new MethodInvocation[1];
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation inv) {
				if (inv.getName().getFullyQualifiedName().equals(funcname)) {
					if (TwoLibraryModel.getLineNr(inv) == programLineNr) {
						result[0] = inv;
						return false;
					}
				}
				return true;
			}
			
		});
		return result[0];
	}

	public static ExprType attrType(ProgramPoint p) {
		return p.getProgramPointExpr().attrType();
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

	
	public static <T extends de.unikl.bcverifier.isl.ast.ASTNode<?>> T getParentOfType(de.unikl.bcverifier.isl.ast.ASTNode<?> n, Class<T> type) {
		while (n != null) {
			if (type.isAssignableFrom(n.getClass())) {
				return (T) n;
			}
			n = n.getParent();
		}
		return null;
	}

	public static ExprType attrType(InstanceofOperation op) {
		ExprType left = op.getLeft().attrType();
		ExprType right = op.getRight().attrType();
		if (!(left instanceof ExprTypeJavaType)) {
			op.addError("instanceof expects a java type on the left hand side but found " + left);
		} else if (!(right instanceof ExprTypeJavaTypeRef)) {			
			op.addError("instanceof expects a reference to a java type on the right hand side but found " + left);
		} else if (!(right instanceof ExprTypeJavaTypeRef)) {
			op.addError("right side of instanceof operation must refer to a Java type");
		} else {
			if (!((ExprTypeJavaType)left).getVersion().equals(((ExprTypeJavaTypeRef)right).getVersion())) {
				op.addError("instanceof must compare type of same library implementation");
			}
		}
		return ExprTypeBool.instance();
	}

	public static ExprType attrType(VersionConst c) {
		CompilationUnit cu = c.attrCompilationUnit();
		TwoLibraryModel env = null;
		if (cu != null) {
			env = cu.getTwoLibraryModel();
		}
		return ExprTypeVersion.get(env, c.getVal());
	}

	public static ExprType attrType(ExprTypeRef e) {
		TwoLibraryModel env = e.attrCompilationUnit().getTwoLibraryModel();
		String name = e.getRight().getName();
		Version version = e.getVersion();
		ITypeBinding jt = env.getSrc(version).resolveType(name);
		if (jt != null) {
			return ExprTypeJavaTypeRef.create(env, version, jt);
		}
		return ExprTypeJavaPackageRef.create(env, version, name);
	}
	

}

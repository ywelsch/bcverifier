package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.LineNrProgramPoint;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NamedTypeDef;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.ProgramPoint;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeProgramPoint;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.isl.checking.types.UnknownType;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFunction;
import de.unikl.bcverifier.librarymodel.AsmClassNodeWrapper;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class TypeHelper {

	public static ExprType attrType(NamedTypeDef t) {
		Version version = t.getVersion();
		String qualifiedName = getQualifiedName(t.getNames());

		return JavaType.create(t,version, qualifiedName);
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
			checkIfSubtype(left, JavaType.getJavaLangObject(env, Version.OLD));
			checkIfSubtype(right, JavaType.getJavaLangObject(env, Version.NEW));
			return ExprTypeBool.instance();
		case UNEQUALS:
		case EQUALS:
			if (!left.attrType().getClass().equals(right.attrType().getClass())) {
				bo.addError("Cannot compare types " + left.attrType() + " and " + right.attrType());
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
		return UnknownType.instance();
	}

	private static void checkIfSubtype(Expr e, ExprType expected) {
		if (!(e.attrType().isSubtypeOf(expected))) {
			e.addError(e, "Expected expression of type "+expected+" but found " + e.attrType());
		}
	}

	public static ExprType attrType(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		if (leftType instanceof JavaType) {
			JavaType javaType = (JavaType) leftType;
			IVariableBinding field = e.attrField();
			if (field != null) {
				return JavaType.create(e.getRight(), javaType.getVersion(), field.getType());
			} else {
				e.addError("Could not find field " + e.getRight().getName() + " in class " + leftType);
			}
		}
		e.addError("Left hand side of member acces is of type " + leftType + " (expected Java type).");
		return UnknownType.instance();
	}

	public static ExprType attrType(IfThenElse e) {
		checkIfSubtype(e.getCond(), ExprTypeBool.instance());
		ExprType thenType = e.getThenExpr().attrType();
		ExprType elseType = e.getElseExpr().attrType();
		if (!thenType.equals(elseType)) {
			e.addError("Types " + thenType + " and " + elseType + " are not equal");
		}
		// could try to find common supertype if types are different
		return thenType;
	}

	public static IVariableBinding attrField(MemberAccess e) {
		ExprType leftType = e.getLeft().attrType();
		if (leftType instanceof JavaType) {
			JavaType javaType = (JavaType) leftType;
			String fieldName = e.getRight().getName();
			ITypeBinding typeBinding = javaType.getTypeBinding();
			IVariableBinding field = Bindings.findFieldInType(typeBinding, fieldName);
			// TODO search fields in super classes?
			return field;
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
			if (e.getNumArgument() > f.getParameterTypes().size()) {
				e.addError("Too many arguments.");
			} else if (e.getNumArgument() < f.getParameterTypes().size()) {
				e.addError("Missing arguments.");
			} else {
				for (int i=0; i<e.getNumArgument(); i++) {
					ExprType argType = e.getArgument(i).attrType();
					ExprType expectedType = f.getParameterTypes().get(i);
					if (!argType.isSubtypeOf(expectedType)) {
						e.getArgument(i).addError("found " + argType + " but expected " + expectedType);
					}
				}
			}
			return f.exactType(e.getArguments());
		}
		return def.attrType();
	}

	public static ExprType attrType(VarAccess e)  {
		Def def = e.attrDef();
		return def.attrType();
	}

	public static ExprType attrType(NullConst e) {
		return JavaType.object(e.attrCompilationUnit().getTwoLibraryModel());
	}

	public static ExprType placeDefType(PlaceDef placeDef) {
		ExprType pptype = placeDef.getProgramPoint().attrType();
		if (pptype instanceof ExprTypeProgramPoint) {
			ExprTypeProgramPoint programPoint = (ExprTypeProgramPoint) pptype;
			return new PlaceType(programPoint);
		} else {
			placeDef.getProgramPoint().addError("Expected program point but found " + pptype);
		}
		
		// old:
//		Expr pos = placeDef.getProgramPoint();
//		if (pos instanceof LineNrProgramPoint) {
//			final LineNrProgramPoint pos2 = (LineNrProgramPoint) pos;
//			ExprType type = pos2.getTypeDef().attrType();
//			if (!(type instanceof JavaType)) {
//				pos.addError("Place must refer to a Java type.");
//				return UnknownType.instance();
//			}
//			JavaType jt = (JavaType) type; 
//			final TwoLibraryModel model = placeDef.attrCompilationUnit().getTwoLibraryModel();
//			ASTNode node = model.findDeclaringNode(jt.getVersion(), jt.getTypeBinding());
//
//			Statement s = getStatementInLine(model, node, pos2.getProgramLineNr());
//			return new PlaceType(jt.getVersion(), s);
//			
//		}
		// TODO
		throw new Error("not implemented");
	}

	private static Statement getStatementInLine(final TwoLibraryModel model, ASTNode parent, final int lineNr) {
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

	public static Def lookup(Expr expr, String name) {
		// special lookup rule for at(_,_,expr) 
		if (expr.getParent().getParent() instanceof FuncCall) {
			FuncCall funcCall = (FuncCall) expr.getParent().getParent();
			if (funcCall.getFuncName().getName().equals("stack") 
					&& funcCall.getNumArgument() == 3 
					&& funcCall.getArgument(0).attrType() instanceof PlaceType
					&& funcCall.getArgument(2) == expr) {
				PlaceType placeType = (PlaceType) funcCall.getArgument(0).attrType();
				Expr stackPointerExpr = funcCall.getArgument(1);
				TwoLibraryModel model = expr.attrCompilationUnit().getTwoLibraryModel();
				
				Def r = lookupJava(model, placeType, name, stackPointerExpr);
				if (r != null) {
					return r;
				}
			}
		}
	
		for (Def d : expr.attrDefinedVars()) {
			if (d.attrName().equals(name)) {
				return d;
			}
		}
		return expr.getParent().lookup(name);
	}

	private static Def lookupJava(TwoLibraryModel model, PlaceType placeType, String name, Expr StackPointerExpr) {
		Statement s = placeType.getStatement();
		
		IVariableBinding binding = lookupJavaVar(s, name);
		if (binding == null) {
			return null;
		}
		return new JavaVariableDef(model, placeType, StackPointerExpr, binding);
	}

	private static IVariableBinding lookupJavaVar(Statement s, final String name) {
		final IVariableBinding[] result = new IVariableBinding[1];
		
		// TODO more precise scoping ForStatement, EnhancedForStatement und Block
		
		for (ASTNode node = s;node != null && result[0] == null; node=node.getParent()) {
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


	public static void checkPlaceDef(PlaceDef placeDef) {
		TwoLibraryModel tlm = placeDef.attrCompilationUnit().getTwoLibraryModel();
		ExprType type = placeDef.attrType();
		if (type instanceof PlaceType) {
			PlaceType placeType = (PlaceType) type;
			Version version = placeType.getVersion();
			int line = placeType.getLineNr();
			if (placeType.getStatement() == null) {
				placeDef.addError("Place " + placeDef.attrName() + " has no statement.");
				return;
			}
			
			ITypeBinding enclosingClassType = placeType.getEnclosingClassType();
			if (enclosingClassType == null) {
				placeDef.addError("Place " + placeDef.attrName() + " is not in a class.");
				return;
			}
			AsmClassNodeWrapper cn = tlm.getClassNodeWrapper(version, enclosingClassType);
			java.util.List<Integer> pcs = cn.getProgramCounterForLine(line);
			if (pcs.size() == 0) {
				placeDef.addError("No bytecode statement found in line " + line + ".");
			} else if (pcs.size() > 1) {
				// TODO should this be allowed or not?
				placeDef.addError("More than one bytecode statement found in line " + line + ".");
			}
		}
	}

	public static ExprType attrType(LineNrProgramPoint p) {
		ExprType type = p.getTypeDef().attrType();
		if (!(type instanceof JavaType)) {
			p.getTypeDef().addError("Program point must refer to a Java type.");
			return UnknownType.instance();
		}
		JavaType jt = (JavaType) type; 
		final TwoLibraryModel model = p.attrCompilationUnit().getTwoLibraryModel();
		ASTNode node = model.findDeclaringNode(jt.getVersion(), jt.getTypeBinding());

		Statement s = getStatementInLine(model, node, p.getProgramLineNr());
		if (s == null) {
			p.addError("No statement found in this line.");
			return UnknownType.instance();
		}
		// TODO check if there is exactly one statement in the line ...
		return new ExprTypeProgramPoint(jt.getVersion(), p.getProgramLineNr(), s);
	}

	public static ExprType attrType(ProgramPoint p) {
		return p.getProgramPointExpr().attrType();
	}

}

package de.unikl.bcverifier.isl.checking;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.eval.CodeSnippetScope;

import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.IfThenElse;
import de.unikl.bcverifier.isl.ast.LineNrPlacePosition;
import de.unikl.bcverifier.isl.ast.List;
import de.unikl.bcverifier.isl.ast.MemberAccess;
import de.unikl.bcverifier.isl.ast.NamedTypeDef;
import de.unikl.bcverifier.isl.ast.NullConst;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.PlacePosition;
import de.unikl.bcverifier.isl.ast.UnaryOperation;
import de.unikl.bcverifier.isl.ast.UnknownDef;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.ast.translation.BuiltinFunction;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.isl.checking.types.UnknownType;

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
		PlacePosition pos = placeDef.getPlacePosition();
		if (pos instanceof LineNrPlacePosition) {
			final LineNrPlacePosition pos2 = (LineNrPlacePosition) pos;
			ExprType type = pos2.getTypeDef().attrType();
			if (!(type instanceof JavaType)) {
				pos.addError("Place must refer to a Java type.");
				return UnknownType.instance();
			}
			JavaType jt = (JavaType) type; 
			final TwoLibraryModel model = placeDef.attrCompilationUnit().getTwoLibraryModel();
			ASTNode node = model.findDeclaringNode(jt.getVersion(), jt.getTypeBinding());
			
			Statement s = getStatementInLine(model, node, pos2.getPlaceLineNr());
			return new PlaceType(jt.getVersion(), s);
			
		}
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
				TwoLibraryModel model = expr.attrCompilationUnit().getTwoLibraryModel();
				Def r = lookupJava(model, placeType, name);
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

	private static Def lookupJava(TwoLibraryModel model, PlaceType placeType, String name) {
		Statement s = placeType.getStatement();
		
		IVariableBinding binding = lookupJavaVar(s, name);
		if (binding == null) {
			return null;
		}
		return new JavaVariableDef(model, placeType.getVersion(), binding);
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

	private static Scope getJavaScope(Statement s) {
//				org.eclipse.jdt.internal.compiler.ast.ASTNode
		return null;
	}

}

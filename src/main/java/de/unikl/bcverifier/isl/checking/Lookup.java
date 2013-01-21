package de.unikl.bcverifier.isl.checking;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.FuncCall;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAtLineProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFuncEval_place;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class Lookup {

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
}

package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.ITypeBinding;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

/**
 * represents the implicit variable 'this' in methods 
 */
public class JavaThis extends JavaVariableDef {

	private ITypeBinding nearestClass;
	private TwoLibraryModel model;
	private ExprTypePlace placeType;
	private Expr stackPointerExpr;
	private ASTNode<?> loc;

	public JavaThis(TwoLibraryModel model, ExprTypePlace placeType,
			Expr stackPointerExpr, ITypeBinding nearestClass, ASTNode<?> loc) {
		super(model, placeType, stackPointerExpr, null);
		this.loc = loc;
		this.model = model;
		this.placeType = placeType;
		this.stackPointerExpr = stackPointerExpr;
		this.nearestClass = nearestClass;
	}


	@Override
	public String attrName() {
		return "this";
	}

	@Override
	public ExprType attrType() {
		return ExprTypeJavaType.create(loc, placeType.getVersion(), nearestClass);
	}
	
	@Override
	public String getRegisterName() {
		// 'this' is always stored in first register:
		return "reg0_r";
	}

}

package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.IVariableBinding;

import b2bpl.bpl.ast.BPLExpression;

import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.JavaType;
import de.unikl.bcverifier.isl.checking.types.PlaceType;
import de.unikl.bcverifier.librarymodel.AsmClassNodeWrapper;
import de.unikl.bcverifier.librarymodel.LocalVarInfo;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class JavaVariableDef extends Def {

	private IVariableBinding binding;
	private Version version;
	private TwoLibraryModel model;
	private Expr stackPointerExpr;
	private PlaceType placeType;

	public JavaVariableDef(TwoLibraryModel model, PlaceType placeType, Expr stackPointerExpr, IVariableBinding binding) {
		this.model = model;
		this.version = placeType.getVersion();
		this.placeType = placeType;
		this.binding = binding;
		this.stackPointerExpr = stackPointerExpr;
	}

	@Override
	public String attrName() {
		return binding.getName();
	}

	@Override
	public ExprType attrType() {
		return JavaType.create(model, version, binding.getType());
	}
	
	public IVariableBinding getBinding() {
		return binding;
	}

	public Version getVersion() {
		return version;
	}

	
	public String getRegisterName() {
		// TODO case for parameters
		AsmClassNodeWrapper cn = model.getClassNodeWrapper(version, placeType.getEnclosingClassType());
		LocalVarInfo loc = cn.getLocalVar(placeType.getLineNr(), attrName());
		if (attrType() instanceof ExprTypeBool || attrType() instanceof ExprTypeInt) {
			return "reg" + loc.getIndex() + "_i";
		} else {
			return "reg" + loc.getIndex() + "_r";
		}
	}

	public Expr getStackPointerExpr() {
		return stackPointerExpr;
	}

}

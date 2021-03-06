package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.IVariableBinding;

import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bytecode.BCMethod;
import b2bpl.bytecode.InstructionHandle;
import b2bpl.bytecode.JClassType;
import b2bpl.bytecode.LocalVariableInfo;
import de.unikl.bcverifier.isl.ast.BinaryOperation;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

/**
 * represents a local variable in the java source code 
 */
public class JavaVariableDef extends Def {

	private final IVariableBinding binding;
	private final Version version;
	private final TwoLibraryModel model;
	private final BPLExpression stackPointerExpr;
	private final ExprTypePlace placeType;
	private final BPLExpression stackSliceExpr;

	public JavaVariableDef(TwoLibraryModel model, ExprTypePlace placeType, BPLExpression stackSliceExpr, BPLExpression stackFrameExpr, IVariableBinding binding) {
		this.model = model;
		this.version = placeType.getVersion();
		this.placeType = placeType;
		this.binding = binding;
		this.stackPointerExpr = stackFrameExpr;
		this.stackSliceExpr = stackSliceExpr;
	}

	@Override
	public String attrName() {
		return binding.getName();
	}

	@Override
	public ExprType attrType() {
		return ExprTypeJavaType.create(model, version, binding.getType());
	}
	
	public IVariableBinding getBinding() {
		return binding;
	}

	public Version getVersion() {
		return version;
	}
	
	public ExprTypePlace getPlaceType() {
		return placeType;
	}


	/**
	 * get the register name for this local variable 
	 */
	public String getRegisterName() {
		JClassType ct = model.getSrc(version).getClassType(placeType.getEnclosingClassType());
		LocalVariableInfo loc = getLocalVarInfo(ct, placeType.getLineNr(), attrName());
		if (attrType() instanceof ExprTypeBool || attrType() instanceof ExprTypeInt) {
			return "reg" + loc.getRegisterIndex() + "_i";
		} else {
			return "reg" + loc.getRegisterIndex() + "_r";
		}
	}

	private LocalVariableInfo getLocalVarInfo(JClassType ct, int lineNr, String varName) {
		for (BCMethod m : ct.getMethods()) {
			for (InstructionHandle ins : m.getInstructions()) {
				if (ins.getSourceLine() == lineNr) {
					for (LocalVariableInfo l : ins.getActiveLocalVars()) {
						if (l.getVariableName().equals(varName)) {
							return l;
						}
					}
				}
			}
		}
		throw new RuntimeException("Var " + attrName() + " not found in line " + placeType.getLineNr());
	}

	public BPLExpression getStackPointerExpr() {
		return stackPointerExpr;
	}

	public BPLExpression getStackSliceExpr() {
		return stackSliceExpr;
	}

}

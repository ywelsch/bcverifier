package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.IVariableBinding;

import de.unikl.bcverifier.TwoLibraryModel;
import de.unikl.bcverifier.isl.ast.ASTNode;
import de.unikl.bcverifier.isl.ast.Def;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.JavaType;

public class JavaVariableDef extends Def {

	private IVariableBinding binding;
	private Version version;
	private TwoLibraryModel model;

	public JavaVariableDef(TwoLibraryModel model, Version version, IVariableBinding binding) {
		this.model = model;
		this.version = version;
		this.binding = binding;
	}

	@Override
	public String attrName() {
		return binding.getName();
	}

	@Override
	public ExprType attrType() {
		return JavaType.create(model, version, binding.getType());
	}

}

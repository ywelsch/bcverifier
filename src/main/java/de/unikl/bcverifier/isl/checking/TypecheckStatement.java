package de.unikl.bcverifier.isl.checking;

import org.eclipse.jdt.core.dom.ITypeBinding;

import de.unikl.bcverifier.isl.ast.Assign;
import de.unikl.bcverifier.isl.ast.GlobVarDef;
import de.unikl.bcverifier.isl.ast.Ident;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.translation.Translation;
import de.unikl.bcverifier.librarymodel.TwoLibraryModel;

public class TypecheckStatement {

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

}

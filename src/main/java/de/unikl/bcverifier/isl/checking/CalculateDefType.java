package de.unikl.bcverifier.isl.checking;

import de.unikl.bcverifier.isl.ast.NamedTypeDef;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.ProgramPoint;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAtLineProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeCallProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.checking.types.ExprTypeProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeUnknown;
import de.unikl.bcverifier.isl.translation.Translation;

import static de.unikl.bcverifier.isl.checking.TypeHelper.checkIfSubtype;

public class CalculateDefType {


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

	}
	
	public static ExprType attrType(NamedTypeDef t) {
		Version version = t.getVersion();
		String qualifiedName = TypeHelper.getQualifiedName(t.getNames());
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
	
	public static ExprType attrType(ProgramPoint p) {
		return p.getProgramPointExpr().attrType();
	}
}

package de.unikl.bcverifier.isl.translation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLArrayExpression;
import b2bpl.bpl.ast.BPLAssignmentCommand;
import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLBuiltInType;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLNode;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLTypeName;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableDeclaration;
import b2bpl.bpl.ast.BPLVariableExpression;
import b2bpl.translation.ITranslationConstants;

import com.google.common.collect.Lists;

import de.unikl.bcverifier.isl.ast.Assign;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.GlobVarDef;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.InvariantModifier;
import de.unikl.bcverifier.isl.ast.InvariantModifierLocal;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.StallCondition;
import de.unikl.bcverifier.isl.ast.Statement;
import de.unikl.bcverifier.isl.ast.VarAccess;
import de.unikl.bcverifier.isl.ast.VarDef;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.BinRelationType;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeAtLineProgramPoint;
import de.unikl.bcverifier.isl.checking.types.ExprTypeBool;
import de.unikl.bcverifier.isl.checking.types.ExprTypeInt;
import de.unikl.bcverifier.isl.checking.types.ExprTypeJavaType;
import de.unikl.bcverifier.isl.checking.types.ExprTypePlace;
import de.unikl.bcverifier.isl.parser.IslError;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFuncLibrarySlice2;
import de.unikl.bcverifier.isl.translation.builtinfuncs.BuiltinFuncTopSlice2;
import de.unikl.bcverifier.specification.LocalPlaceDefinitions;
import de.unikl.bcverifier.specification.Place;
import de.unikl.bcverifier.specification.SpecAssignment;
import de.unikl.bcverifier.specification.SpecExpr;

public class Translation {
	

	public static final String PLACE_OPTION_NOSPLIT = "nosplit";
	public static final String PLACE_OPTION_NOSYNC = "nosync";

	public static java.util.List<SpecExpr> generateInvariants(CompilationUnit cu) {
		List<SpecExpr> result = new ArrayList<SpecExpr>();
		for (Statement s : cu.getStatements()) {
			if (s instanceof Invariant) {
				Invariant inv = (Invariant) s;
				BPLExpression welldefinedness = inv.getExpr().translateExprWellDefinedness();
				BPLExpression invExpr = inv.getExpr().translateExpr();
				String comment = inv.getExpr().print() 
						+ " " + IslError.create(inv, "This invariant might not hold.");
				
				if (isLocalInvariant(inv)) {
					// for local invariants, add a guard which checks if 
					// the execution is currently at a local place
					BPLExpression isAtLocalPlace = BuiltinFuncLibrarySlice2.isLibrarySlice(Version.OLD, 
							BuiltinFuncTopSlice2.getIpVar(Version.OLD)
							);
					welldefinedness = new BPLBinaryLogicalExpression(
							BPLBinaryLogicalExpression.Operator.IMPLIES, 
							isAtLocalPlace, 
							welldefinedness);
					invExpr = new BPLBinaryLogicalExpression(
							BPLBinaryLogicalExpression.Operator.IMPLIES, 
							isAtLocalPlace, 
							invExpr);
				}
				
				result.add(new SpecExpr(invExpr, welldefinedness, comment));
			}
		}
		return result;
	}

	private static boolean isLocalInvariant(Invariant inv) {
		for (InvariantModifier mod : inv.getInvariantModifiers()) {
			if (mod instanceof InvariantModifierLocal) {
				return true;
			}
		}
		return false;
	}

	public static LocalPlaceDefinitions generatePlaces(CompilationUnit cu) {
		Map<Integer, List<Place>> oldPlaces = new HashMap<Integer, List<Place>>();
		Map<Integer, List<Place>> newPlaces = new HashMap<Integer, List<Place>>();
		
		for (Statement s : cu.getStatements()) {
			if (s instanceof PlaceDef) {
				PlaceDef def = (PlaceDef) s;
				SpecExpr condition;
				if (def.hasCondition()) {
					condition = SpecExpr.fromExpr(def.getCondition());
				} else {
					condition = new SpecExpr(BPLBoolLiteral.TRUE, BPLBoolLiteral.TRUE);
				}
				Expr stallCond = null;
				Expr measure = null;
				if (def.hasStallCondition()) {
					StallCondition stall = def.getStallCondition();
					stallCond = stall.getCondition();
					if (stall.hasMeasure()) {
						measure = stall.getMeasure();
					}
				}
				SpecExpr oldStallCond = null;
				SpecExpr newStallCond = null;
				if (stallCond != null) {
					def.attrCompilationUnit().setPhase(Phase.PRE);
					oldStallCond = SpecExpr.fromExpr(stallCond);
					def.attrCompilationUnit().setPhase(Phase.POST);
					newStallCond = SpecExpr.fromExpr(stallCond);
				}
				SpecExpr oldMeasure = null;
				SpecExpr newMeasure = null;
				if (measure != null) {
					def.attrCompilationUnit().setPhase(Phase.PRE);
					oldMeasure = SpecExpr.fromExpr(measure);
					def.attrCompilationUnit().setPhase(Phase.POST);
					newMeasure = SpecExpr.fromExpr(measure);
				}
				
				ExprType placePositionType = def.getProgramPoint().attrType();
				if (placePositionType instanceof ExprTypeAtLineProgramPoint) {
					ExprTypeAtLineProgramPoint progPoint = (ExprTypeAtLineProgramPoint) placePositionType;
					List<SpecAssignment> assigns = Lists.newArrayList();
					for (Assign a : def.getAssignmentss()) {
						SpecAssignment asc = new SpecAssignment(a.getVar().translateExpr(), a.getExpr().translateExpr(), a.getExpr().translateExprWellDefinedness(), 
									a.getVar().print() + " := " + a.getExpr().print());
						assigns.add(asc);
					}
					String className = ((ExprTypePlace)def.attrType()).getEnclosingClassType().getQualifiedName();
					Place p = new Place(progPoint.getVersion() == Version.OLD, def.getName().getName(), className, def.hasPlaceOption(PLACE_OPTION_NOSPLIT), def.hasPlaceOption(PLACE_OPTION_NOSYNC),
							condition, oldStallCond, oldMeasure, newStallCond, newMeasure, assigns);
					if (progPoint.getVersion() == Version.OLD) {
						put(oldPlaces, progPoint.getLine(), p);
					} else {
						put(newPlaces, progPoint.getLine(), p);
					}
				}
			}
		}
		LocalPlaceDefinitions result = new LocalPlaceDefinitions(oldPlaces, newPlaces);
		return result;
	}

	private static void put(Map<Integer, List<Place>> places, int line, Place p) {
		List<Place> l = places.get(line);
		if (l == null) {
			l = new ArrayList<Place>();
			places.put(line, l);
		}
		l.add(p);
	}

	private static String exprToString(BPLExpression inv) {
		if (inv == null)
			return null;
		Writer s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		BPLPrinter printer = new BPLPrinter(pw );
		inv.accept(printer);
		pw.flush();
		return "(" + s.toString() + ")";
	}
	
	private static String cmdToString(BPLNode cmd) {
		if (cmd == null)
			return null;
		Writer s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		BPLPrinter printer = new BPLPrinter(pw );
		cmd.accept(printer);
		pw.flush();
		return s.toString();
	}

	public static List<String> generatePreludeAddition(CompilationUnit cu) {
		List<String> result = Lists.newArrayList();
		
		return result;
	}
	
	public static List<String> generatePreconditions(CompilationUnit cu) {
		List<String> result = Lists.newArrayList();
		for (Statement s : cu.getStatements()) {
			if (s instanceof PlaceDef) {
				PlaceDef def = (PlaceDef) s;
				ExprTypePlace placeType = (ExprTypePlace) def.attrType();
				if (placeType.isLocalPlace())
					continue; // splitvc for local places is statically resolved during code generation
				BPLExpression isHavoc;
				if (!def.hasPlaceOption(PLACE_OPTION_NOSPLIT)) {
					isHavoc = BPLBoolLiteral.FALSE;
					BPLAssignmentCommand r = new BPLAssignmentCommand(
							new BPLArrayExpression(
									new BPLVariableExpression(ITranslationConstants.USE_HAVOC)
									, new BPLVariableExpression(placeType.getBoogiePlaceName(cu.getTwoLibraryModel()))) 
							,isHavoc
							);
					result.add(cmdToString(r));
				}
			}
		}
		return result;
	}

	public static java.util.List<String> generateGlobalAssignments(CompilationUnit cu) {
		List<String> result = Lists.newArrayList();
		for (Statement s : cu.getStatements()) {
			if (s instanceof Assign) {
				Assign gvd = (Assign) s;
				VarAccess vd = gvd.getVar();
				result.add(cmdToString(new BPLAssignmentCommand(new BPLVariableExpression(vd.getName().getName()), gvd.getExpr().translateExpr())));
			}
		}
		return result;
	}

	public static java.util.List<String> generateInitialAssignments(CompilationUnit cu) {
		List<String> result = Lists.newArrayList();
		for (Statement s : cu.getStatements()) {
			if (s instanceof GlobVarDef) {
				GlobVarDef gvd = (GlobVarDef) s;
				VarDef vd = gvd.getVar();
				result.add(cmdToString(new BPLAssignmentCommand(new BPLVariableExpression(vd.attrName()), gvd.getInitialValue().translateExpr())));
			}
		}
		return result;
	}

	public static java.util.List<BPLVariableDeclaration> generateGlobalVariables(CompilationUnit cu) {
		List<BPLVariableDeclaration> result = Lists.newArrayList();
		for (Statement s : cu.getStatements()) {
			if (s instanceof GlobVarDef) {
				GlobVarDef gvd = (GlobVarDef) s;
				VarDef vd = gvd.getVar();
				BPLType type;
				if (vd.attrType() instanceof ExprTypeJavaType) {
					type = new BPLTypeName(ITranslationConstants.REF_TYPE); 
				} else if (vd.attrType() instanceof BinRelationType) {
					type = new BPLTypeName(ITranslationConstants.BINREL_TYPE); 
				} else if (vd.attrType() instanceof ExprTypeInt) {
					type = BPLBuiltInType.INT; 
				} else if (vd.attrType() instanceof ExprTypeBool) {
					type = BPLBuiltInType.BOOL; 
				} else {
					throw new Error("Type not supported yet.");
				}
				result.add(new BPLVariableDeclaration(new BPLVariable(vd.attrName(), type, ExprTranslation.createTypAssum(vd, true))));
			}
		}
		return result;
	}
}

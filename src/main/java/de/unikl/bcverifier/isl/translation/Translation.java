package de.unikl.bcverifier.isl.translation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.EmptyBPLVisitor;
import b2bpl.bpl.IBPLVisitor;
import b2bpl.bpl.ast.BPLBinaryArithmeticExpression;
import b2bpl.bpl.ast.BPLBinaryLogicalExpression;
import b2bpl.bpl.ast.BPLBoolLiteral;
import b2bpl.bpl.ast.BPLBuiltInType;
import b2bpl.bpl.ast.BPLEqualityExpression;
import b2bpl.bpl.ast.BPLExpression;
import b2bpl.bpl.ast.BPLIntLiteral;
import b2bpl.bpl.ast.BPLQuantifierExpression;
import b2bpl.bpl.ast.BPLRelationalExpression;
import b2bpl.bpl.ast.BPLType;
import b2bpl.bpl.ast.BPLVariable;
import b2bpl.bpl.ast.BPLVariableExpression;
import de.unikl.bcverifier.bpl.UsedVariableFinder;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Expr;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.LocalInvariant;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.StallCondition;
import de.unikl.bcverifier.isl.ast.Statement;
import de.unikl.bcverifier.isl.ast.Version;
import de.unikl.bcverifier.isl.checking.types.ExprType;
import de.unikl.bcverifier.isl.checking.types.ExprTypeProgramPoint;
import de.unikl.bcverifier.specification.LocalPlaceDefinitions;
import de.unikl.bcverifier.specification.Place;
import de.unikl.bcverifier.specification.SpecInvariant;

public class Translation {
	

	public static java.util.List<SpecInvariant> generateInvariants(CompilationUnit cu) {
		List<SpecInvariant> result = new ArrayList<SpecInvariant>();
		for (Statement s : cu.getStatements()) {
			if (s instanceof Invariant) {
				Invariant inv = (Invariant) s;
				BPLExpression welldefinedness = inv.getExpr().translateExprWellDefinedness();
				BPLExpression invExpr = inv.getExpr().translateExpr();
				String comment = inv.getExpr().print();
				
				// forall interaction frames
				// TODO only do this when ifram var is used.
				welldefinedness = forallInteractionFrames(welldefinedness);
				invExpr = forallInteractionFrames(invExpr);
				
				result.add(new SpecInvariant(invExpr, welldefinedness, comment));
			}
		}
		return result;
	}

	private static BPLExpression forallInteractionFrames(BPLExpression expr) {
		
		Map<String, BPLVariable> tofind = Maps.newHashMap();
		tofind.put("iframe", null);
		UsedVariableFinder variableFinder = new UsedVariableFinder(tofind);
		expr.accept(variableFinder);
		if (variableFinder.getUsedVariables().size() == 0) {
			// variable iframe not used in expression
			return expr;
		}
		
		expr = new BPLBinaryLogicalExpression(
				BPLBinaryLogicalExpression.Operator.IMPLIES, 
				ExprWellDefinedness.conjunction(
					// 0 <= iframe 
					new BPLRelationalExpression(
							BPLRelationalExpression.Operator.LESS_EQUAL,
							new BPLIntLiteral(0),
							new BPLVariableExpression("iframe")
							),
					// iframe <= ip1
					new BPLRelationalExpression(
							BPLRelationalExpression.Operator.LESS_EQUAL,
							new BPLVariableExpression("iframe"),
							new BPLVariableExpression("ip1")
							),
					// iframe % 2 == 1				
					new BPLEqualityExpression(
							BPLEqualityExpression.Operator.EQUALS,
							new BPLBinaryArithmeticExpression(
									BPLBinaryArithmeticExpression.Operator.REMAINDER,
									new BPLVariableExpression("iframe"),
									new BPLIntLiteral(2)
									),
							new BPLIntLiteral(1)
							)
				), 
				expr); 
		
		return new BPLQuantifierExpression(
				BPLQuantifierExpression.Operator.FORALL,
				new BPLVariable[] { new BPLVariable("iframe", BPLBuiltInType.INT) },
				expr
				);
	}

	public static java.util.List<SpecInvariant> generateLocalInvariants(CompilationUnit cu) {
		List<SpecInvariant> result = new ArrayList<SpecInvariant>();
		for (Statement s : cu.getStatements()) {
			if (s instanceof LocalInvariant) {
				LocalInvariant inv = (LocalInvariant) s;
				BPLExpression welldefinedness = inv.getExpr().translateExprWellDefinedness();
				BPLExpression invExpr = inv.getExpr().translateExpr();
				String comment = inv.getExpr().print();
				result.add(new SpecInvariant(invExpr, welldefinedness, comment));
			}
		}
		return result;
	}

	public static LocalPlaceDefinitions generatePlaces(CompilationUnit cu) {
		Map<Integer, List<Place>> oldPlaces = new HashMap<Integer, List<Place>>();
		Map<Integer, List<Place>> newPlaces = new HashMap<Integer, List<Place>>();
		
		for (Statement s : cu.getStatements()) {
			if (s instanceof PlaceDef) {
				PlaceDef def = (PlaceDef) s;
				BPLExpression condition;
				if (def.hasCondition()) {
					condition = def.getCondition().translateExpr();
				} else {
					condition = BPLBoolLiteral.TRUE;
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
				BPLExpression oldStallCond = null;
				BPLExpression newStallCond = null;
				if (stallCond != null) {
					def.attrCompilationUnit().setPhase(Phase.PRE);
					oldStallCond = stallCond.translateExpr();
					def.attrCompilationUnit().setPhase(Phase.POST);
					newStallCond = stallCond.translateExpr();
				}
				BPLExpression oldMeasure = null;
				BPLExpression newMeasure = null;
				if (measure != null) {
					def.attrCompilationUnit().setPhase(Phase.PRE);
					oldMeasure = measure.translateExpr();
					def.attrCompilationUnit().setPhase(Phase.POST);
					newMeasure = measure.translateExpr();
				}
				
				ExprType placePositionType = def.getProgramPoint().attrType();
				if (placePositionType instanceof ExprTypeProgramPoint) {
					ExprTypeProgramPoint progPoint = (ExprTypeProgramPoint) placePositionType;
					Place p = new Place(progPoint.getVersion() == Version.OLD, def.getName().getName(), 
							exprToString(condition), exprToString(oldStallCond), exprToString(oldMeasure), exprToString(newStallCond), exprToString(newMeasure));
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
	

}

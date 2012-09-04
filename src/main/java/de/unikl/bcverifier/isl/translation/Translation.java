package de.unikl.bcverifier.isl.translation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b2bpl.bpl.BPLPrinter;
import b2bpl.bpl.ast.BPLExpression;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.LocalInvariant;
import de.unikl.bcverifier.isl.ast.PlaceDef;
import de.unikl.bcverifier.isl.ast.Statement;
import de.unikl.bcverifier.isl.ast.Version;
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
				result.add(new SpecInvariant(invExpr, welldefinedness, comment));
			}
		}
		return result;
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
				BPLExpression condition = def.getCondition().translateExpr();
				Place p = new Place(def.getName().getName(), exprToString(condition), null);
				if (def.getPlacePosition().attrVersion() == Version.OLD) {
					put(oldPlaces, def.getPlacePosition().attrLine(), p);
				} else {
					put(newPlaces, def.getPlacePosition().attrLine(), p);
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
		Writer s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		BPLPrinter printer = new BPLPrinter(pw );
		inv.accept(printer);
		pw.flush();
		return "(" + s.toString() + ")";
	}
	

}

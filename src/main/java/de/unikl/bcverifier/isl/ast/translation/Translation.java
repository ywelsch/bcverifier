package de.unikl.bcverifier.isl.ast.translation;

import java.util.ArrayList;
import java.util.List;

import b2bpl.bpl.ast.BPLAssertCommand;
import b2bpl.bpl.ast.BPLAssumeCommand;
import b2bpl.bpl.ast.BPLCommand;
import b2bpl.bpl.ast.BPLExpression;
import beaver.Symbol;
import de.unikl.bcverifier.isl.ast.CompilationUnit;
import de.unikl.bcverifier.isl.ast.Invariant;
import de.unikl.bcverifier.isl.ast.Statement;
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

	

}

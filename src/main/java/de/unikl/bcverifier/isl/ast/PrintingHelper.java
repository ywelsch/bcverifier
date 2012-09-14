package de.unikl.bcverifier.isl.ast;

import java.io.IOException;

public class PrintingHelper {

	public static Void print(Appendable r, NullConst nullConst) {
		print(r, "null");
		return null;
	}

	

	public static Void print(Appendable r, BoolConst boolConst) {
		print(r, boolConst.getVal() ? "true" : "false");
		return null;
	}

	public static Void print(Appendable r, VarAccess varAccess) {
		print(r, varAccess.getName().getName());
		return null;
	}

	public static Void print(Appendable r, UnaryOperation e) {
		print(r, e.getOperator().toString());
		e.getExpr().printTo(r);
		return null;
	}

	public static Void print(Appendable r, IntConst e) {
		print(r, e.getVal());
		return null;
	}

	public static Void print(Appendable r, MemberAccess e) {
		e.getLeft().printTo(r);
		print(r, ".");
		print(r, e.getRight().getName());
		return null;
	}

	public static Void print(Appendable r, ErrorExpr e) {
		print(r, "<ERROR " + e.getMessage() + ">");
		return null;
	}

	public static Void print(Appendable r, IfThenElse e) {
		print(r, "if ");
		e.getCond().printTo(r);
		print(r, "then ");
		e.getThenExpr().printTo(r);
		print(r, "else ");
		e.getElseExpr().printTo(r);
		return null;
	}

	public static Void print(Appendable r, ForallExpr e) {
		print(r, "forall ");
		boolean first = true;
		for (VarDef v : e.getBoundVars()) {
			if (!first) {
				print(r, ", ");
			}
			first = false;
			print(r, v.getTypeDef().attrType().toString());
			print(r, " ");
			print(r, v.getName().getName());
		}
		print(r, " :: ");
		e.getExpr().printTo(r);
		return null;
	}

	public static Void print(Appendable r, FuncCall e) {
		print(r, e.getFuncName().getName());
		print(r, "(");
		boolean first = true;
		for (Expr a : e.getArguments()) {
			if (!first) {
				print(r, ", ");
			}
			first = false;
			a.printTo(r);
		}
		print(r, ")");
		return null;
	}

	public static Void print(Appendable r, BinaryOperation e) {
		print(r, "(");
		e.getLeft().printTo(r);
		print(r, " ");
		print(r, e.getOperator().toString());
		print(r, " ");
		e.getRight().printTo(r);
		print(r, ")");
		return null;
	}
	
	private static void print(Appendable r, String s) {
		try {
			r.append(s);
		} catch (IOException e) {
			throw new Error(e);
		}
	}



	public static Void print(Appendable r, LineNrProgramPoint p) {
		print(r, p.getTypeDef().toString()); // TODO print correctly
		print(r, ", line " + p.attrLine());
		return null;
	}

}

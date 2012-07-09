package de.unikl.bcverifier.isl.ast;

import java.util.ArrayList;
import java.util.List;


public class AstUtils {

	public static <S extends ASTNode<?>,T extends S> List<S> list(de.unikl.bcverifier.isl.ast.List<T> l) {
		List<S> result = new ArrayList<S>(l.getNumChild());
		for (T t : l) {
			result.add(t);
		}
		return result;
	}
}

package de.unikl.bcverifier.isl.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import beaver.Symbol;
import de.unikl.bcverifier.isl.ast.ASTNode;

public class IslError {
	
	private final int line;
	private final int column;
	private final int endLine;
	private final int endColumn;
	private final String message;
	
	private static final Pattern errorPattern = Pattern.compile("\\[isl:([0-9]+):([0-9]+)\\-([0-9]+):([0-9]+)\\|(.*)\\]");
	
	protected IslError(int line, int column, int endLine, int endColumn,
			String message) {
		this.line = line;
		this.column = column;
		this.endLine = endLine;
		this.endColumn = endColumn;
		this.message = message;
	}

	public IslError(ASTNode<?> node, String msg) {
		this.line = Symbol.getLine(node.getStart());
		this.column = Symbol.getColumn(node.getStart());
		this.endLine = Symbol.getLine(node.getEnd());
		this.endColumn = Symbol.getColumn(node.getEnd());
		this.message = msg;
	}

	@Override
	public String toString() {
		return getErrorString();
	}
	
	public String getErrorString() {
		return "[isl:" + line + ":" + column + "-" + endLine + ":" + endColumn + "|" + message + "]";
	}
	
	public static IslError parse(String s) {
		Matcher m = errorPattern.matcher(s);
		if (m.matches()) {
			return new IslError(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), m.group(5));
		}
		return null;
	}
	
	public static IslError findInString(String msg) {
		Matcher m = errorPattern.matcher(msg);
		if (m.find()) {
			return new IslError(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), m.group(5));
		}
		return null;
	}
	
	public static IslError create(int line, int column, int endLine, int endColumn, String msg) {
		return new IslError(line, column, endLine, endColumn, msg);
	}

	public static IslError create( ASTNode<?> e, String msg) {
		int line = Symbol.getLine(e.getStart());
		int column = Symbol.getColumn(e.getStart());
		int endLine = Symbol.getLine(e.getEnd());
		int endColumn = Symbol.getColumn(e.getEnd());
		return create(line, column, endLine, endColumn, msg);
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndColumn() {
		return endColumn;
	}

	public String getMessage() {
		return message;
	}

	
	
}

package ackermann;

public class Stack {
	private final Stack n;
	public final int val;
	private final int size;
	
	private Stack(Stack n, int val) {
		this.n = n;
		this.val = val;
		if (n == null) {
			this.size = 1;
		} else {
			this.size = n.size + 1;
		}
	}
	
	public static Stack initialStack() {
		return null;
	}
	
	public static boolean sizeNotOne(Stack s) {
		return s != null && s.n != null;
	}
	
	public static Stack push(Stack s, int val) {
		return new Stack(s, val);
	}
	
	public static Stack pop(Stack s) {
		return s.n;
	}
	
}


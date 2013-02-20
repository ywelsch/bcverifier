package awk;

public class A {
	private int x = 3;
	
	
	private A() {
		x = 4;
	}
	
	private int blub() {
		x = 5;
		return x;
	}
	
	public void bla() {
		x = foo();
		blub();
		x = 3;
	}
	
	protected static int foo() {
		return 2;
	}
}
package awk;

public class A {
	private int x = 3;
	
	private int blub() {
		x = 5;
		return x;
	}
	
	public void bla() {
		x = foo();
		blub();
		x = 3;
	}
	
	public static int foo() {
		return 1;
	}
}
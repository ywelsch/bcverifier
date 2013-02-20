package awk;

public class A {
	public int blub(int x) {
		int z = 5;
		for (int i=0; i<x; i++) {
			z++;
		}
		if (z != 7) {
			z-=5;
		}
		return z;
	}
}
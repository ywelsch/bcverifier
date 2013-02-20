package awk;

public class A {
	public int blub(int x) {
		if (x <= 0) return 0;
		int z = 0;
		for (int i=0; i<x; i++) {
			z++;
		}
		return z;
	}
}
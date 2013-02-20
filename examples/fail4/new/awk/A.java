package awk;

public class A {
	public int blub(Callable c, int x) {
		if (c == null) 
			return 0;
		int z = x;
		c.foo(z);
		z++;
		if (z > 5) {
			c.foo(z);
			z++;
		}
		return z;
	}
}
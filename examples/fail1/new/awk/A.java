package awk;

public class A {
	public int blub(int x, int y) {
		if (x > 5) {
			if (y <= 3) {
				int z = 5;
				z -= 4;
				return z;
			} else {
				int z = 5;
				z -= 3;
				return z;
			}
		} else {
			return 3;
		}
	}
	
	public int bla(int x) {
		return blub(x, 2);
	}
}
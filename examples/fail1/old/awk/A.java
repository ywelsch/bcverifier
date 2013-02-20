package awk;

public class A {
	public int blub(int x, int y) {
		if (x > 5) {
			if (y < 3) {
				return 1;
			} else {
				return 2;
			}
		} else {
			return 3;
		}
	}
	
	public int bla(int x) {
		return blub(x, 2);
	}
}
package ackermann;

public class Ackermann {
	public static int a(int x, int y) {
		if (x < 0 || y < 0) 
			return 0;
		if (x == 0) {
			return y+1;
		} else if (y == 0) {
			return a(x-1, 1);
		} else {
			return a(x-1, a(x,y-1));
		}
	}
}
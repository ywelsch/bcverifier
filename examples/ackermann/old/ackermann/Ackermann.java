package ackermann;

public class Ackermann {
	public static int a(int x, int y) {
		if (x < 0 || y < 0) 
			return 0;
		int res;
		if (x == 0) {
			res = y+1;
			return res;
		} else if (y == 0) {
			res = a(x-1, 1);
			return res;
		} else {
			int r = a(x,y-1);
			res = a(x-1, r);
			return res;
		}
	}
}
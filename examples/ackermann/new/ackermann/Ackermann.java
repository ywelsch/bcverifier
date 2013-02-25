package ackermann;

import static ackermann.Stack.*;

public class Ackermann {
	public static int a(int x, int y) {
		if (x < 0 || y < 0) 
			return 0;
		Stack s = initialStack();
		s = push(s, x);
		s = push(s, y);
		while (sizeNotOne(s)) {
			s = pop(s);
			x = s.val;
			s = pop(s);
			y = s.val;
			if (x == 0) {
				s = push(s, y+1);
			} else if (y == 0) {
				s = push(s, x-1);
				s = push(s, 1);
			} else {
				s = push(s, x-1);
				s = push(s, x);
				s = push(s, y-1);
			}
		}
		s = pop(s);
		return s.val;
	}
}


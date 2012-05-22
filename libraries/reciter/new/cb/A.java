package cb;

public class A {
	public int exec(C c, int s) {
		if (s == 0) {
			c.run();
			return 0;
		} else {
			c.run();
			return 1 + exec(c, s - 1);
		}
    }
}

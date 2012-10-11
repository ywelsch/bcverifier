package cb;

public class A {
	public int sum(int n) {
		if (n <= 0) return 0;
		int result = 0;
		for (int i = 0; i <= n; i++) {
			result += i;
		}
		return result;
    }
}

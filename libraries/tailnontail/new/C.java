public class C {
    public int f(int n){
		return f_h(n, 1);
	}
    private int f_h(int n, int a){
    	if (n == 0) {
            return a;
    	} else {
            int result = f_h(n - 1, a * n);
            return result;
    	}
	}
}
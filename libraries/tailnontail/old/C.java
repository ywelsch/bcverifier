public class C {
	public int f(int n){
		if (n == 0) {
    	    return 1;
		} else {
    	    int result = n * f(n - 1);
    	    return result;
		}
	}
}
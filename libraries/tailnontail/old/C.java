public class C {
	public int f(int n){
		if (n == 0) {
    	    return 1;
		} else {
    	    int result = f(n - 1);
    	    return n * result;
		}
	}
}
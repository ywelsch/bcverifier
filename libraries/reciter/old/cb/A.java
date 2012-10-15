package cb;

public class A {
  public static int sum(int n) {
	  if (n <= 0) {
		  return 0;
	  } else {
		 int result = sum(n - 1);
		 return result + n;
	  }
  }
}
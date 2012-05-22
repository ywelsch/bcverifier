package cb;

public class A {
  public int exec(C c, int s) {
	  int result = 0;
	  for (int i = 0; i < s; i++) {
		  result += i;
		  c.run();
	  }
	  return result;
  }
}

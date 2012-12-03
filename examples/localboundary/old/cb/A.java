package cb;

public class A {
  public int exec(C c) {
    int x = 0;
    if (c != null) c.run();
    return x;
  }
}
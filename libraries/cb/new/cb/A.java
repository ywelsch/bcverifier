package cb;

public class A {
  private int g;
  public A exec(C c) {
    if (c != null) c.run();
    return null;
  }
  public void inc() { g = g + 2; }
}
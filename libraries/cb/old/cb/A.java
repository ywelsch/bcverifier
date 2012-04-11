package cb;

public class A {
  private int g;
  public A exec(C c) {
    if (c != null) c.run();
    if (g % 2 == 0) { return null; }
    else { return this; }
  }
  public void inc() { g = g + 2; }
}

package cb;

public class A {
  private int g;
  public boolean exec(C c) {
    if (c != null) c.run();
    return g % 2 == 0;
  }
  public void inc() { g = g + 2; }
}
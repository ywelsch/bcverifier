package cb;

public class A {
  private int g;
  public boolean exec(C c) {
    if (c != null) c.run();
    return true;
  }
  public void inc() { g = g + 2; }
}
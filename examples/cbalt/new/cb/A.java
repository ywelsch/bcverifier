package cb;

public class A {
  private int g;
  public boolean exec(C c) {
    int i = 4;
    if (c != null) c.run();
    return true;
  }
  public void inc() { g = g + 2; }
}
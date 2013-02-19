package cb;

public class A {
  private int x;
  private boolean l;
  public void inc(C c) {
    if (c == null) return;
    if (!l) {
      l = true;
      c.run();
      x = x + 1;
      l = false;
    }
  }
  public int get() {
    return x;
  }
}
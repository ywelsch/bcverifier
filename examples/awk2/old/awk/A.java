package awk;

public class A {
  private int x;
  public int exec(C c) {
    x = 0;
    if (c != null) c.run();
    x = 1;
    if (c != null) c.run();
    return x;
  }
}
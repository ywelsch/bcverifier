// Version 1
public interface C { void run(); }

public class A {
  int g; // default value is 0
  C exec(C c) {
    if (c != null) c.run();
    if (g % 2 == 1) { return c; }
    else { c = self(c); return c; }
  }
  private C self(C c) {
    return c;
  }
  void inc() { g = g + 2; }
}

// Version 2
public interface C { void run(); }

public class A {
  int g; // default value is 0
  C exec(C c) {
    if (c != null) c.run();
    return c;
  }
  void inc() { g = g + 2; }
}
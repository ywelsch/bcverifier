// Version 1
public interface C { void run(); }

public class A {
  int g; // default value is 0
  A exec(C c) {
    if (c != null) c.run();
    if (g % 2 == 0) { return null; }
    else { return self(this); }
  }
  private A self(A a) {
    return a;
  }
  void inc() { g = g + 2; }
}

// Version 2
public interface C { void run(); }

public class A {
  int g; // default value is 0
  A exec(C c) {
    if (c != null) c.run();
    return null;
  }
  void inc() { g = g + 2; }
}
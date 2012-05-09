package subtypes;

public class A {
  public A m() {
	  return new B();
  }
}

class B extends A {}
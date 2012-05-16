package subtypes;

public class A {
  public A m() {
	  return new A();
  }
}

class B extends A {
    public A m() {
        return null;
    }
}
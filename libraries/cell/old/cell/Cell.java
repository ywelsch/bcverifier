package cell;

public class Cell<T> {
  private T c;
  public void set(T o) {c = o;}
  public T get() {return c;}
}
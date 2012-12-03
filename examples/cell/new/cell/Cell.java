package cell;

public class Cell<T> {
  private T c1, c2;
  private boolean f;
  public void set(T o) {
    f = !f;
    if(f) c1 = o; else c2 = o;
  }
  public T get(){
    return f ? c1 : c2;
  }
}
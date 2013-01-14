package cell;

public class Cell {
  private static Object c1, c2;
  private static boolean f;
  public static void set(Object o) {
    f = !f;
    if(f) c1 = o; else c2 = o;
  }
  public static Object get(){
    if(f) return c1; else return c2;
  }
}
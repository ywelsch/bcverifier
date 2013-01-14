package cell;

public class Cell {
  private static Object c;
  public static void set(Object o) {c = o;}
  public static Object get() {return c;}
}
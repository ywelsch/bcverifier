package namegen;
public class Fact {
  private static int x;
  public static C fresh() {
    return new C(++x);
  }
  public static boolean check(C o) {
    return true;
  }
}
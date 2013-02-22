package namegen;
public class Fact {
  private static int x;
  public static C fresh() {
    return new C(++x);
  }
  public static boolean check(C o) {
    if (o == null) return true;
    return o.z <= x;
  }
}
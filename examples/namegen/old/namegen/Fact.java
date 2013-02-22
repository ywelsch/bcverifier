package namgen;
public class Fact {
  private static int x;
  public C fresh() {
    return new C(++x);
  }
  public boolean check(C o) {
    if (o == null) return true;
    return o.z <= x;
  }
}
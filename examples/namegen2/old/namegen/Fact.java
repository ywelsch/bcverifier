package namgen;
public class Fact {
  private static int x;
  public C fresh() {
    return new C(++x);
  }
  public boolean check(C o1, C o2) {
    if (o1 != null && o2 != null)
      return o1.z == o2.z;
    return false;
  }
}
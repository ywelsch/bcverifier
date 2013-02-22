package namegen;
public class Fact {
  private static int x;
  public C fresh() {
    return new C(++x);
  }
  public boolean check(C o) {
    return true;
  }
}
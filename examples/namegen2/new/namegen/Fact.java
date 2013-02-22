package namegen;
public class Fact {
  public C fresh() {
    return new C();
  }
  public boolean check(C o1, C o2) {
    if (o1 != null && o2 != null)
      return o1 == o2;
    return false;
  }
}
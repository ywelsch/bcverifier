public class C {
  public int m(int n){
    int i = 0;
    int t = 0;
    while(i < n){
      i++;
      t += i;
    }
    return t * t;
  }
}
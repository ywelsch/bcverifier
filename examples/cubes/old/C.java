public class C {
  public int m(int n){
    int i = 0;
        int s = 0;
    while(i < n){
      i++;
            s += i * i * i;
    }
    return s;
  }
}
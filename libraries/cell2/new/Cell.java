
public class Cell {
    Object c1, c2;
    int n;
    void set(Object o) {
        c1 = o;
        c2 = o;
    }
    Object get(){
        n = n +1;
        return n % 2 == 0 ? c1 : c2;
    }
}

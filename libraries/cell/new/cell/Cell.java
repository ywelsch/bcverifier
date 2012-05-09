package cell;

public class Cell {
    private Object c1, c2;
    private boolean f;
    public void set(Object o) {
        f = !f;
        if(f) c1 = o; else c2 = o;
    }
    public Object get(){
        return f ? c1 : c2;
    }
}
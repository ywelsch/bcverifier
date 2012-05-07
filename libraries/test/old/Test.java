
public class Test {
    private int i;
    private Worker worker;
    
    public void set(int i){
        this.i = i;
    }
    
    public void get() {
        worker.send(i);
    }
}

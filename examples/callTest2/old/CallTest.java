
public class CallTest {
    private Object f;
    private Worker w;
    
    public CallTest(){
    	this.w = new MyWorker();
    }

    public void set(){
    	if(w != null)
        	this.f = w.get();
    }
}

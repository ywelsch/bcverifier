public class C{
	private MyList list;

	public void m(){
		loop(0);
		return;
	}
	
	private void loop(int i){
	    if(i>=5)
	        return;
	    list.set(i, new Object());
	    loop(i+1);
	    return;
	}
}
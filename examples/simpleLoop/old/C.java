public class C{
	private MyList list;

	public void setList(MyList list) {
		this.list = list;
	}
	
	public void m(){
		for(int i=0; i<5; i++){
			list.set(i, new Object());
		}
	}
}
public class C{
	private MyList list;

	public void m(){
		int i = 0;
		while(i<5){
			list.set(i, new Object());
			i++;
		}
	}
}
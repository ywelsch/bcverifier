public class Static{
	public Static(){

	}

	public int get() {
		return Static.getTheAnswer();
	}

	public static int getTheAnswer(){
		return 42;
	}
}
public class Static{
	public Static(){

	}

	public int get() {
		return Static.getTheAnswer(3);
	}

	public static int getTheAnswer(int i){
		return i;
	}
}
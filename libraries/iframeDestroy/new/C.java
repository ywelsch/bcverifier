public class C {
	private int f;
	private I i;

	public int m() {
		this.f = 5;
		if(i != null){
			i.notifyMe();
		}
		return this.f;
	}

	public void setNotifier(I notifier) {
		this.i = notifier;
	}

	public void n() {
		this.f = 0;
	}
}
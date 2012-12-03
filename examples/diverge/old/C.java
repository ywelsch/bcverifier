public class C {
	public void m() {
		int i = 0;
		while(true) {
			if (i == 0) {
				i = 1;
			} else if (i == 1) {
				i = 0;
			} else {
				return;
			}
		}
	}
}
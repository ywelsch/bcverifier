package util;

class MyIter implements Iterator {
	private Node n;
	private Observable o;
	private int expectedModCount;
	
	MyIter(Observable o) {
		this.o = o;
		this.n = o.fst;
		this.expectedModCount = o.modCount;
	}
	
	public boolean hasNext() {
		if (o.modCount != expectedModCount)
			return false;
		return n != null; 
	}
	
	public Observer next() {
		if (o.modCount != expectedModCount)
			return null;
		if (n == null) 
			return null;
		Observer temp = n.ob;
		n = n.next;
		return temp;
	}
}
package util;

class ObsIter implements Iterator {
	private Node n;
	private Observable o;
	private int expectedModCount;
	
	ObsIter(Observable o) {
		this.o = o;
		this.n = o.snt;
		this.expectedModCount = o.modCount;
	}
	
	public boolean hasNext() {
		if (o.modCount != expectedModCount)
			return false;
		return n.getNext() != null;
	}
	
	public Observer next() {
		if (o.modCount != expectedModCount)
			return null;
		if (n.getNext() == null) 
			return null;
		n = n.getNext();
		return n.getObs();
	}
}
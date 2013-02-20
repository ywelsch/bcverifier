package util;

class ObsIter implements Iterator {
	private Node n;
	private Observable o;
	private int expModCount;
	
	ObsIter(Observable o) {
		this.o = o;
		this.n = o.snt;
		this.expModCount = o.modCount;
	}
	
	public boolean hasNext() {
		if (o.modCount != expModCount)
			return false;
		return n.getNext() != null;
	}
	
	public Observer next() {
		if (o.modCount != expModCount)
			return null;
		if (n.getNext() == null) 
			return null;
		n = n.getNext();
		return n.getObs();
	}
}
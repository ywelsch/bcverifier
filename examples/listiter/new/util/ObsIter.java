package util;

class ObsIter implements Iterator {
	private Node n;
	private Observable o;
	private int expModCount;
	
	ObsIter(Observable o) {
		this.o = o;
		this.n = o.snt.getNext();
		this.expModCount = o.modCount;
	}
	
	public boolean hasNext() {
		if (o.modCount != expModCount)
			return false;
		return n != null; 
	}
	
	public Observer next() {
		if (o.modCount != expModCount)
			return null;
		if (n == null) 
			return null;
		Observer temp = n.getObs();
		n = n.getNext();
		return temp;
	}
}
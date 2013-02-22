package util;

class ObsIter implements Iterator {
  private Node n;
  private Observable o;
  private int expectedModCount;

  ObsIter(Observable o) {
    this.o = o;
    this.n = o.snt.getNext();
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
    Observer temp = n.getObs();
    n = n.getNext();
    return temp;
  }
}
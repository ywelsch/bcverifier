package util;
public class Observable {
  Node snt =
        new Node(null, null); int modCount = 0;

  public void add(Observer ob) {
    if (ob == null) return;
    snt.setNext(new Node(ob,
                snt.getNext())); modCount++;
  }

  public Observer get(int i) {
    Node n = snt.getNext();
    for (int c = 0; c < i; c++) {
      if (n == null) return null;
      n = n.getNext();
    }
    if (n == null) return null;
    return n.getObs();
  }

  public void notifyAllObs() {
    Node n = snt.getNext();
    if (n != null) n.notifyRec();
  }
  
  public Iterator iterator() {
	  return new ObsIter(this);
  }
}
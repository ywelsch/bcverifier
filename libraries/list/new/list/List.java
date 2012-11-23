package list;

public class List {
  private Node snt = new Node();
  
  public Object getFirst() {
	  if (snt.next == null) return null;
	  return snt.next.ob;
  }

  public void add(Object ob) {
	  Node newNode = new Node();
	  newNode.ob = ob;
	  newNode.next = snt.next;
	  snt.next = newNode;
  }
  
  // returns i-th object, or null, otherwise
  public Object get(int i) {
    int c = 0;
    Node n = this.snt;
    n = n.next;
    while(c < i) {
      if (n != null) {
        n = n.next;
        c++;
      } else {
        break;
      }
    }

    if (n != null) {
      return n.ob;
    } else {
      return null;
    }
  }
  /*
  public void notifyAllObs() {
	  Node n = snt.next;
	  while (n != null) {
		  if (n.obs != null) {
			  n.obs.notifyMe();
		  }
		  n = n.next;
	  }
  }*/
}

package list;

public class List {
  private Node snt = new Node();
  
  // returns i-th object, or null, otherwise
  /*public Object get(int i) {
    int c = 0;
    Object result = null;
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
      result = n.ob;
    }

    return result;
  }*/
  
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
}

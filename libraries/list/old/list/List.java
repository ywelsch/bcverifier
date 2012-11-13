package list;

public class List {
  private Node fst;
  
  /*
  // returns i-th object, or null, otherwise
  public Object get(int i) {
    int c = 0;
    Object result = null;
    Node n = this.fst;
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
    if (fst == null) return null;
    return fst.ob;
  }
  
  public void add(Object ob) {
    Node newNode = new Node();
    newNode.ob = ob;
    newNode.next = fst;
    fst = newNode;
  }
}
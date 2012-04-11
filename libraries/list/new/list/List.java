package list;

public class LinkedList {
  private Node snt = new Node();
  // returns i-th observer, or null, otherwise
  public Observer get(int i) {
    int c = 0;
    Observer result = null;
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
  }

  public void add(Observer ob) {
    //...
  }
}

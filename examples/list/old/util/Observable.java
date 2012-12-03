package util;
public class Observable {
  private Node fst;

  public void add(Observer ob) {
    if (ob == null) return;
    Node newNode = new Node();
    newNode.ob = ob;
    newNode.next = fst;
    fst = newNode;
  }

  public Observer get(int i) {
    int c = 0;
    Node n = fst;
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

  public void notifyAllObs() {
    Node n = fst;
    while (n != null) {
      n.ob.notifyObs();
      n = n.next;
    }
    return; // dummy statement
  }
}
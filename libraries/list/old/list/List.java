package list;

public class List {
  private Node fst;

  public void add(Observer ob) {
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
  /*
  public void notifyAllObs() {
    Node n = fst;
    while (n != null) {
      if (n.obs != null) {
        n.obs.notifyMe();
      }
      n = n.next;
    }
  }*/
}
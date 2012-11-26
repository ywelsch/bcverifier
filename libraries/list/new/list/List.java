package list;

public class List {
  private Node snt = new Node(null, null);

  public void add(Observer ob) {
    snt.next = new Node(ob, snt.next);
  }

  public Observer get(int i) {
    Node n = snt.next;
    for (int c = 0; c < i; c++) {
      if (n == null) {
        break;
      } else {
        n = n.next;
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
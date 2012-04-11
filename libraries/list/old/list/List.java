package list;

public class List {
  private Node fst;
  // returns i-th observer, or null, otherwise
  public Observer get(int i) {
    int c = 0;
    Observer result = null;
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
  }
  public void add(Observer ob) {
    //...
  }
}
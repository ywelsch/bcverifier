package util;
class Node {
  private Observer ob;
  private Node next;

  Node(Observer ob, Node next) {
    this.ob = ob;
    this.next = next;
  }

  Node getNext() { return next; }

  void setNext(Node next) {
    this.next = next; }

  Observer getObs() { return ob; }

  void notifyRec() {
    ob.notifyObs();
    if (next != null) {
      next.notifyRec();
    }
    return; // dummy statement
  }
}
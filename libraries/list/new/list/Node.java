package list;

class Node {
  Node(Observer ob, Node next) {
    this.ob = ob;
    this.next = next;
  }
  Observer ob;
  Node next;
}
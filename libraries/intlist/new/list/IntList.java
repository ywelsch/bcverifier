package list;

public class IntList {
  private Node snt = new Node();
  
  public void add(int val) {
	  Node n = new Node();
	  n.value = val;
	  n.next = snt.next;
	  snt.next = n;
  }
  
  public int getFirst() {
	  if (snt.next == null) return 0;
	  return snt.next.value;
  }
}
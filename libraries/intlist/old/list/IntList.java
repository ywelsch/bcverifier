package list;

public class IntList {
  private Node fst;

  public void add(int val) {
	  Node n = new Node();
	  n.value = val;
	  n.next = fst;
	  fst = n;
  }
  
  public int getFirst() {
	  if (fst == null) return 0;
	  return fst.value;
  }
}
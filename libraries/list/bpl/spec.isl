invariant forall new List l :: l.snt != null;

invariant forall old Node n1, old Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall old List l1, old List l2 :: l1 != l2 && (l1.fst != null || l2.fst != null) ==> l1.fst != l2.fst;
invariant forall old List l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
invariant forall new List l1, new List l2 :: l1 != l2 ==> l1.snt != l2.snt;
invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall new List l, new Node n :: l.snt != n.next;

invariant forall old List l :: l.fst != null ==> createdByLibrary(l.fst);
invariant forall new List l :: createdByLibrary(l.snt);



/*
invariant (forall old List l1, new List l2 :: l1 ~ l2 ==> (l1.fst != null <==> l2.snt.next != null));
invariant (forall old List l1, new List l2 :: l1 ~ l2 && l1.fst != null && l2.snt.next != null ==> l1.fst.ob ~ l2.snt.next.ob);
*/

var binrelation bij = add(empty(), null, null);
invariant bijective(bij);
var old Node x1 = null;
var new Node x2 = null;
assign bij = if x1 != null && x2 != null then add(bij, x1, x2) else bij;
assign x1 = null;
assign x2 = null;
local place p1 = line 37 of old List assign x1 = newNode;
local place p2 = line 37 of new List assign x2 = newNode;

invariant related(bij, null, null);
invariant forall old List l1, new List l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next);
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) && n1 != null && n2 != null ==> related(bij, n1.next, n2.next);
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) && n1 != null && n2 != null ==> n1.ob ~ n2.ob;
invariant forall old java.lang.Object o1, new java.lang.Object o2 :: related(bij, o1, o2) ==> o1 instanceof old Node && o2 instanceof new Node;




// (forall old java.lang.Object o1, new java.lang.Object o2 :: related(bij, o1, o2) ==> o1 instanceof old Node && o2 instanceof new Node) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> !(n1.ob instanceof old Node) && !(n2.ob instanceof new Node)) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> createdByLibrary(n1) && !exposed(n1) && createdByLibrary(n2) && !exposed(n2)) &&


invariant forall new List l :: l.snt != null;

invariant forall old Node n1, old Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall old List l1, old List l2 :: l1 != l2 && (l1.fst != null || l2.fst != null) ==> l1.fst != l2.fst;
invariant forall old List l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
invariant forall new List l1, new List l2 :: l1 != l2 ==> l1.snt != l2.snt;
invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall new List l, new Node n :: l.snt != n.next;

invariant forall old List l :: l.fst != null ==> createdByLibrary(l.fst);
invariant forall new List l :: createdByLibrary(l.snt);




invariant (forall old List l1, new List l2 :: l1 ~ l2 ==> (l1.fst != null <==> l2.snt.next != null));
invariant (forall old List l1, new List l2 :: l1 ~ l2 && l1.fst != null && l2.snt.next != null ==> l1.fst.ob ~ l2.snt.next.ob);



invariant exists Bijection bij :: 
// related(bij, null, null) &&
// (forall old java.lang.Object o1, new java.lang.Object o2 :: related(bij, o1, o2) ==> o1 instanceof old Node && o2 instanceof new Node) &&
 (forall old List l1, new List l2 :: l1 ~ l2 ==> (l1.fst == null && l2.snt.next == null) || (l1.fst != null && l2.snt.next != null ==> related(bij, l1.fst, l2.snt.next))) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> n1.ob ~ n2.ob) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> !(n1.ob instanceof old Node) && !(n2.ob instanceof new Node)) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> related(bij, n1.next, n2.next)) &&
// (forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> createdByLibrary(n1) && !exposed(n1) && createdByLibrary(n2) && !exposed(n2)) &&
true
;


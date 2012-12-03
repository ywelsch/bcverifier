// Data properties for old library implementation
invariant forall old Observable l :: l.fst != null ==> createdByLibrary(l.fst);
invariant forall old Node n :: createdByLibrary(n) &&
  (n.next != null ==> createdByLibrary(n.next));

// Data properties for new library implementation
invariant forall new Observable l :: l.snt != null;
invariant forall new Observable l1, new Observable l2 :: l1 != l2 ==>
  l1.snt != l2.snt;
invariant forall new Observable l :: createdByLibrary(l.snt);
invariant forall new Node n :: createdByLibrary(n) &&
  (n.next != null ==> createdByLibrary(n.next));

// Data relation between libraries
var binrelation bij = add(empty(), null, null);
var old Node x1 = null;
var new Node x2 = null;
invariant bijective(bij) && related(bij, null, null) && x1 == null && x2 == null;

assign bij = if x1 != null && x2 != null then add(bij, x1, x2) else bij;
assign x1 = null;
assign x2 = null;

invariant forall old Observable l1, new Observable l2 :: l1 ~ l2 ==>
  related(bij, l1.fst, l2.snt.next);
invariant forall old Node n1, new Observable l2 :: !related(bij, n1, l2.snt);
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==>
  related(bij, n1.next, n2.next) && n1.ob ~ n2.ob &&
  n1.ob != null && n2.ob != null;

/* repeat invariants as local invariants (all in one line) */ local invariant forall old Observable l :: l.fst != null ==> createdByLibrary(l.fst); local invariant forall old Node n :: createdByLibrary(n) && (n.next != null ==> createdByLibrary(n.next)); local invariant forall new Observable l :: l.snt != null; local invariant forall new Observable l1, new Observable l2 :: l1 != l2 ==> l1.snt != l2.snt; local invariant forall new Observable l :: createdByLibrary(l.snt); local invariant forall new Node n :: createdByLibrary(n) && (n.next != null ==> createdByLibrary(n.next)); local invariant bijective(bij) && related(bij, null, null) && x1 == null && x2 == null; local invariant forall old Observable l1, new Observable l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next); local invariant forall old Node n1, new Observable l2 :: !related(bij, n1, l2.snt); local invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> related(bij, n1.next, n2.next) && n1.ob ~ n2.ob && n1.ob != null && n2.ob != null;

// add method
local place p1 = line 10 of old Observable assign x1 = newNode nosync;
local place p2 = line 7 of new Node assign x2 = this nosync;

// get method
local invariant at(q1) <==> at(q2);
local place q1 = line 18 of old Observable when c < i && n != null;
local place q2 = line 15 of new Observable when c < i && n != null;
local invariant at(q1) && at(q2) ==> related(bij, eval(q1, n), eval(q2, n)) &&
  eval(q1, c) == eval(q2, c) && eval(q1, i) == eval(q2, i) &&
  eval(q1, n) != null && eval(q2, n) != null;
local invariant at(q1) && at(q2) && related(bij, eval(q1, n), eval(q2, n)) ==>
  related(bij, eval(q1, n.next), eval(q2, n.next));

// notifyAllObs method
local invariant at(pn1) == at(pn2) && at(qn1) == at(qn2) && at(pe1) == at(pe2);
local place pcall = call notifyRec in line 24 of new Observable nosync;
local place pn1 = line 34 of old Observable when n != null;
local place pn2 = line 19 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local place qn1 = line 37 of old Observable
  stall when stackIndex(new) > 1 with measure stackIndex(new);
local place qn2 = line 23 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local invariant at(pn1) && at(pn2) ==> related(bij, eval(pn1, n), eval(pn2, this))
  && eval(pn1, n) != null && eval(pn2, this) != null;
local invariant at(pn1) && at(pn2) ==>
  related(bij, eval(pn1, n.next), eval(pn2, this.next));
local invariant at(pn1) && at(pn2) ==>
  (eval(pn1, n.next) == null <==> eval(pn2, this.next) == null) &&
  eval(pn1, n.ob) != null && eval(pn2, this.ob) != null &&
  eval(pn1, n.ob) ~ eval(pn2, this.ob);
// Additional place to help the verifier deduce the control flow
local place pe1 = line 35 of old Observable when n != null;
local place pe2 = line 20 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local invariant at(pe1) && at(pe2) ==>
  related(bij, eval(pe1, n), eval(pe2, this)) && eval(pe1, n) != null &&
  eval(pe2, this) != null;
local invariant at(pe1) && at(pe2) ==>
  related(bij, eval(pe1, n.next), eval(pe2, this.next));
local invariant at(pe1) && at(pe2) ==>
  (eval(pe1, n.next) == null <==> eval(pe2, this.next) == null);
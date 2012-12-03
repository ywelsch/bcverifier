invariant forall new Observable l :: l.snt != null;
local invariant forall new Observable l :: l.snt != null;

invariant forall old Observable l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
local invariant forall old Observable l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
invariant forall new Observable l1, new Observable l2 :: l1 != l2 ==> l1.snt != l2.snt;
local invariant forall new Observable l1, new Observable l2 :: l1 != l2 ==> l1.snt != l2.snt;
invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
local invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;

invariant forall old Observable l :: l.fst != null ==> createdByLibrary(l.fst) && !exposed(l.fst);
local invariant forall old Observable l :: l.fst != null ==> createdByLibrary(l.fst) && !exposed(l.fst);
invariant forall new Observable l :: createdByLibrary(l.snt) && !exposed(l.snt);
local invariant forall new Observable l :: createdByLibrary(l.snt) && !exposed(l.snt);
invariant forall old Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
local invariant forall old Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
invariant forall new Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
local invariant forall new Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);

invariant forall old Node n :: createdByLibrary(n) && !exposed(n);
local invariant forall old Node n :: createdByLibrary(n) && !exposed(n);
invariant forall new Node n :: createdByLibrary(n) && !exposed(n);
local invariant forall new Node n :: createdByLibrary(n) && !exposed(n);

var binrelation bij = add(empty(), null, null);
invariant bijective(bij);
local invariant bijective(bij);
var old Node x1 = null;
invariant x1 == null;
local invariant x1 == null;
var new Node x2 = null;
invariant x2 == null;
local invariant x2 == null;


assign bij = if x1 != null && x2 != null then add(bij, x1, x2) else bij;
assign x1 = null;
assign x2 = null;

invariant related(bij, null, null);
local invariant related(bij, null, null);
invariant forall old Observable l1, new Observable l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next);
local invariant forall old Observable l1, new Observable l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next);
invariant forall old Node n1, new Observable l2 :: !related(bij, n1, l2.snt);
local invariant forall old Node n1, new Observable l2 :: !related(bij, n1, l2.snt);
invariant forall old Observable l1, new Observable l2 :: related(bij, l1.fst, l2.snt.next) && l1.fst != null && l2.snt.next != null ==> l1 ~ l2;
local invariant forall old Observable l1, new Observable l2 :: related(bij, l1.fst, l2.snt.next) && l1.fst != null && l2.snt.next != null ==> l1 ~ l2;
invariant forall old java.lang.Object o1, new java.lang.Object o2 :: related(bij, o1, o2) ==> o1 instanceof old Node && o2 instanceof new Node;
local invariant forall old java.lang.Object o1, new java.lang.Object o2 :: related(bij, o1, o2) ==> o1 instanceof old Node && o2 instanceof new Node;
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> n1.ob ~ n2.ob;
local invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> n1.ob ~ n2.ob;
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> createdByLibrary(n1) && !exposed(n1) && createdByLibrary(n2) && !exposed(n2);
local invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> createdByLibrary(n1) && !exposed(n1) && createdByLibrary(n2) && !exposed(n2);
invariant forall old Node n1, new Node n2 :: related(bij, n1.next, n2.next) && n1.next != null && n2.next != null ==> related(bij, n1, n2);
local invariant forall old Node n1, new Node n2 :: related(bij, n1.next, n2.next) && n1.next != null && n2.next != null ==> related(bij, n1, n2);
invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> related(bij, n1.next, n2.next);
local invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> related(bij, n1.next, n2.next);


invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> n1.ob != null && n2.ob != null;
local invariant forall old Node n1, new Node n2 :: related(bij, n1, n2) ==> n1.ob != null && n2.ob != null;

local place p1 = line 10 of old Observable assign x1 = newNode nosync;
local place p2 = line 7 of new Node assign x2 = this nosync;

local place q1 = line 18 of old Observable when c < i && n != null;
local place q2 = line 15 of new Observable when c < i && n != null;
local invariant at(q1) <==> at(q2);
local invariant at(q1) && at(q2) ==> related(bij, eval(q1, n), eval(q2, n));
local invariant at(q1) && at(q2) && eval(q1, n) != null && eval(q2, n) != null && related(bij, eval(q1, n), eval(q2, n)) ==> related(bij, eval(q1, n.next), eval(q2, n.next));
local invariant at(q1) && at(q2) ==> eval(q1, c) == eval(q2, c);
local invariant at(q1) && at(q2) ==> eval(q1, i) == eval(q2, i);

local place pcall = call notifyRec in line 24 of new Observable nosync;
local place pn1 = line 34 of old Observable when n != null;
local place pn2 = line 19 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local invariant at(pn1) <==> at(pn2);
local invariant at(pn1) && at(pn2) ==> related(bij, eval(pn1, n), eval(pn2, this));
local invariant at(pn1) && at(pn2) ==> eval(pn1, n) != null && eval(pn2, this) != null;
local invariant at(pn1) && at(pn2) && eval(pn1, n) != null && eval(pn2, this) != null ==> related(bij, eval(pn1, n.next), eval(pn2, this.next));
local invariant at(pn1) && at(pn2) && eval(pn1, n) != null && eval(pn2, this) != null ==> (eval(pn1, n.next) == null <==> eval(pn2, this.next) == null);
local invariant at(pn1) && at(pn2) && eval(pn1, n) != null && eval(pn2, this) != null ==> eval(pn1, n.ob) != null && eval(pn2, this.ob) != null && eval(pn1, n.ob) ~ eval(pn2, this.ob);

local place pe1 = line 35 of old Observable when n != null;
local place pe2 = line 20 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local invariant at(pe1) <==> at(pe2);
local invariant at(pe1) && at(pe2) ==> related(bij, eval(pe1, n), eval(pe2, this));
local invariant at(pe1) && at(pe2) ==> eval(pe1, n) != null && eval(pe2, this) != null;
local invariant at(pe1) && at(pe2) && eval(pe1, n) != null && eval(pe2, this) != null ==> related(bij, eval(pe1, n.next), eval(pe2, this.next));
local invariant at(pe1) && at(pe2) && eval(pe1, n) != null && eval(pe2, this) != null ==> (eval(pe1, n.next) == null <==> eval(pe2, this.next) == null);

local place pf1 = line 37 of old Observable stall when stackIndex(new) > 1 with measure stackIndex(new);
local place pf2 = line 23 of new Node when stackIndex(new) > 0 && at(pcall, 0);
local invariant at(pf1) <==> at(pf2);

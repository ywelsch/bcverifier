invariant forall new List l :: l.snt != null;
local invariant forall new List l :: l.snt != null;

invariant forall old Node n1, old Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
local invariant forall old Node n1, old Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall old List l1, old List l2 :: l1 != l2 && (l1.fst != null || l2.fst != null) ==> l1.fst != l2.fst;
local invariant forall old List l1, old List l2 :: l1 != l2 && (l1.fst != null || l2.fst != null) ==> l1.fst != l2.fst;
invariant forall old List l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
local invariant forall old List l, old Node n :: l.fst != null || n.next != null ==> l.fst != n.next;
invariant forall new List l1, new List l2 :: l1 != l2 ==> l1.snt != l2.snt;
local invariant forall new List l1, new List l2 :: l1 != l2 ==> l1.snt != l2.snt;
invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
local invariant forall new Node n1, new Node n2 :: n1 != n2 && (n1.next != null || n2.next != null) ==> n1.next != n2.next;
invariant forall new List l, new Node n :: l.snt != n.next;
local invariant forall new List l, new Node n :: l.snt != n.next;

invariant forall old List l :: l.fst != null ==> createdByLibrary(l.fst) && !exposed(l.fst);
local invariant forall old List l :: l.fst != null ==> createdByLibrary(l.fst) && !exposed(l.fst);
invariant forall new List l :: createdByLibrary(l.snt) && !exposed(l.snt);
local invariant forall new List l :: createdByLibrary(l.snt) && !exposed(l.snt);
invariant forall old Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
local invariant forall old Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
invariant forall new Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);
local invariant forall new Node n :: n.next != null ==> createdByLibrary(n.next) && !exposed(n.next);

// aliasing
      invariant forall new Node n1, new Node n2 :: n1.next == n2.next && n1.next != null ==> n1 == n2;
local invariant forall new Node n1, new Node n2 :: n1.next == n2.next && n1.next != null ==> n1 == n2; 
      invariant forall old Node n1, old Node n2 :: n1.next == n2.next && n1.next != null ==> n1 == n2;
local invariant forall old Node n1, old Node n2 :: n1.next == n2.next && n1.next != null ==> n1 == n2; 

      invariant forall new List l1, new List l2 :: l1.snt == l2.snt && l1.snt != null ==> l1 == l2;
local invariant forall new List l1, new List l2 :: l1.snt == l2.snt && l1.snt != null ==> l1 == l2;
      invariant forall old List l1, old List l2 :: l1.fst == l2.fst && l1.fst != null ==> l1 == l2;
local invariant forall old List l1, old List l2 :: l1.fst == l2.fst && l1.fst != null ==> l1 == l2;

// the sentinel is not in bij with any other node:
      invariant forall old Node n1, new List l2 :: !related(bij, n1, l2.snt);
local invariant forall old Node n1, new List l2 :: !related(bij, n1, l2.snt);

// when a node points to x2, then is is a sentinel node of some list
      invariant forall new Node n :: x2 != null && n.next == x2 ==> (exists new List l :: l.snt == n);
local invariant forall new Node n :: x2 != null && n.next == x2 ==> (exists new List l :: l.snt == n);   

var binrelation bij = add(empty(), null, null);
invariant bijective(bij);
local invariant bijective(bij);
var old Node x1 = null;
invariant x1 == null;
local invariant x1 == null;
var new Node x2 = null;
invariant x2 == null;
local invariant x2 == null;

local place p1 = line 15 of old List assign x1 = newNode;
local place p2 = line 15 of new List assign x2 = newNode;

local place pl1 = line 23 of old List when c < i splitvc;
local place pl2 = line 24 of new List when c < i splitvc;

local invariant at(pl1) <==> at(pl2);
local invariant at(pl1) && at(pl2) ==> related(bij, eval(pl1, n), eval(pl2, n));
local invariant at(pl1) && at(pl2) ==> eval(pl1, c) == eval(pl2, c);
local invariant at(pl1) && at(pl2) ==> eval(pl1, i) == eval(pl2, i);


local place pe1 = line 31 of old List splitvc;
local place pe2 = line 32 of new List splitvc;

local invariant at(pe1) && at(pe2) ==> related(bij, eval(pe1, n), eval(pe2, n));

assign bij = if x1 != null && x2 != null then add(bij, x1, x2) else bij;
assign x1 = null;
assign x2 = null;

invariant related(bij, null, null);
local invariant related(bij, null, null);
invariant forall old List l1, new List l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next);
local invariant forall old List l1, new List l2 :: l1 ~ l2 ==> related(bij, l1.fst, l2.snt.next);
invariant forall old List l1, new List l2 :: related(bij, l1.fst, l2.snt.next) && l1.fst != null && l2.snt.next != null ==> l1 ~ l2;
local invariant forall old List l1, new List l2 :: related(bij, l1.fst, l2.snt.next) && l1.fst != null && l2.snt.next != null ==> l1 ~ l2;
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
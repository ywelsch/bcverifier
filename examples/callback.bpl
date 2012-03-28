// Callback example
const unique cbLoc1: bool; // whether top of stack is at location Loc1
const unique cbLoc2: bool; // whether top of stack is at location Loc2
const unique this1: Ref; // this value of top of stack1
const unique this2: Ref; // this value of top of stack2

const unique Bij1.v: Field Ref;
const unique Bij2.v: Field Ref;

function BijInv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) {
  ( forall o1, o2: Ref :: related[o1, o2] ==> related[heap1[o1, Bij1.v], heap2[o2, Bij2.v]] )
  &&
  ( cbLoc1 && cbLoc2 ==> related[this1, this2] )
  &&
  SystemInv(heap1, heap2, related)
}


procedure checkBij(o1:Ref, o2:Ref)
  modifies m1_receiver, m2_receiver, m1_param, m2_param, heap1, heap2, related;
{
  assume RelM(m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related);
  assume BijInv(heap1, heap2, related);

  assert m1_receiver != null;
  m1_param := heap1[m1_receiver, Bij1.v];

  assert m2_receiver != null;
  m2_param := heap2[m2_receiver, Bij2.v];

  assert RelatedOrFreshMessages(m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related);
  call relateM();
  assert BijInv(heap1, heap2, related);
}
// Refactoring example
// const unique Cell2.i: Field Ref;
//
// function RefactInv(heap1: HeapType, heap2: HeapType, related: SimulType) returns (bool) {
//   ( forall o1, o2: Ref :: related[o1, o2] ==> related[heap1[o1, Cell1.v], heap2[o2, Cell2.i]] )
//   &&
//   ( forall o1, o2: Ref :: o1 != null && o2 != null && related[o1, o2] && !heap2[o2, Cell2.f] ==> related[heap1[o1, Cell1.v], heap2[o2, Cell2.v2]] )
//   &&
//   SystemInv(heap1, heap2, related)
// }
//
// procedure checkRefactGet()
//   modifies heap1, heap2, related;
// {
//   var res1: Ref;
//   var res2: Ref;
// }

//
// ...
//
// package X;
//
// ...
//
// ... class C {
//   ...
//   private I i;
//
//
//     $e.i$ where $e$ of type $X.C$
//
//     $e.i = e'$ where $e$ of type $X.C$
//
//
//
// }
//
// transformed to
// ...
//
// package X;
//
// ...
//
// ... class C {
//   ...
//   private Cell c;
//
//
//     let $x$ = $e$ in
//         if $x.c$ == null then null else (I)$x.c$.get();
//
//     let $x$ = $e$ in
//         if $x.c$ == null then $x.c$ = new Cell() else null;
//         $x.c$.set($e'$); (I)$x.c$.get()
// }
//
// class Cell {
//   Object v;
//   Object get() { return v; }
//   Object set(Object newV) { this.v = newV; }
// }

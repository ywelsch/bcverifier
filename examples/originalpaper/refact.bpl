// Refactoring example
// const unique Cell2.i: Field Ref;
//
// function RefactInv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) {
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

// PRELUDE

type Ref;
const null: Ref;

type Field _;
type Heap = <alpha>[Ref, Field alpha] alpha;

const unique alloc, exposed, createdByCtxt: Field bool;

type Class;
const dynType: Field Class;

function AllocObj(r:Ref, heap:Heap) returns (bool) { r != null && heap[r,alloc] }

type Bij = [Ref, Ref] bool;
axiom ( forall related:Bij, heap1:Heap, heap2:Heap, r1,r2:Ref :: related[r1,r2] ==> AllocObj(r1,heap1) && AllocObj(r2,heap2) );
axiom ( forall related:Bij, r1,r2,r3,r4:Ref :: related[r1,r2] && related[r3,r4] ==> (r1 == r3 <==> r2 == r4) );

var heap1: Heap where WellformedHeap(heap1);
var heap2: Heap where WellformedHeap(heap2);
var related: Bij where WellformedCoupling(heap1,heap2,related);

// Consistent update (ensures axioms and allows null values to be passed)
procedure {:inline 1} Update(r1:Ref, r2:Ref) modifies heap1,heap2,related; {
  assert RelNull(r1,r2,related) || ( !(exists r:Ref :: related[r1,r]) && !(exists r:Ref :: related[r,r2]) );
  if (r1 != null && r2 != null) { heap1[r1,exposed] := true; heap2[r2,exposed] := true; related[r1,r2] := true; }
}

function WellformedHeap(heap: Heap) returns (bool) {
  heap[null,alloc] &&
  ( forall r:Ref, f:Field Ref :: heap[r,alloc] ==> heap[heap[r,f],alloc] ) &&
  ( forall r:Ref, f:Field Ref :: !AllocObj(r,heap) ==> heap[r,f] == null ) &&
  ( forall r:Ref, f:Field int :: !heap[r,alloc] ==> heap[r,f] == 0 ) &&
  ( forall r:Ref, f:Field bool :: !heap[r,alloc] ==> heap[r,f] == false )
  // and more ...
}

function WellformedCoupling(heap1: Heap, heap2: Heap, related:Bij) returns (bool) {
  ( forall r1,r2:Ref :: related[r1,r2] ==> heap1[r1,exposed] && heap2[r2,exposed] ) &&
  ( forall r1:Ref :: AllocObj(r1,heap1) && heap1[r1,exposed] ==> (exists r2:Ref :: related[r1,r2]) ) &&
  ( forall r2:Ref :: AllocObj(r2,heap2) && heap2[r2,exposed] ==> (exists r1:Ref :: related[r1,r2]) )
  // and more ...
}

function RelNull(r1:Ref, r2:Ref, related:Bij) returns (bool) {
  (r1 == null && r2 == null) || (r1 != null && r2 != null && related[r1,r2])
}

function NonNull(c: Class, f: Field Ref, heap:Heap) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap) && RefOfType(r,heap,c) ==> heap[r,f] != null )
}

function Internal(c: Class, f: Field Ref, heap:Heap) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap) && RefOfType(r,heap,c) ==> !heap[heap[r,f],createdByCtxt] )
}

const unique Object: Class;
type Method;
const unique rtrn: Method;

procedure {:inline 1} UpdateM(m1_receiver:Ref, m2_receiver:Ref, m1_method:Method, m2_method:Method, m1_param:Ref, m2_param:Ref)
  modifies heap1,heap2,related;
{
  assert m1_method == m2_method;
  assert m1_method != rtrn ==> m1_receiver != null && m2_receiver != null;
  if (m1_method != rtrn) {
    call Update(m1_receiver,m2_receiver);
  }
  call Update(m1_param,m2_param);
}

function RelM(m1_receiver:Ref, m2_receiver:Ref, m1_method:Method, m2_method:Method, m1_param:Ref, m2_param:Ref, related:Bij) returns (bool) {
  (m1_method == m2_method) && (m1_method != rtrn ==> related[m1_receiver,m2_receiver]) && RelNull(m1_param,m2_param,related)
}

function RefOfType(o:Ref, heap:Heap, t:Class) returns (bool) {
  AllocObj(o, heap) ==> heap[o, dynType] == t // for simplicity no subtyping at the moment
}

function FType(heap:Heap, c:Class, f: Field Ref, t:Class) returns (bool) {
  ( forall o: Ref :: AllocObj(o, heap) && heap[o, dynType] == c ==> RefOfType(heap[o, f], heap, t) ) &&
  ( forall o: Ref :: AllocObj(o, heap) && heap[o, dynType] != c ==> heap[o, f] == null )
}

function Unique(c: Class, f:Field Ref, heap:Heap) returns (bool) {
  ( forall r1,r2:Ref :: AllocObj(r1,heap) && AllocObj(r2,heap) && RefOfType(r1,heap,c) && RefOfType(r2,heap,c) && r1 != r2 ==> heap[r1,f] != heap[r2,f] )
}

// Extensionality for simulations:
axiom ( forall r1,r2:Ref, rel:Bij :: rel[r1,r2 := rel[r1,r2]] == rel );
// Extensionality for heaps:
axiom ( forall<alpha> r:Ref, f:Field alpha, heap:Heap :: heap[r,f := heap[r,f]] == heap );
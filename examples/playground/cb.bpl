// BEGIN PRELUDE

type Ref;
const null: Ref;

type Field _;
type Heap = <alpha>[Ref, Field alpha] alpha;

const unique alloc, exposed, createdByCtxt: Field bool;

type Class;
const dynType: Field Class;

function AllocObj(r:Ref, heap:Heap) returns (bool) { r != null && heap[r,alloc] }

type Bij = [Ref, Ref] bool;
//axiom ( forall related:Bij :: { Bijective(related) } Bijective(related) );

var heap1: Heap where WellformedHeap(heap1);
var heap2: Heap where WellformedHeap(heap2);
var related: Bij where WellformedCoupling(heap1,heap2,related);

//function Related(r1:Ref, r2:Ref) returns bool;

function Bijective(related:Bij) returns (bool) {
  ( forall r1,r2,r3,r4:Ref :: related[r1,r2] && related[r3,r4] ==> (r1 == r3 <==> r2 == r4) )
}

// Consistent update (ensures axiom and allows null values to be passed)
procedure {:inline 1} Update(r1:Ref, r2:Ref) modifies heap1,heap2,related; {
  assert RelNull(r1,r2,related) || ( !(exists r:Ref :: related[r1,r]) && !(exists r:Ref :: related[r,r2]) );
  if (r1 != null && r2 != null) { heap1[r1,exposed] := true; heap2[r2,exposed] := true; related[r1,r2] := true; }
  //assert Bijective(related);
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
  Bijective(related) &&
  ObjectCoupling(heap1,heap2,related) &&
  ( forall r1,r2:Ref :: related[r1,r2] ==> heap1[r1,exposed] && heap2[r2,exposed] ) &&
  ( forall r1:Ref :: AllocObj(r1,heap1) && heap1[r1,exposed] ==> (exists r2:Ref :: related[r1,r2]) ) &&
  ( forall r2:Ref :: AllocObj(r2,heap2) && heap2[r2,exposed] ==> (exists r1:Ref :: related[r1,r2]) )
  // and more ...
}

function ObjectCoupling(heap1: Heap, heap2: Heap, related:Bij) returns (bool) {
  ( forall r1,r2:Ref :: related[r1,r2] ==> AllocObj(r1,heap1) && AllocObj(r2,heap2) )
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

function Unique(c:Class, f:Field Ref, heap:Heap) returns (bool) {
  ( forall r1,r2:Ref :: AllocObj(r1,heap) && AllocObj(r2,heap) && RefOfType(r1,heap,c) && RefOfType(r2,heap,c) && r1 != r2 ==> heap[r1,f] != heap[r2,f] )
}

// Extensionality for simulations:
axiom ( forall r1,r2:Ref, rel:Bij :: rel[r1,r2 := rel[r1,r2]] == rel );
// Extensionality for heaps:
axiom ( forall<alpha> r:Ref, f:Field alpha, heap:Heap :: heap[r,f := heap[r,f]] == heap );


type Var _; // Local variables (used to model stack)
type StackPtr = int;
axiom ( forall sp:StackPtr :: sp >= 0);
var sp1: StackPtr;
var sp2: StackPtr;
type StackFrame = <alpha>[Var alpha] alpha;
type Stack = [StackPtr] StackFrame; // similar to curried version of heap, only consider a single interaction frame
var stack1: Stack where WellformedStack(stack1, sp1, heap1);
var stack2: Stack where WellformedStack(stack2, sp2, heap2);

const unique this: Var Ref;

function WellformedStack(stack:Stack, sp:StackPtr, heap:Heap) returns (bool) {
  ( forall p:StackPtr, v:Var Ref :: p <= sp ==> heap[stack[p][v], alloc] ) &&
  ( forall p:StackPtr :: p <= sp ==> AllocObj(stack[p][this], heap) ) &&
  ( forall p:StackPtr, v:Var Ref :: p > sp ==> stack[p][v] == null ) &&
  ( forall p:StackPtr, v:Var int :: p > sp ==> stack[p][v] == 0 ) &&
  ( forall p:StackPtr, v:Var bool :: p > sp ==> stack[p][v] == false )
  // and more ...
}

// only holds if we have no internal method calls
var additionalCondsBetweenStacks: bool where related[stack1[0][this], stack2[0][this]];

// Extensionality for Stack:
axiom ( forall sp:StackPtr, stack:Stack :: stack[sp := stack[sp]] == stack );

type Kind;
const unique intKind: Kind;
const unique refKind: Kind;
const unique boolKind: Kind;
axiom ( forall k:Kind :: k == intKind || k == refKind || k == boolKind );


var m1_receiver,m1_param,m2_receiver,m2_param: Ref;
var m1_method,m2_method: Method;

const unique class: Var Class;
const unique method: Var Method;

function ifThenElse<alpha>(bool, alpha, alpha) returns (alpha);
axiom (forall<alpha> b:bool, x:alpha, y:alpha :: b ==> ifThenElse(b,x,y) == x);
axiom (forall<alpha> b:bool, x:alpha, y:alpha :: !b ==> ifThenElse(b,x,y) == y);

function isPublic(Class) returns (bool);
axiom isPublic(Object);
//function isClass(Class) returns (bool);
axiom (forall t: Class :: Object <: t ==> t == Object);

function definesMethod(Class,Method) returns (bool);

type Address;

// END PRELUDE


const unique A, C: Class;
axiom isPublic(A);
axiom !isPublic(C);
axiom C <: Object;
axiom A <: Object;
axiom !(C <: A);
axiom !(A <: C);
axiom (forall t:Class :: C <: t ==> t == C || Object <: t); // maybe better encoding with "complete"?

const unique g: Field int;
const unique run,exec,inc: Method;

axiom (forall m:Method :: definesMethod(A,m) <==> m == exec || m == inc);
axiom (forall t:Class, m:Method :: t <: C ==> !definesMethod(t,m));

const unique exec_begin, inc_begin, exec_invoc_call : Address; // one for each entry point

const unique c: Var Ref;
const unique pos: Var bool;  // ghost variable

function Inv(heap1:Heap, heap2:Heap, stack1:Stack, stack2:Stack, sp1:StackPtr, sp2:StackPtr, related:Bij) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap1) ==> heap1[r,g] % 2 == 0 ) &&
  sp1 == 0 && sp2 == 0 && RelNull(stack1[sp1][c], stack2[sp2][c], related) &&
  stack1[sp1][pos] == stack2[sp2][pos]
}

var rega1, rega2, regb1, regb2, regc1, regc2 : Ref;
var regaint1, regaint2, regbint1, regbint2: int;
var regabool1, regabool2, regbbool1, regbbool2: bool;
const retA : Address;
  
procedure Check_CB_Ctxt_MGC_New()
  modifies heap1, heap2, stack1, stack2, sp1, sp2, m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related;
{
  var fresh1, fresh2 : Ref;
  var classT : Class;
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related);
  assume isPublic(classT);
  assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == classT;
  heap1[fresh1,alloc] := true;
  assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == classT;
  heap2[fresh2,alloc] := true;
  assert Inv(heap1,heap2,stack1,stack2,sp1,sp2,related);
}

procedure Check_CB_Sanity_Inv()
  modifies heap1, heap2, stack1, stack2, sp1, sp2, m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related;
{
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related);
  assert false;
}


procedure Check_CB()
  modifies heap1, heap2, stack1, stack2, sp1, sp2, m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related;
{
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related);
  assume RelM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param,related);
  if (m1_method != rtrn) {
    assume sp1 == 0 && sp2 == 0;
  }

  if (m1_method == exec) {
    stack1[sp1][this] := m1_receiver;
    stack1[sp1][c] := m1_param;
    if (stack1[sp1][c] != null) {
      stack1[sp1][pos] := true;
      // output label: call c.run()
      m1_receiver, m1_method, m1_param := stack1[sp1][c], run, null;
      goto L2;
      RtrnCBexec1:
    }
    if (heap1[stack1[sp1][this],g] % 2 == 0) {
      if (sp1 == 0) {
        // output label: rtrn c
        m1_method, m1_param := rtrn, stack1[sp1][c];
        goto L2;
      } else { assert false; }
    } else {
      if (sp1 == 0) {
        // output label: rtrn null
        m1_method, m1_param := rtrn, null;
        goto L2;
      } else { assert false; }
    }
  } else
  if (m1_method == rtrn) {
    if (stack1[sp1][pos]) { goto RtrnCBexec1; }
    else { goto RtrnCBinc1; }
  } else
  if (m1_method == inc) {
    stack1[sp1][this] := m1_receiver;
    stack1[sp1][c] := m1_param;
    heap1[stack1[sp1][this],g] := heap1[stack1[sp1][this],g] + 2;
    if (stack1[sp1][c] != null) {
      stack1[sp1][pos] := false;
      // call c.run()
      m1_receiver, m1_method, m1_param := stack1[sp1][c], run, null;
      goto L2;
      RtrnCBinc1:
    }
    if (sp1 == 0) {
      // void rtrn, use null value
      m1_method, m1_param := rtrn, null;
      goto L2;
    } else { assert false; }
  }
  assume false;

  L2:
  if (m2_method == exec) {
    stack2[sp2][this] := m2_receiver;
    stack2[sp2][c] := m2_param;
    if (stack2[sp2][c] != null) {
      stack2[sp2][pos] := true;
      // output label: call c.run()
      m2_receiver, m2_method, m2_param := stack2[sp2][c], run, null;
      goto Checking;
      RtrnCBexec2:
    }
    if (sp2 == 0) {
      // output label: rtrn c
      m2_method, m2_param := rtrn, stack2[sp2][c];
      goto Checking;
    } else { assert false; }
  } else
  if (m2_method == rtrn) {
    if (stack2[sp2][pos]) { goto RtrnCBexec2; }
    else { goto RtrnCBinc2; }
  } else
  if (m2_method == inc) {
    stack2[sp2][this] := m2_receiver;
    stack2[sp2][c] := m2_param;
    heap2[stack2[sp2][this],g] := heap2[stack2[sp2][this],g] + 2;
    if (stack2[sp2][c] != null) {
      stack2[sp2][pos] := false;
      // call c.run()
      m2_receiver, m2_method, m2_param := stack2[sp2][c], run, null;
      goto Checking;
      RtrnCBinc2:
    }
    if (sp2 == 0) {
      // void rtrn, use null value
      m2_method, m2_param := rtrn, null;
      goto Checking;
    } else { assert false; }
  }
  assume false;

  Checking:
  call UpdateM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param);
  assert Inv(heap1,heap2,stack1,stack2,sp1,sp2,related);
}

/* The modulo operation */
axiom (forall x: int, y: int :: {x % y} {x / y} x % y == x - (x / y) * y);
axiom (forall x: int, y: int :: {x % y} (0 < y ==> 0 <= x % y && x % y < y) && (y < 0 ==> y < x % y && x % y <= 0));
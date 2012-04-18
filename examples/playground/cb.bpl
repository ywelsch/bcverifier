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
  assert m1_method == stack2[sp2][meth];
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
//axiom ( forall sp:StackPtr :: sp >= 0);
var sp1: StackPtr;
var sp2: StackPtr;
type StackFrame = <alpha>[Var alpha] alpha;
type Stack = [StackPtr] StackFrame; // similar to curried version of heap, only consider a single interaction frame
var stack1: Stack where WellformedStack(stack1, sp1, heap1);
var stack2: Stack where WellformedStack(stack2, sp2, heap2);

const unique this: Var Ref;

function WellformedStack(stack:Stack, sp:StackPtr, heap:Heap) returns (bool) {
  ( forall p:StackPtr, v:Var Ref :: 0 <= p && p <= sp ==> heap[stack[p][v], alloc] ) &&
  ( forall p:StackPtr :: 0 <= p && p <= sp ==> AllocObj(stack[p][this], heap) ) &&
  ( forall p:StackPtr, v:Var Ref :: p < 0 || p > sp ==> stack[p][v] == null ) 
//  ( forall p:StackPtr, v:Var int :: p < 0 || p > sp ==> stack[p][v] == 0 ) &&
//  ( forall p:StackPtr, v:Var bool :: p < 0 || p > sp ==> stack[p][v] == false )
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
//var m1_method,stack2[sp2][meth]: Method;

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

var isCall: bool;

const unique place : Var Address;
const unique retA : Address;
const unique meth: Var Method;
const unique receiver, param1: Var Ref;
//const unique isCall: Var bool;

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
const unique run,exec,inc,self: Method;

axiom (forall m:Method :: definesMethod(A,m) <==> m == exec || m == inc);
axiom (forall t:Class, m:Method :: t <: C ==> !definesMethod(t,m));

const unique exec_begin, inc_begin, exec_invoc_run1, exec_invoc_run2, exec_invoc_self, self_begin : Address; // one for each entry point

const unique c, a: Var Ref;
const unique pos: Var bool;  // ghost variable

function Inv(heap1:Heap, heap2:Heap, stack1:Stack, stack2:Stack, sp1:StackPtr, sp2:StackPtr, related:Bij, place: Var Address) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap1) ==> heap1[r,g] % 2 == 0 ) &&
  sp1 == 0 && sp2 == 0 && RelNull(stack1[sp1][c], stack2[sp2][c], related) &&
  stack1[sp1][place] == stack2[sp2][place]
}

var rega1, rega2, regb1, regb2, regc1, regc2 : Ref;
var regaint1, regaint2, regbint1, regbint2: int;
var regabool1, regabool2, regbbool1, regbbool2: bool;



  /* 
procedure Check_CB_Ctxt_MGC_New()
  modifies heap1, heap2, stack1, stack2, sp1, sp2, m1_receiver, m2_receiver, m1_param, m2_param, related;
{
  var fresh1, fresh2 : Ref;
  var classT : Class;
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related,place);
  assume isPublic(classT);
  assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == classT;
  heap1[fresh1,alloc] := true;
  assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == classT;
  heap2[fresh2,alloc] := true;
  assert Inv(heap1,heap2,stack1,stack2,sp1,sp2,related,place);
}

procedure Check_CB_Sanity_Inv()
  modifies heap1, heap2, stack1, stack2, sp1, sp2, m1_receiver, m2_receiver, m1_param, m2_param, related;
{
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related,place);
  //assert false;
}*/

procedure Check_CB()
  modifies isCall, heap1, heap2, stack1, stack2, sp1, sp2, related;
{
  assume Inv(heap1,heap2,stack1,stack2,sp1,sp2,related,place);
  assume RelM(stack1[sp1][receiver],stack2[sp2][receiver],stack1[sp1][meth],stack2[sp2][meth],stack1[sp1][param1],stack2[sp2][param1],related);
  if (stack1[sp1][meth] != rtrn) {
    assume sp1 == 0 && sp2 == 0;
  } else {
  	assume sp1 >= 0 && sp2 >= 0;
  }
  
  dispatch1:
  if (isCall && stack1[sp1][meth] == exec) { goto exec_begin1; } else
  if (isCall && stack1[sp1][meth] == inc) { goto inc_begin1; } else
  //if (isCall && m1_method == self) { goto self_begin1; } else
  if (!isCall && stack1[sp1][meth] == run) { goto exec_invoc_run1_1, exec_invoc_run1_2; } else
  //if (!isCall && m1_method == self) { goto exec_invoc_self; } else
  { return; }
  
//  dispatch1callinternal:
//  if (stack1[sp1][place] == exec_begin) { goto exec_begin1; } else
//  if (stack1[sp1][place] == inc_begin) { goto inc_begin1; } else
//  if (stack1[sp1][place] == self_begin) { goto self_begin1; } else
//  { return; }
  
    self_begin1:
    assume stack1[sp1][place] == self_begin;
    
    //rtrn
    if (sp1 == 0) {
      // void rtrn, use null value
      stack1[sp1][place] := inc_begin;
      isCall := false; stack1[sp1][meth] := self; stack1[sp1][param1] := stack1[sp1][a];
      goto dispatch2;
    } else { 
    	stack1[sp1][place] := inc_begin;
        isCall := false; stack1[sp1][meth] := rtrn; stack1[sp1][param1] := stack1[sp1][a];
    	goto dispatch1;
    	//assert false;
    }
    
  
  	exec_begin1:
  	assume stack1[sp1][place] == exec_begin;
    stack1[sp1][this] := stack1[sp1][receiver];
    stack1[sp1][c] := stack1[sp1][param1];
    if (stack1[sp1][c] != null) {
      stack1[sp1][pos] := true;
      // output label: call c.run()
      stack1[sp1][place] := exec_invoc_run1;
      stack1[sp1][receiver] := stack1[sp1][c]; stack1[sp1][meth] := run; stack1[sp1][param1] := null;
      goto dispatch2;
      exec_invoc_run1_1:
      assume stack1[sp1][place] == exec_invoc_run1;
    }
    if (heap1[stack1[sp1][this],g] % 2 == 0) {
      if (sp1 == 0) {
        // output label: rtrn c
        stack1[sp1][place] := exec_begin;
        stack1[sp1][meth] := rtrn; stack1[sp1][param1] := stack1[sp1][c];
        goto dispatch2;
      } else { assert false; }
    } else {
      // call c := this.self(c)
      stack1[sp1][place] := exec_invoc_self;
      isCall := true; stack1[sp1][receiver] := stack1[sp1][this]; stack1[sp1][meth] := self; stack1[sp1][param1] := stack1[sp1][c];
      sp1 := sp1 + 1;
      exec_invoc_self:
      sp1 := sp1 - 1;
      stack1[sp1][c] := stack1[sp1][param1];
      // rtrn c
      if (sp1 == 0) {
        // output label: rtrn c
        stack1[sp1][place] := exec_begin;
        isCall := false; stack1[sp1][meth] := rtrn; stack1[sp1][param1] := stack1[sp1][c];
        goto dispatch2;
      } else { assert false; }
    }
  
  	inc_begin1:
  	assume stack1[sp1][place] == inc_begin;
    stack1[sp1][this] := stack1[sp1][receiver];
    stack1[sp1][c] := stack1[sp1][param1];
    heap1[stack1[sp1][this],g] := heap1[stack1[sp1][this],g] + 2;
    if (stack1[sp1][c] != null) {
      stack1[sp1][pos] := false;
      // call c.run()
      stack1[sp1][place] := exec_invoc_run2;
      stack1[sp1][receiver] := stack1[sp1][c]; stack1[sp1][meth] := run; stack1[sp1][param1] := null;
      goto dispatch2;
      exec_invoc_run1_2:
      assume stack1[sp1][place] == exec_invoc_run2;
    }
    if (sp1 == 0) {
      // void rtrn, use null value
      stack1[sp1][place] := inc_begin;
      stack1[sp1][meth] := rtrn; stack1[sp1][param1] := null;
      goto dispatch2;
    } else { assert false; }

  dispatch2:
  if (isCall && stack2[sp2][meth] == exec) { goto exec_begin2; } else
  if (isCall && stack2[sp2][meth] == inc) { goto inc_begin2; } else
  if (!isCall && stack2[sp2][meth] == run) { goto exec_invoc_run2_1, exec_invoc_run2_2; } else
  { return; }

  	exec_begin2:
  	assume stack2[sp2][place] == exec_begin;
    stack2[sp2][this] := stack2[sp2][receiver];
    stack2[sp2][c] := stack2[sp2][param1];
    if (stack2[sp2][c] != null) {
      stack2[sp2][pos] := true;
      // output label: call c.run()
      stack2[sp2][place] := exec_invoc_run1;
      stack2[sp2][receiver] := stack2[sp2][c]; stack2[sp2][meth] := run; stack2[sp2][param1] := null;
      goto Checking;
      exec_invoc_run2_1:
      assume stack2[sp2][place] == exec_invoc_run1;
    }
    if (sp2 == 0) {
      // output label: rtrn c
      stack2[sp2][place] := exec_begin;
      stack2[sp2][meth] := rtrn; stack2[sp2][param1] := stack2[sp2][c];
      goto Checking;
    } else { assert false; }
    
  	inc_begin2:
  	assume stack2[sp2][place] == inc_begin;
    stack2[sp2][this] := stack2[sp2][receiver];
    stack2[sp2][c] := stack2[sp2][param1];
    heap2[stack2[sp2][this],g] := heap2[stack2[sp2][this],g] + 2;
    if (stack2[sp2][c] != null) {
      stack2[sp2][pos] := false;
      // call c.run()
      stack2[sp2][place] := exec_invoc_run2;
      stack2[sp2][receiver] := stack2[sp2][c]; stack2[sp2][meth] := run; stack2[sp2][param1] := null;
      goto Checking;
      exec_invoc_run2_2:
      assume stack2[sp2][place] == exec_invoc_run2;
    }
    if (sp2 == 0) {
      // void rtrn, use null value
      stack2[sp2][place] := inc_begin;
      stack2[sp2][meth] := rtrn; stack2[sp2][param1] := null;
      goto Checking;
    } else { assert false; }

  Checking:
  call UpdateM(stack1[sp1][receiver],stack2[sp2][receiver],stack1[sp1][meth],stack2[sp2][meth],stack1[sp1][param1],stack2[sp2][param1]);
  assert Inv(heap1,heap2,stack1,stack2,sp1,sp2,related,place);
}

/* The modulo operation */
axiom (forall x: int, y: int :: {x % y} {x / y} x % y == x - (x / y) * y);
axiom (forall x: int, y: int :: {x % y} (0 < y ==> 0 <= x % y && x % y < y) && (y < 0 ==> y < x % y && x % y <= 0));
const unique C: Class; // <: unique Object;
const unique D: Class; // <: unique C;
const unique m: Method;
const unique fact: Method;

function ExampleInv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) {
  ( forall o1, o2: Ref :: related[o1, o2] ==> heap1[o1, dynType] == C && heap2[o2, dynType] == C )
  &&
  SystemInv(heap1, heap2, related)
}

procedure checkExample()
  modifies m1_receiver, m2_receiver, m1_param, m2_param, m1_method, m2_method, heap1, heap2, related;
{
  var this1, this2, res1, res2, fresh1, fresh2: Ref;
  var label : Method;

  assume label == m || label == fact;

  //assume RelM(m1_receiver, m2_receiver, m1_method, m2_method, m1_param, m2_param, related);
  assume ExampleInv(heap1, heap2, related);

  assume related[this1, this2];

  goto invoc;


  new:

  assume UnAllocObj(fresh1, heap1);
  call InitObj(fresh1, heap1);
  call Expose(fresh1, heap1);
  assume heap1[fresh1, dynType] == C;
  assume UnAllocObj(fresh2, heap2);
  call InitObj(fresh2, heap2);
  call Expose(fresh2, heap2);
  assume heap2[fresh2, dynType] == C || heap2[fresh2, dynType] == D;
  assume heap1[fresh1, dynType] == heap2[fresh2, dynType];
  related[fresh1,fresh2] := true;
  goto end;

  invoc:

  dispatch1:
  if (label == fact) {
  havoc fresh1;
  assume UnAllocObj(fresh1, heap1);
  assume !heap1[fresh1, isExposed];
  heap1[fresh1, alloc] := true;
//  call InitObj(fresh1, heap1);
  heap1[fresh1, dynType] := C;
  heap1[fresh1, isExposed] := true;
  goto dispatch2;
  }
  if (label == m) {
  goto dispatch2;
  }
  goto dispatch2;

  dispatch2:
  if (label == fact) {
  havoc fresh2;
  assume UnAllocObj(fresh2, heap2);
  assume !heap2[fresh2, isExposed];
  heap2[fresh2, alloc] := true;
//  call InitObj(fresh2, heap2);
  heap2[fresh2, dynType] := C;
  heap2[fresh2, isExposed] := true;
  goto Checking;
  }
  if (label == m) {
    assume heap2[this2, dynType] == C || heap2[this2, dynType] == D;
    if (heap2[this2, dynType] == C) {

      goto Checking;
    }
    if (heap2[this2, dynType] == D) {
      assert false;
      goto Checking;
    }
    assert false;
  }
  assert false;

  Checking:

  if (label == fact) {
    related[fresh1, fresh2] := true;
    goto end;
  }
  if (label == m) {
    goto end;
  }
  goto end;

  end:

  assert ExampleInv(heap1, heap2, related);
}
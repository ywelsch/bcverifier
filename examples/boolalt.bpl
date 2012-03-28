const unique Bool,OBool: Class;
const unique f: Field bool;
const unique g: Field Ref;

function Bijective(related:Bij) returns (bool) {
  ( forall r1,r2,r3,r4:Ref :: related[r1,r2] && related[r3,r4] ==> (r1 == r3 <==> r2 == r4) )
}

function ObjectCoupling(heap1: Heap, heap2: Heap, related:Bij) returns (bool) {
  ( forall r1,r2:Ref :: related[r1,r2] ==> AllocObj(r1,heap1) && AllocObj(r2,heap2) )
}

function Inv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) {
  Internal(OBool,g,heap1) && Internal(OBool,g,heap2) &&
  ( exists relint:Bij ::  Bijective(relint) && ObjectCoupling(heap1,heap2,relint) &&
   ( forall o1, o2: Ref :: related[o1,o2] && RefOfType(o1,heap1,OBool) && RefOfType(o2,heap2,OBool) ==> relint[heap1[o1,g],heap2[o2,g]] ) &&
   ( forall o1, o2: Ref :: relint[o1,o2] ==> heap1[o1,f] != heap2[o2,f] )
  )
}


procedure Check_OBool_Getg()
  modifies heap1, heap2, related; {
  var this1,this2:Ref, res1,res2:bool;
  assume Inv(heap1,heap2,related) && related[this1,this2] && RefOfType(this1,heap1,OBool) && RefOfType(this2,heap2,OBool);
  call res1 := inlineGet(heap1,heap1[this1,g]);
  call res2 := inlineGet(heap2,heap2[this2,g]);
  res2 := !res2;
  assert res1 == res2 && Inv(heap1,heap2,related);
}

procedure {:inline 1} inlineGet(heap:Heap, this:Ref) returns (res:bool)
  requires !heap[this,createdByCtxt]; {
  res := heap[this,f];
}

procedure Check_OBool_Setg()
  modifies heap1, heap2, related;
{
  var this1,this2:Ref, val1,val2:bool;
  assume Inv(heap1,heap2,related) && related[this1,this2] && RefOfType(this1,heap1,OBool) && RefOfType(this2,heap2,OBool) && (val1 == val2);
  call heap1 := inlineSet(heap1,heap1[this1,g],val1);
  call heap2 := inlineSet(heap2,heap2[this2,g],!val2);
  assert Inv(heap1,heap2,related);
}

procedure {:inline 1} inlineSet(heap:Heap, this:Ref, val:bool) returns (res:Heap)
  requires !heap[this,createdByCtxt];
{
  res := heap[this,f := val];
}

procedure Check_OBool_New()
  modifies heap1, heap2, related;
{
  var fresh1, fresh2, r1, r2: Ref;
  assume Inv(heap1,heap2,related);
  assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == OBool;
  heap1[fresh1,alloc] := true;
  assume !heap1[r1, alloc] && heap1[r1,dynType] == Bool;
  heap1[r1,alloc] := true;
  heap1[fresh1,g] := r1;
  call heap1 := inlineSet(heap1,heap1[fresh1,g],false);
  assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == OBool;
  heap2[fresh2,alloc] := true;
  assume !heap2[r2, alloc] && heap2[r2,dynType] == Bool;
  heap2[r2,alloc] := true;
  heap2[fresh2,g] := r2;
  call heap2 := inlineSet(heap2,heap2[fresh2,g],true);
  call Update(fresh1,fresh2);
  assert Inv(heap1,heap2,related);
}
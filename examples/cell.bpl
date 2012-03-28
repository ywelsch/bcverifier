const unique c,c1,c2: Field Ref; const unique f: Field bool;

function Inv(heap1:Heap, heap2:Heap, related:Bij) returns (bool) {
  ( forall o1,o2:Ref :: related[o1,o2] && heap2[o2,f] ==> RelNull(heap1[o1,c], heap2[o2,c1], related) ) &&
  ( forall o1,o2:Ref :: related[o1,o2] && !heap2[o2,f] ==> RelNull(heap1[o1, c], heap2[o2,c2], related) )
}

procedure Check_Cell_Get()
  modifies heap1, heap2, related;
{
  var this1, res1, this2, res2: Ref;
  assume Inv(heap1,heap2,related) && related[this1,this2];
  res1 := heap1[this1,c];
  if (heap2[this2,f]) {
    res2 := heap2[this2,c1];
  } else {
    res2 := heap2[this2,c2];
  }
  call Update(res1,res2);
  assert Inv(heap1,heap2,related);
}

procedure Check_Cell_Set()
  modifies heap1, heap2, related;
{
  var this1, o1, this2, o2: Ref;
  assume Inv(heap1,heap2,related) && related[this1,this2] && RelNull(o1,o2,related);
  heap1[this1,c] := o1;
  heap2[this2,f] := !heap2[this2,f];
  if (heap2[this2,f]) {
    heap2[this2,c1] := o2;
  } else {
    heap2[this2,c2] := o2;
  }
  assert Inv(heap1,heap2,related);
}

procedure Check_Cell_New()
  modifies heap1, heap2, related;
{
  var fresh1, fresh2: Ref;
  assume Inv(heap1,heap2,related);
  assume !heap1[fresh1,alloc];
  heap1[fresh1,alloc] := true;
  assume !heap2[fresh2,alloc];
  heap2[fresh2,alloc] := true;
  call Update(fresh1,fresh2);
  assert Inv(heap1,heap2,related);
}
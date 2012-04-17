const unique c,c1,c2: Field Ref; const unique n: Field int;

function Inv(heap1:Heap, heap2:Heap, related:Bij) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap2) ==> heap2[r,c1] == heap2[r,c2] ) &&
  ( forall r1,r2: Ref :: related[r1,r2] ==> RelNull(heap1[r1,c], heap2[r2,c1], related) ) }

procedure Check_Cell_Get()
  modifies heap1, heap2, related;
{
  var this1, res1, this2, res2: Ref;
  assume Inv(heap1,heap2,related) && related[this1,this2];
  res1 := heap1[this1,c];
  heap2[this2,n] := heap2[this2,n] + 1;
  if (heap2[this2,n] % 2 == 0) {
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
  heap2[this2,c1] := o2;
  heap2[this2,c2] := o2;
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
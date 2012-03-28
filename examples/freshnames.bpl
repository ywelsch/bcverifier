function Inv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) { true }

procedure Check_m()
  modifies heap1, heap2, related; {
  var this1,this2,fresh1:Ref, res1,res2:bool;
  assume Inv(heap1,heap2,related) && related[this1,this2];

  assume !heap1[fresh1, alloc];
  heap1[fresh1,alloc] := true;
  res1 := (this1 == fresh1);

  res2 := false;
  assert Inv(heap1,heap2,related) && res1 == res2;
}

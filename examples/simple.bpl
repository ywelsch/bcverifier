var globalVar:bool;

procedure simple()
  modifies globalVar;
{
  var flag:bool;
  assume flag;
  start:
  assume globalVar;
  if (flag) {
    goto check;
  } else {
    goto end;
  }

  check:
  if (flag) {
    assert globalVar;
    havoc globalVar;
    assume globalVar;
    flag := false;
    goto start;
  } else {
    assert false;
  }

  end:
}

//extractLoops


// Biijective preservation
const unique Bij1.v: Field Ref;
const unique Bij2.v: Field Ref;

function BijInv(heap1: Heap, heap2: Heap, related: Bij) returns (bool) {
  ( forall o1, o2: Ref :: related[o1, o2] ==> related[heap1[o1, Bij1.v], heap2[o2, Bij2.v]] )
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




/*
procedure checkLinkedListAddFront(this1:Ref, this2:Ref, ob1:Ref, ob2:Ref)
  modifies heap1, heap2, related, relint;
{
  var fresh1, fresh2: Ref;
  assume Inv(heap1, heap2, related, relint);
  assume RefOfType(this1,heap1,LinkedList);
  assume RefOfType(this2,heap2,LinkedList);
  assume RefOfType(ob1,heap1,Object);
  assume RefOfType(ob2,heap2,Object);
  assume related[this1,this2] && RelNull(ob1, ob2, related);

  assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == Node;
  heap1[fresh1,alloc] := true;
  heap1[fresh1,ob] := ob1;
  //assert ( forall r: Ref :: !relint[fresh1,r] );
  //assert ( forall r: Ref :: !relint[r,fresh1] );
//  heap1[this1, fst] := null;
//  heap1[fresh1, next] := heap1[this1, fst];
//  heap1[this1, fst] := fresh1;

  assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == Node;
  heap2[fresh2,alloc] := true;
  heap2[fresh2,ob] := ob2;
  assert heap1[fresh1,next] == null;
  assert heap2[fresh2,next] == null;
  //assert !heap2[fresh2,exposed];

//  assert ( forall r: Ref :: !relint[fresh2,r] );
  //assert ( forall r: Ref :: !relint[r,fresh2] );
//  heap2[heap2[this2, snt], next] := null;
//  heap2[fresh2, next] := heap2[heap2[this2, snt], next];
//  heap2[heap2[this2, snt], next] := fresh2;

  // update relint
  //assert (fresh1 == null && fresh2 == null) || ( fresh1 != null && fresh2 != null && (forall r:Ref :: relint[fresh1,r] ==> r == fresh2) && (forall r:Ref :: relint[r,fresh2] ==> r == fresh1) );
//  assert false;
//  assert WfBij(heap1, heap2, relint[fresh1,fresh2 := true]);

  //call UpdateRelint(fresh1,fresh2);
  //assert false;
  assert Inv(heap1, heap2, related, relint);
}*/
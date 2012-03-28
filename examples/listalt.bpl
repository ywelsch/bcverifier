const unique Node, LinkedList: Class; // and Object from Prelude
const unique ob,next,fst,snt: Field Ref;

// Typing of the heaps
var heap1_typing:bool where FType(heap1,Node,ob,Object) && FType(heap1,Node,next,Node) && FType(heap1,LinkedList,fst,Node);
var heap2_typing:bool where FType(heap2,Node,ob,Object) && FType(heap2,Node,next,Node) && FType(heap2,LinkedList,snt,Node);

var relint: Bij; // Internal relation between Node objects

procedure {:inline 1} UpdateRelint(r1:Ref, r2:Ref)
  modifies relint;
  requires (r1 == null && r2 == null) || ( r1 != null && r2 != null && (forall r:Ref :: relint[r1,r] ==> r == r2) && (forall r:Ref :: relint[r,r2] ==> r == r1) );
{
  if (r1 != null && r2 != null) { relint[r1,r2] := true; }
}

/*function Inv(heap1:Heap, heap2:Heap, related:Bij, relint:Bij) returns (bool) {
  NonNull(LinkedList,snt,heap2) &&
  Unique(Node,next,heap1) &&
  Unique(Node,next,heap2) &&
  Unique(LinkedList,fst,heap1) &&
  Unique(LinkedList,snt,heap2) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RefOfType(o1,heap1,Node) && RefOfType(o2,heap2,Node) ) &&
  ( forall o1,o2:Ref :: related[o1,o2] ==> (RefOfType(o1,heap1,LinkedList) || RefOfType(o1,heap1,Object)) && (RefOfType(o2,heap2,LinkedList) || RefOfType(o2,heap2,Object)) ) &&
  ( forall o1,o2:Ref :: related[o1,o2] ==> RelNull(heap1[o1,fst], heap2[heap2[o2,snt], next], relint) ) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,next], heap2[o2,next], relint) ) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,ob], heap2[o2,ob], related) )
}*/


// the following works currently only automatically with /loopUnroll:num
procedure check_LinkedList_Get()
  modifies heap1, heap2, related;
{
  var this1,this2,res1,res2,node1,node2: Ref;
  var i1,i2,count1,count2 : int;
  assume Inv(heap1, heap2, related, relint);
  assume related[this1,this2] && RefOfType(this1,heap1,LinkedList) && RefOfType(this2,heap2,LinkedList) && i1 == i2;

  res1 := null;
  node1 := heap1[this1, fst];

  count1 := 0;
  while(count1 < i1) {
    if (node1 != null) {
      node1 := heap1[node1, next];
      count1 := count1 + 1;
    } else {
      break;
    }
  }
  if (node1 != null) {
    res1 := heap1[node1, ob];
  }

  res2 := null;
  node2 := heap2[this2, snt];
  node2 := heap2[node2, next];

  count2 := 0;
  while(count2 < i2) {
    if (node2 != null) {
      node2 := heap2[node2, next];
      count2 := count2 + 1;
    } else {
      break;
    }
  }
  if (node2 != null) {
    res2 := heap2[node2, ob];
  }

  call Update(res1,res2);
  assert Inv(heap1, heap2, related, relint);
}

procedure check_LinkedList_Add()
  modifies heap1, heap2, related, relint;
{
  var this1,this2,ob1,ob2,node1,node2,fresh1,fresh2: Ref;
  assume Inv(heap1, heap2, related, relint);
  assume RefOfType(this1,heap1,LinkedList);
  assume RefOfType(this2,heap2,LinkedList);
  assume RefOfType(ob1,heap1,Object);
  assume RefOfType(ob2,heap2,Object);
  assume related[this1,this2] && RelNull(ob1, ob2, related);

  node1 := heap1[this1, fst];

  while(node1 != null) {
    if (heap1[node1, next] == null) {
      havoc fresh1;
      assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == Node;
      heap1[fresh1,alloc] := true;
      heap1[fresh1, ob] := ob1;
      heap1[node1, next] := fresh1;
      break;
    } else {
      node1 := heap1[node1, next];
    }
  }

  node2 := heap2[this2, snt];
  node2 := heap2[node2, next];

  while(node2 != null) {
    if (heap2[node2, next] == null) {
      havoc fresh2;
      assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == Node;
      heap2[fresh2,alloc] := true;
      heap2[fresh2, ob] := ob2;
      heap2[node2, next] := fresh2;
      call UpdateRelint(fresh1,fresh2);
      break;
    } else {
      node2 := heap2[node2, next];
    }
  }
  assert Inv(heap1, heap2, related, relint);
}

function Inv(heap1:Heap, heap2:Heap, related:Bij, relint:Bij) returns (bool) {
  NonNull(LinkedList,snt,heap2) &&
  Unique(Node,next,heap1) &&
  Unique(Node,next,heap2) &&
  Unique(LinkedList,fst,heap1) &&
  Unique(LinkedList,snt,heap2) &&
  ( forall o1,o2:Ref :: related[o1,o2] ==> (RefOfType(o1,heap1,LinkedList) || RefOfType(o1,heap1,Object)) && (RefOfType(o2,heap2,LinkedList) || RefOfType(o2,heap2,Object)) ) &&
//  ( exists relint:Bij ::
  ( forall o1,o2:Ref :: relint[o1,o2] ==> AllocObj(o1,heap1) && AllocObj(o2,heap2) && RefOfType(o1,heap1,Node) && RefOfType(o2,heap2,Node) ) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,ob], heap2[o2,ob], related) ) &&
/*  ( forall o1,o2:Ref :: relint[o1,o2] <==>
      ( exists r1,r2:Ref :: (( relint[r1,r2] && heap1[r1,next] == o1 && heap2[r2,next] == o2 ) ||
      ( related[r1,r2] && RefOfType(r1,heap1,LinkedList) && RefOfType(r2,heap2,LinkedList) && heap1[r1,fst] == o1 && heap2[heap2[r2,snt],next] == o2 ))) )
    &&*/
    ( forall o1,o2:Ref :: related[o1,o2] ==> RelNull(heap1[o1,fst],heap2[heap2[o2,snt],next],relint) )
    &&
    ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,next],heap2[o2,next],relint) )
}

procedure check_LinkedList_First()
  modifies heap1, heap2, related;
{
  var this1,this2,res1,res2,node1,node2: Ref;
  assume Inv(heap1, heap2, related, relint);
  assume related[this1,this2] && RefOfType(this1,heap1,LinkedList) && RefOfType(this2,heap2,LinkedList);

  res1 := null;
  node1 := heap1[this1, fst];
  if (node1 != null) {
    res1 := heap1[node1, ob];
  }
  res2 := null;
  node2 := heap2[this2, snt];
  assert node2 != null;
  node2 := heap2[node2, next];
  if (node2 != null) {
    res2 := heap2[node2, ob];
  }

  call Update(res1,res2);
  assert Inv(heap1, heap2, related, relint);

  // get second
  if (node1 != null) {
    node1 := heap1[node1, next];
  }
  if (node1 != null) {
    res1 := heap1[node1, ob];
  }
  if (node2 != null) {
    node2 := heap2[node2, next];
  }
  if (node2 != null) {
    res2 := heap2[node2, ob];
  }

  call Update(res1,res2);
  assert Inv(heap1, heap2, related, relint);
}
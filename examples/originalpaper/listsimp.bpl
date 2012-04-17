const unique Node,Subject,Observer: Class;
const unique ob,next,fst,snt: Field Ref;
const unique notify,update,add: Method;

// Typing of the heaps
var heap1_typing:bool where FType(heap1,Node,ob,Observer) && FType(heap1,Node,next,Node) && FType(heap1,Subject,fst,Node);
var heap2_typing:bool where FType(heap2,Node,ob,Observer) && FType(heap2,Node,next,Node) && FType(heap2,Subject,snt,Node);

var relint: Bij; // Internal relation between Node Observers

procedure {:inline 1} UpdateRelint(r1:Ref, r2:Ref)
  modifies relint;
{
  assert RelNull(r1,r2,relint) || ( !(exists r:Ref :: relint[r1,r]) && !(exists r:Ref :: relint[r,r2]) );
  if (r1 != null && r2 != null) { relint[r1,r2] := true; }
}

var stackabs1: Ref;
var stackabs2: Ref;

function Inv(heap1:Heap, heap2:Heap, related:Bij, relint:Bij, stackabs1:Ref, stackabs2:Ref) returns (bool) {
  NonNull(Subject,snt,heap2) &&
  Unique(Node,next,heap1) &&
  Unique(Node,next,heap2) &&
  Unique(Subject,fst,heap1) &&
  Unique(Subject,snt,heap2) &&
  ( exists relint2:Bij ::
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RefOfType(o1,heap1,Node) && RefOfType(o2,heap2,Node) ) &&
  ( forall o1,o2:Ref :: related[o1,o2] ==> (RefOfType(o1,heap1,Subject) || RefOfType(o1,heap1,Observer)) && (RefOfType(o2,heap2,Subject) || RefOfType(o2,heap2,Observer)) ) &&
  ( forall o1,o2:Ref :: related[o1,o2] ==> RelNull(heap1[o1,fst], heap2[heap2[o2,snt], next], relint) ) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,next], heap2[o2,next], relint) ) &&
  ( forall o1,o2:Ref :: relint[o1,o2] ==> RelNull(heap1[o1,ob], heap2[o2,ob], related) ) &&
  relint[stackabs1,stackabs2]
   && ( forall o1,o2:Ref :: relint[o1,o2] <==>
        ( exists r1,r2:Ref :: (( relint[r1,r2] && heap1[r1,next] == o1 && heap2[r2,next] == o2 ) ||
        ( related[r1,r2] && RefOfType(r1,heap1,Subject) && RefOfType(r2,heap2,Subject) && heap1[r1,fst] == o1 && heap2[heap2[r2,snt],next] == o2 ))) )
    )
}

procedure check_Subject_Notify()
  modifies heap1, heap2, related, relint, stackabs1, stackabs2;
{
  var this1,this2,node1,node2,ob1,ob2,fresh1,fresh2: Ref;
  var m1_receiver,m2_receiver: Ref;
  var m1_param,m2_param: Ref;
  var m1_method,m2_method: Method;
  assume Inv(heap1, heap2, related, relint, stackabs1, stackabs2);
  assume RelM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param,related);
  assume RefOfType(this1,heap1,Subject) && RefOfType(this2,heap2,Subject);
  assume RefOfType(ob1,heap1,Observer) && RefOfType(ob2,heap2,Observer);

  if (m1_method == notify) {
    this1 := m1_receiver;
    node1 := heap1[this1, fst];

    while(node1 != null) {
      ob1 := heap1[node1,ob];
      if (ob1 != null) {
        stackabs1 := node1;
        // call ob1.update();
        m1_receiver, m1_method, m1_param := ob1, update, null;
        goto L2;
        RtrnCB1:
        node1 := stackabs1;
        node1 := heap1[node1,next];
      } else {
        return; // do not consider this case
      }
    }
    m1_method, m1_param := rtrn, null;
  } else
  if (m1_method == rtrn) {
    goto RtrnCB1;
  } else
  if (m1_method == add) {
    this1, ob1 := m1_receiver, m1_param;
    //heap1[this1, fst] := null;
    assume !heap1[fresh1, alloc] && heap1[fresh1,dynType] == Node;
    heap1[fresh1,alloc] := true;
    heap1[fresh1,ob] := ob1;
    heap1[fresh1,next] := heap1[this1, fst];
    heap1[this1, fst] := fresh1;
  }


  L2:
  if (m2_method == notify) {
    this2 := m2_receiver;
    node2 := heap2[this2, snt];
    node2 := heap2[node2, next];

    while(node2 != null) {
      ob2 := heap2[node2,ob];
      if (ob2 != null) {
        stackabs2 := node2;
        // call ob2.update();
        m2_receiver, m2_method, m2_param := ob2, update, null;
        goto Checking;
        RtrnCB2:
        node2 := stackabs2;
        node2 := heap2[node2,next];
      } else {
        return;
      }
    }
    m2_method, m2_param := rtrn, null;
  } else
  if (m2_method == rtrn) {
    goto RtrnCB2;
  }  else
  if (m2_method == add) {
    this2, ob2 := m2_receiver, m2_param;
    assume !heap2[fresh2, alloc] && heap2[fresh2,dynType] == Node;
    heap2[fresh2,alloc] := true;
    heap2[fresh2,ob] := ob2;
    heap2[fresh2,next] := heap2[heap2[this2, snt], next];
    heap2[heap2[this2, snt], next] := fresh2;
    //assert !RelNull(fresh1,fresh2,relint);
    //relint[fresh1,fresh2] := true;
    assert RelNull(heap1[fresh1,next], heap2[fresh2,next], relint);
    call UpdateRelint(fresh1,fresh2); // Inconsistent state :(
    assert false;
  }

  Checking:
  call UpdateM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param);
  assert Inv(heap1, heap2, related, relint, stackabs1, stackabs2);
}
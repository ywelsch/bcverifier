const unique g: Field int;
const unique run,exec,inc: Method;

function Inv(heap1:Heap, heap2:Heap, related:Bij) returns (bool) {
  ( forall r: Ref :: AllocObj(r,heap1) ==> heap1[r,g] % 2 == 0 ) }

procedure Check_CB()
  modifies heap1, heap2, related;
{
  var this1,this2,c1,c2: Ref;
  var m1_receiver,m1_param,m2_receiver,m2_param: Ref;
  var m1_method,m2_method: Method;
  assume Inv(heap1,heap2,related);
  assume RelM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param,related);

  if (m1_method == exec) {
    this1, c1 := m1_receiver, m1_param;
    if (c1 != null) {
      // output label: call c1.run()
      m1_receiver, m1_method, m1_param := c1, run, null;
      goto L2;
      RtrnCB1:
    }
    if (heap1[this1,g] % 2 == 0) {
      // output label: rtrn null
      m1_method, m1_param := rtrn, null;
      goto L2;
    } else {
      // output label: rtrn this1
      m1_method, m1_param := rtrn, this1;
      goto L2;
    }
  } else
  if (m1_method == rtrn) {
    goto RtrnCB1;
  } else
  if (m1_method == inc) {
    this1 := m1_receiver;
    heap1[this1,g] := heap1[this1,g] + 2;
    // void rtrn, use null value
    m1_method, m1_param := rtrn, null;
    goto L2;
  }

  L2:
  if (m2_method == exec) {
    this2, c2 := m2_receiver, m2_param;
    if (c2 != null) {
      // output label: call c2.run()
      m2_receiver, m2_method, m2_param := c2, run, null;
      goto Checking;
      RtrnCB2:
    }
    // output label: rtrn null
    m2_method, m2_param := rtrn, null;
    goto Checking;
  } else
  if (m2_method == rtrn) {
    goto RtrnCB2;
  } else
  if (m2_method == inc) {
    this2 := m2_receiver;
    heap2[this2,g] := heap2[this2,g] + 2;
    // void rtrn, use null value
    m2_method, m2_param := rtrn, null;
    goto Checking;
  }

  Checking:
  call UpdateM(m1_receiver,m2_receiver,m1_method,m2_method,m1_param,m2_param);
  assert Inv(heap1,heap2,related);
}

/* The modulo operation */
axiom (forall x: int, y: int :: {x % y} {x / y} x % y == x - (x / y) * y);
axiom (forall x: int, y: int :: {x % y} (0 < y ==> 0 <= x % y && x % y < y) && (y < 0 ==> y < x % y && x % y <= 0));
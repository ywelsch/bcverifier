const unique c,c1,c2: Field Ref; const unique n: Field int;

function Inv(heap1:Heap, heap2:Heap, related:Bij) returns (bool) {
  ( forall r:Ref :: AllocObj(r,heap2) ==> heap2[r,c1] == heap2[r,c2] ) &&
  ( forall r1,r2: Ref :: related[r1,r2] ==> RelNull(heap1[r1,c], heap2[r2,c1], related) ) }


procedure Check_Cell_New()
  modifies heap1, heap2, related;
{
  var fresh1: Ref;
  var fresh2: Ref;
  var inline$Update$0$r1: Ref;
  var inline$Update$0$r2: Ref;
  var inline$Update$0$related: Bij;
  var heap1_0: Heap;
  var heap2_0: Heap;
  var heap1_1: Heap;
  var heap2_1: Heap;
  var related_0: Bij;
  var related_1: Bij;
  var heap1_2: Heap;
  var heap2_2: Heap;
  var heap1_3: Heap;
  var heap2_3: Heap;
  var related_2: Bij;
  var heap1_4: Heap;
  var heap2_4: Heap;
  var heap1_5: Heap;
  var heap2_5: Heap;
  var related_3: Bij;
  var related_4: Bij;
  var heap1_6: Heap;
  var heap2_6: Heap;
  var heap1_7: Heap;
  var heap2_7: Heap;


  anon0:
    assume Inv(heap1, heap2, related);
    assume !heap1[fresh1, alloc];
    heap1[fresh1, alloc] := true;
    assume !heap2[fresh2, alloc];
    heap2[fresh2, alloc] := true;
    heap1[fresh1, exposed] := true;
    heap2[fresh2, exposed] := true;
    goto inline$Update$0$Entry;

  inline$Update$0$Entry:
    inline$Update$0$r1 := fresh1;
    inline$Update$0$r2 := fresh2;
    assert (inline$Update$0$r1 == null && inline$Update$0$r2 == null) || (inline$Update$0$r1 != null && inline$Update$0$r2 != null && (forall r: Ref :: related[inline$Update$0$r1, r] ==> r == inline$Update$0$r2) && (forall r: Ref :: related[r, inline$Update$0$r2] ==> r == inline$Update$0$r1));
    inline$Update$0$related := related;
    goto inline$Update$0$anon0;

  inline$Update$0$anon0:
    goto inline$Update$0$anon2_Else;

  inline$Update$0$anon2_Else:
    assume !(inline$Update$0$r1 != null && inline$Update$0$r2 != null);
    assert false;
}
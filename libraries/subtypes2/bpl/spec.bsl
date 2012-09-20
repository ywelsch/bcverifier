>>>invariant
( forall o1,o2:Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RelNull(o1,o2, related) ==> (RefOfType(o1, heap1, $subtypes.B) <==> RefOfType(o2, heap2, $subtypes.B)) )
<<<
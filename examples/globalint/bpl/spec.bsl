>>>invariant
( forall r1, r2: Ref :: Obj(heap1, r1) && RefOfType(r1, heap1, $C) && Obj(heap2, r2) && RefOfType(r2, heap2, $C) && related[r1, r2] ==> heap1[r1,$C.g] + 1 == heap2[r2,$C.f] )
<<<
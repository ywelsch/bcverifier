>>>invariant
( forall r: Ref :: Obj(heap1, r) && RefOfType(r, heap1, $cb.A) ==> heap1[r,$cb.A.g] mod 2 == 0 )
<<<
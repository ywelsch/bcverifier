>>>invariant
( forall r:Ref :: RefOfType(r, heap2, $Cell) && Obj(heap2, r) ==> heap2[r,$Cell.c1] == heap2[r,$Cell.c2] )
( forall r1,r2: Ref :: Obj(heap1, r1) && Obj(heap2, r2) && RefOfType(r1, heap1, $Cell) && RefOfType(r2, heap2, $Cell) && related[r1,r2] ==> related[heap1[r1,$Cell.c], heap2[r2,$Cell.c1]] )
<<<
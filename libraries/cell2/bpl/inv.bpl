( forall r:Ref :: IsAllocated(heap2, r) ==> heap2[r,$Cell.c1] == heap2[r,$Cell.c2] )
( forall r1,r2: Ref :: related[r1,r2] ==> RelNull(heap1[r1,$Cell.c], heap2[r2,$Cell.c1], related) )
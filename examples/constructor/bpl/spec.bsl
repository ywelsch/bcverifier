>>>invariant
(forall o1: Ref :: Obj(heap1, o1) && RefOfType(o1, heap1, $A) ==> heap1[o1, $A.o] != null) //for lib 1
(forall o2: Ref :: Obj(heap2, o2) && RefOfType(o2, heap2, $A) ==> heap2[o2, $A.o] != null) //for lib 2
// (forall o1, o2: Ref :: Obj(heap1, o1) && isOfType(o1, heap1, $A) && Obj(heap2, o2) && isOfType(o2, heap2, $A) ==> RelNull(heap1[o1, $A.o], heap2[o2, $A.o], related))
<<<
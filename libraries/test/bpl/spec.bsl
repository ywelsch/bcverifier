>>>invariant
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RefOfType(o1, heap1, $Test) && RefOfType(o2, heap2, $Test) && related[o1,o2] ==> heap1[o1, $Test.i] == heap2[o2, $Test.i])
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RefOfType(o1, heap1, $Test) && RefOfType(o2, heap2, $Test) && related[o1,o2] ==> RelNull(heap1[o1, $Test.worker], heap2[o2, $Test.worker], related))
<<<
(forall o1, o2: Ref :: isOfType(o1, $Test) && isOfType(o2, $Test) && IsAllocated(heap1, o1) && IsAllocated(heap2, o2) && related[o1,o2] ==> heap1[o1, $Test.i] == heap2[o2, $Test.i])
(forall o1, o2: Ref :: isOfType(o1, $Test) && isOfType(o2, $Test) && IsAllocated(heap1, o1) && IsAllocated(heap2, o2) && related[o1,o2] ==> RelNull(heap1[o1, $Test.worker], heap2[o2, $Test.worker], related))
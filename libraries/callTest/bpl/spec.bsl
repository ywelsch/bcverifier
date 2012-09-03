>>>invariant
(forall o1,o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RefOfType(o1, heap1, $CallTest) && RefOfType(o2, heap2, $CallTest) && related[o1, o2] ==> RelNull(heap1[o1, $CallTest.w], heap2[o2, $CallTest.w], related))
<<<
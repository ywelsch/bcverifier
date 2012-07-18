>>>invariant
(forall o1: Ref :: Obj(heap1, o1) && RefOfType(o1, heap1, $CallTest) ==> typ(heap1[o1, $CallTest.w], heap1) == $MyWorker)
(forall o2: Ref :: Obj(heap2, o2) && RefOfType(o2, heap2, $CallTest) ==> typ(heap2[o2, $CallTest.w], heap2) == $MyWorker)
NonNull($CallTest, $CallTest.w, heap1)
NonNull($CallTest, $CallTest.w, heap2)
<<<
(forall o1,o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && isOfType(o1, heap1, $CallTest) && isOfType(o2, heap2, $CallTest) ==> heap1[heap1[o1, $CallTest.w], dynType] == $MyWorker && heap2[heap2[o2, $CallTest.w], dynType] == $MyWorker)
NonNull($CallTest, $CallTest.w, heap1) && NonNull($CallTest, $CallTest.w, heap2)
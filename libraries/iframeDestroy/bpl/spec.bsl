>>>invariant
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RefOfType(o1, heap1, $C) && RefOfType(o2, heap2, $C) && related[o1, o2] ==> RelNull(heap1[o1, $C.i], heap2[o2, $C.i], related))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> ( stack1[iframe][spmap1[iframe]][place] == lib1_C.m#int_notifyMe0 ==> (heap1[stack1[iframe][spmap1[iframe]][param0_r], $C.f] == 5) ))
(forall iframe: int :: 0<=iframe && iframe<=ip2 && iframe % 2 == 1 ==> ( stack2[iframe][spmap2[iframe]][place] == lib2_C.m#int_notifyMe0 ==> (heap2[stack2[iframe][spmap2[iframe]][param0_r], $C.f] == 5) ))
<<<
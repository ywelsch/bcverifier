>>>invariant
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))

(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe mod 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack2[iframe][spmap2[iframe]][place] == lib2_C.m_set$int$java.lang.Object_0))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe mod 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack1[iframe][spmap1[iframe]][reg1_i] == stack2[iframe][spmap2[iframe]][reg1_i]))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe mod 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (spmap1[iframe] == 0 && spmap2[iframe] == 0))
<<<
>>>preconditions
useHavoc[lib1_C.m_set$int$java.lang.Object_0] := false;
useHavoc[lib2_C.m_set$int$java.lang.Object_0] := false;
<<<

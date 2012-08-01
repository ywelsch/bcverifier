>>>invariant
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))
<<<
>>>local_invariant
stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0 <==> stack2[ip2-1][spmap2[ip2-1]][place] == lib2_C.m_set$int$java.lang.Object0
(stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[ip1-1][spmap1[ip1-1]][reg1_i] == stack2[ip2-1][spmap2[ip2-1]][reg1_i])
(stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (spmap1[ip1-1] == 0 && spmap2[ip2-1] == 0)
<<<
>>>preconditions
useHavoc[lib1_C.m_set$int$java.lang.Object0] := false;
useHavoc[lib2_C.m_set$int$java.lang.Object0] := false;
<<<
>>>invariant
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))
<<<
>>>local_invariant
stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0 <==> stack2[sp2-1][place] == lib2_C.m_set$int$java.lang.Object0
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[sp1-1][reg1_i] == stack2[sp2-1][reg1_i])
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (sp1 == 1 && sp2 == 1)
<<<
>>>preconditions
useHavoc[lib1_C.m_set$int$java.lang.Object0] := false;
useHavoc[lib2_C.m_set$int$java.lang.Object0] := false;
<<<
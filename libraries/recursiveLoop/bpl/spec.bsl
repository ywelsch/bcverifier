>>>invariant
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))

(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack2[iframe][spmap2[iframe]][place] == lib2_C.loop$int_set$int$java.lang.Object_0))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack1[iframe][spmap1[iframe]][reg1_i] == stack2[iframe][spmap2[iframe]][reg1_i]))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack1[iframe][spmap1[iframe]][reg1_i] <= 5))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (spmap1[iframe] == 0 && spmap2[iframe] == (spmap1[iframe] + 1 + stack1[iframe][spmap1[iframe]][reg1_i])))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_C.m_set$int$java.lang.Object_0) ==> (stack1[iframe][spmap1[iframe]][reg0_r] == stack1[iframe][0][reg0_r] && stack2[iframe][spmap2[iframe]][reg0_r] == stack2[iframe][0][reg0_r]))

<<<
>>>local_invariant
stack1[ip1][spmap1[ip1]][place] == afterLoop <==> ((stack2[ip2][spmap2[ip2]][place] == endLoop) || (stack2[ip2][spmap2[ip2]][place] == afterRec))
stack2[ip2][spmap2[ip2]][place] == endLoop ==> (spmap2[ip2] >= 1 && spmap2[ip2] <= 6)
stack2[ip2][spmap2[ip2]][place] == endLoop ==> (stack2[ip2][0][meth] == $m)
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))
<<<
>>>preconditions
useHavoc[lib1_C.m_set$int$java.lang.Object_0] := false;
useHavoc[lib2_C.loop$int_set$int$java.lang.Object_0] := false;
<<<
>>>places
afterLoop = old 12 (true) (stack2[ip2][spmap2[ip2]][place] == endLoop) (spmap2[ip2])
endLoop = new 18 (true)
afterRec = new 10 (true)
<<<
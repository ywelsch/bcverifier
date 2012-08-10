>>>invariant
(forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))

(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack2[ip2-1][spmap2[ip2-1]][place] == lib2_C.loop$int_set$int$java.lang.Object0)
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[ip1-1][spmap1[ip1-1]][reg1_i] == stack2[ip2-1][spmap2[ip2-1]][param1_i])
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[ip1-1][spmap1[ip1-1]][reg1_i] <= 5)
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (spmap1[ip1-1] == 0 && spmap2[ip2-1] == (spmap1[ip1-1] + 1 + stack1[ip1-1][spmap1[ip1-1]][reg1_i]))
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[ip1-1][spmap1[ip1-1]][param0_r] == stack1[ip1-1][0][param0_r] && stack2[ip2-1][spmap2[ip2-1]][param0_r] == stack2[ip2-1][0][param0_r])

<<<
>>>local_invariant
//stack1[ip1][spmap1[ip1]][place] == afterLoop ==> spmap1[ip1] == 0
stack1[ip1][spmap1[ip1]][place] == afterLoop <==> ((stack2[ip2][spmap2[ip2]][place] == endLoop) || (stack2[ip2][spmap2[ip2]][place] == afterRec))
stack2[ip2][spmap2[ip2]][place] == endLoop ==> (spmap2[ip2] >= 1 && spmap2[ip2] <= 6)
(stack1[ip1][spmap1[ip1]][place] == afterLoop && stack2[ip2][spmap2[ip2]][place] == afterRec) ==> (forall o1,o2: Ref :: ObjOfType(o1, $C, heap1) && ObjOfType(o2, $C, heap2) && related[o1, o2] ==> RelNull(heap1[o1, $C.list], heap2[o2, $C.list], related))
<<<
>>>preconditions
useHavoc[lib1_C.m_set$int$java.lang.Object0] := false;
useHavoc[lib2_C.loop$int_set$int$java.lang.Object0] := false;
stall1[afterLoop, endLoop] := true;
<<<
>>>places
afterLoop = old 8 (true)
endLoop = new 14 (true)
afterRec = new 6 (true)
<<<
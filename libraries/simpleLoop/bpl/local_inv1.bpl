stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0 <==> stack2[sp2-1][place] == lib2_C.m_set$int$java.lang.Object0
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[sp1-1][reg1_i] == stack2[sp2-1][reg1_i])
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (ObjOfType(stack1[sp1 - 1][param0_r], $C, heap1) && ObjOfType(stack2[sp2 - 1][param0_r], $C, heap2) && related[stack1[sp1 - 1][param0_r], stack2[sp2 - 1][param0_r]])
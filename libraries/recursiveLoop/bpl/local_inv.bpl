stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0 <==> stack2[sp2-1][place] == lib2_C.loop$int_set$int$java.lang.Object0
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[sp1-1][reg1_i] == stack2[sp2-1][param1_i])
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[sp1-1][reg1_i] <= 5)
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (sp1 == 1 && sp2 == (sp1 + 1 + stack1[sp1-1][reg1_i]))
(stack1[sp1-1][place] == lib1_C.m_set$int$java.lang.Object0) ==> (stack1[sp1-1][param0_r] == stack1[0][param0_r] && stack2[sp2-1][param0_r] == stack2[0][param0_r])
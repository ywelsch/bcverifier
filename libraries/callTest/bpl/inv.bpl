//(forall o1,o2: Ref :: related[o1, o2] ==> RelNull(heap1[o1, $CallTest.w], heap2[o2, $CallTest.w], related))
isOfType(stack1[0][param0_r], heap1, $CallTest) && isOfType(stack2[0][param0_r], heap2, $CallTest) ==> RelNull(heap1[stack1[0][param0_r], $CallTest.w], heap2[stack2[0][param0_r], $CallTest.w], related)

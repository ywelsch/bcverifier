>>>places
newBeforeReturn = new 9 (true)
oldInLoop = old 5 (true) (stack1[sp1][reg1_i] - stack1[sp1][reg3_i])
oldBeforeReturn = old 7 (true)
<<<

>>>preconditions
stall2[oldInLoop, newBeforeReturn] := true;
<<<

//reg1_i = n
//reg2_i = x
//reg3_i = i
>>>local_invariant
(stack1[sp1][place] == oldBeforeReturn || stack1[sp1][place] == oldInLoop) <==> (stack2[sp2][place] == newBeforeReturn)
stack1[sp1][place] == oldBeforeReturn ==> stack1[sp1][reg2_i] == stack2[sp2][reg2_i] //value of both x variables is equal before return
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg1_i] == stack2[sp2][reg1_i])     //n on both sides is the same
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg2_i] == stack1[sp1][reg3_i])     //the value of x is the value of i (before adding)
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg3_i] < stack1[sp1][reg1_i])      //i runs until n
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg3_i] >= 0)                       //i starts at 0
stack2[sp1][place] == newBeforeReturn ==> (if(stack2[sp2][reg1_i] >= 0) then (stack2[sp2][reg2_i] == stack2[sp2][reg1_i]) else (stack2[sp2][reg2_i] == 0))
<<<
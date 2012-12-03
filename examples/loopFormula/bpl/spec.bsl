>>>places
newBeforeReturn = new 9 (true) (stack1[ip1][spmap1[ip1]][place] == oldInLoop)
oldInLoop = old 5 (true)
oldBeforeReturn = old 7 (true)
<<<
//reg1_i = n
//reg2_i = x
//reg3_i = i
>>>local_invariant
(stack1[ip1][spmap1[ip1]][place] == oldBeforeReturn || stack1[ip1][spmap1[ip1]][place] == oldInLoop) <==> (stack2[ip2][spmap2[ip2]][place] == newBeforeReturn)
stack1[ip1][spmap1[ip1]][place] == oldBeforeReturn ==> stack1[ip1][spmap1[ip1]][reg2_i] == stack2[ip2][spmap2[ip2]][reg2_i] //value of both x variables is equal before return
stack1[ip1][spmap1[ip1]][place] == oldInLoop ==> (stack1[ip1][spmap1[ip1]][reg1_i] == stack2[ip2][spmap2[ip2]][reg1_i])     //n on both sides is the same
stack1[ip1][spmap1[ip1]][place] == oldInLoop ==> (stack1[ip1][spmap1[ip1]][reg2_i] == stack1[ip1][spmap1[ip1]][reg3_i])     //the value of x is the value of i (before adding)
stack1[ip1][spmap1[ip1]][place] == oldInLoop ==> (stack1[ip1][spmap1[ip1]][reg3_i] < stack1[ip1][spmap1[ip1]][reg1_i])      //i runs until n
stack1[ip1][spmap1[ip1]][place] == oldInLoop ==> (stack1[ip1][spmap1[ip1]][reg3_i] >= 0)                       //i starts at 0
stack2[ip2][spmap2[ip2]][place] == newBeforeReturn ==> (if(stack2[ip2][spmap2[ip2]][reg1_i] >= 0) then (stack2[ip2][spmap2[ip2]][reg2_i] == stack2[ip2][spmap2[ip2]][reg1_i]) else (stack2[ip2][spmap2[ip2]][reg2_i] == 0))
<<<
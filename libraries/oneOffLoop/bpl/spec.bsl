>>>local_invariant
stack1[ip1][spmap1[ip1]][place] == inLoop1 <==> stack2[ip2][spmap2[ip2]][place] == inLoop2
(stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][param1_i] == stack2[ip2][spmap2[ip2]][param1_i]) //the value of the parameter is the same
(stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][reg2_i] == stack2[ip2][spmap2[ip2]][reg2_i]) //the value of x is the same
(stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][reg3_i] == stack2[ip2][spmap2[ip2]][reg3_i]) //the value of i is the same
//(stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (spmap1[ip1] == 0 && spmap2[ip2] == 0)
<<<
>>>places
inLoop1 = old 5 (stack1[ip1][spmap1[ip1]][reg3_i] > 0)
inLoop2 = new 6 (true)
<<<
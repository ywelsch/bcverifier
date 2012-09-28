>>>local_invariant
(forall f: int :: stack1[ip1][spmap1[ip1]][place] == inLoop1 <==> stack2[ip2][spmap2[ip2]][place] == inLoop2)
(forall f: int :: (stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][reg1_i] == stack2[ip2][spmap2[ip2]][reg1_i])) //the value of the parameter is the same
(forall f: int :: (stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][reg2_i] == stack2[ip2][spmap2[ip2]][reg2_i])) //the value of x is the same
(forall f: int :: (stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (stack1[ip1][spmap1[ip1]][reg3_i] == stack2[ip2][spmap2[ip2]][reg3_i])) //the value of i is the same
//(forall f: int :: (stack1[ip1][spmap1[ip1]][place] == inLoop1) ==> (spmap1[ip1] == 0 && spmap2[ip2] == 0))
<<<
>>>places
inLoop1 = old 7 (true)
inLoop2 = new 8 (true)
<<<
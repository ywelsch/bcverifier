>>>local_invariant
stack1[sp1][place] == inLoop1 <==> stack2[sp2][place] == inLoop2
(stack1[sp1][place] == inLoop1) ==> (stack1[sp1][reg1_i] == stack2[sp2][reg1_i]) //the value of the parameter is the same
(stack1[sp1][place] == inLoop1) ==> (stack1[sp1][reg2_i] == stack2[sp2][reg2_i]) //the value of x is the same
(stack1[sp1][place] == inLoop1) ==> (stack1[sp1][reg3_i] == stack2[sp2][reg3_i]) //the value of i is the same
//(stack1[sp1][place] == inLoop1) ==> (sp1 == 0 && sp2 == 0)
<<<
>>>places
inLoop1 = old 7 (stack1[sp1][reg3_i] > 0)
inLoop2 = new 8 (stack2[sp2][reg3_i] > 0)
<<<
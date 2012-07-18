>>>local_invariant
stack1[sp1][place] == inLoop1 <==> stack2[sp2][place] == inLoop2
(stack1[sp1][place] == inLoop1) ==> (stack1[sp1][reg1_i] == stack2[sp2][reg1_i])
(stack1[sp1][place] == inLoop1) ==> (stack1[sp1][reg2_i] == stack2[sp2][reg2_i])
//(stack1[sp1][place] == inLoop1) ==> (sp1 == 0 && sp2 == 0)
<<<
>>>places
inLoop1 = old 7 true
inLoop2 = new 8 true
<<<
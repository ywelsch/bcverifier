>>>places
newBeforeReturn = new 7 (true)
oldInLoop = old 5 (true) (stack1[sp1][reg1_i] - stack1[sp1][reg3_i])
oldBeforeReturn = old 7 (true)
<<<

>>>preconditions
stall2[oldInLoop, newBeforeReturn] := true;
<<<

>>>invariant
( forall x, n: int :: (x == (((n-1)*n)/2)) ==> ((x + n) == ((n*(n+1))/2)) )
<<<

reg1_i = n
reg2_i = x
reg3_i = i
>>>local_invariant
( forall x, n: int :: (x == (((n-1)*n)/2)) ==> ((x + n) == ((n*(n+1))/2)) )
(stack1[sp1][place] == oldBeforeReturn || stack1[sp1][place] == oldInLoop) <==> (stack2[sp2][place] == newBeforeReturn)
stack1[sp1][place] == oldBeforeReturn ==> stack1[sp1][reg2_i] == stack2[sp2][reg2_i] //value of both x variables is equal before return
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg2_i] == (((stack1[sp1][reg3_i] - 1) * stack1[sp1][reg3_i]) / 2))
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg3_i] <= stack1[sp1][reg1_i])
stack1[sp1][place] == oldInLoop ==> (stack1[sp1][reg3_i] >= 0)
stack2[sp2][place] == newBeforeReturn ==> (if(stack2[sp2][reg1_i] >= 0) then (stack2[sp2][reg2_i] == ((stack1[sp1][reg1_i] * (stack1[sp1][reg1_i] + 1)) / 2)) else (stack2[sp2][reg2_i] == 0))
<<<
local place inLoop1 = line 7 of old C;
local place inLoop2 = line 8 of new C;

// both libraries are in the loop at the same time.	 
	local invariant at(inLoop1) <==> at(inLoop2);

//the values of the variables are the same
	local invariant at(inLoop1) && at(inLoop2) ==> 
		   stack(inLoop1, n) == stack(inLoop2, n)
		&& stack(inLoop1, x) == stack(inLoop2, x)
		&& stack(inLoop1, i) == stack(inLoop2, i);

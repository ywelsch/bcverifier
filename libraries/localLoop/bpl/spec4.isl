local place inLoop1 = line 7 of old C;
local place inLoop2 = line 8 of new C;

// both libraries are in the loop at the same time.	 
	local invariant at(inLoop1) <==> at(inLoop2);

//the values of the variables are the same
	local invariant at(inLoop1) && at(inLoop2) ==> 
		   eval(inLoop1, n) == eval(inLoop2, n)
		&& eval(inLoop1, x) == eval(inLoop2, x)
		&& eval(inLoop1, i) == eval(inLoop2, i);

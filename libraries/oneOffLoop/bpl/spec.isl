local place inLoop1 = line 5 of old C when i > 0;
local place inLoop2 = line 6 of new C;

//both libraries are in the loop at the same time.	 
local invariant at(inLoop1) <==> at(inLoop2);

// the values are ...
local invariant at(inLoop1) ==> 
	   stack(inLoop1, n) == stack(inLoop2, n)
	&& stack(inLoop1, x) == stack(inLoop2, x)
	&& stack(inLoop1, i) == stack(inLoop2, i);
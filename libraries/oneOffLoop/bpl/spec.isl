place inLoop1 = line 5 in old C where i > 0;
place inLoop2 = line 6 in new C where true;

//both libraries are in the loop at the same time.	 
local invariant at(inLoop1) <==> at(inLoop2);

// the values are ...
local invariant at(inLoop1) ==> 
	   stack(inLoop1, n) == stack(inLoop2, n)
	&& stack(inLoop1, x) == stack(inLoop2, x)
	&& stack(inLoop1, i) == stack(inLoop2, i);
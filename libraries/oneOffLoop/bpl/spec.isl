place inLoop1 = line 5 in old C where stack(inLoop1, sp1(), i > 0);
place inLoop2 = line 6 in new C where stack(inLoop2, sp2(), true);

//both libraries are in the loop at the same time.	 
local invariant at(inLoop1, sp1()) <==> at(inLoop2, sp2());

// the values are ...
local invariant at(inLoop1, sp1()) && at(inLoop2, sp2()) ==> 
	   stack(inLoop1, sp1(), n) == stack(inLoop2, sp2(), n)
	&& stack(inLoop1, sp1(), x) == stack(inLoop2, sp2(), x)
	&& stack(inLoop1, sp1(), i) == stack(inLoop2, sp2(), i);
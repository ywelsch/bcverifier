place inLoop1: old C, line 5 where stack(inLoop1, sp1(), i > 0);
place inLoop2: new C, line 6 where stack(inLoop2, sp2(), true);

//both libraries are in the loop at the same time.	 
local invariant at(inLoop1, sp1()) <==> at(inLoop2, sp2());

// the values are ...
local invariant at(inLoop1, sp1()) && at(inLoop2, sp2()) ==> 
	   stack(inLoop1, sp1(), n) == stack(inLoop2, sp2(), n) //the value of the parameter is the same
	&& stack(inLoop1, sp1(), x) == stack(inLoop2, sp2(), x) //the value of x is the same
	&& stack(inLoop1, sp1(), i) == stack(inLoop2, sp2(), i); //the value of i is the same
	

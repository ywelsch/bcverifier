programpoint inLoop1Point = line 7 in old C;
place inLoop1 = inLoop1Point where true;
place inLoop2 = line 8 in new C where true;

// both libraries are in the loop at the same time.	 
	local invariant at(inLoop1, sp1()) <==> at(inLoop2, sp2());

//the values of the variables are the same
	local invariant at(inLoop1, sp1()) && at(inLoop2, sp2()) ==> 
		   stack(inLoop1, sp1(), n) == stack(inLoop2, sp2(), n)
		&& stack(inLoop1, sp1(), x) == stack(inLoop2, sp2(), x)
		&& stack(inLoop1, sp1(), i) == stack(inLoop2, sp2(), i);

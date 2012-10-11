local place inLoop1 = line 5 of old C when i > 0;
local place inLoop2 = line 6 of new C;

local invariant at(inLoop1) && at(inLoop2) ==> 
	   stack(inLoop1, n) == stack(inLoop2, n)
	&& stack(inLoop1, x) == stack(inLoop2, x)
	&& stack(inLoop1, i) == stack(inLoop2, i);
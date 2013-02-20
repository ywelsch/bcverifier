local place p1 = line 7 of old A;
local place p2 = line 7 of new A;

local invariant at(p1) <==> at(p2);
local invariant at(p1) ==> 
	   eval(p1, z+5) == eval(p2, z)
	&& eval(p1, i) == eval(p2, i)
	&& eval(p1, x) == eval(p2, x) 
	&& eval(p2, z >= 5);
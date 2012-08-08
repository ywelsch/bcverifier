sync place newBeforeReturn: new C, line 9;
sync place oldBeforeReturn: old C, line 7;
place inLoop: old C, line 5;

// termination measure:
local invariant forall inLoop p1, inLoop p2 :: 
	happenedBefore(p1, p2) ==> p1.n - p1.i > p2.n - p1.i; 

local invariant at(inLoop) ==> latestInstance(inLoop).n - latestInstance(inLoop).i >= 0;


local invariant (at(newBeforeReturn) || at(inLoop)) <==> at(oldBeforeReturn);

//value of both x variables is equal before return
local invariant forall oldBeforeReturn p1, newBeforeReturn p2 :: p1.x == p2.x;


// n on both sides is the same
local invariant forall inLoop p1, newBeforeReturn p2 ::  ==> p1.n == p2.n ;

// the value of x is the value of i (before adding)
local invariant forall inLoop p :: p.x == p.i;
	
// i starts at 0 and runs until n
local invariant forall inLoop p ::  p.i >= 0 && p.i < p.n;
	

local invariant forall newBeforeReturn p ::
	if (p.n >= 0) then 
		p.x == p.n
	else
		p.x == 0;


place new beforeReturn:
	before C.sum.return
	when true;
	
place old inLoop:
	before C.sum.forLoops[0].body[0]
	when true
	termination measure n-i;
	
place old beforeReturn:
	before C.sum.return
	when true;

stall new beforeReturn while old inLoop;

invariant (old stack @ beforeReturn || old stack @ inLoop ) <==> (new stack @ beforeReturn);

//value of both x variables is equal before return
invariant (old stack @ beforeReturn) ==> (old beforeReturn.x == new beforeReturn.x);


invariant (old stack @ inLoop) ==> 
	// n on both sides is the same
	old inLoop.n == new beforeReturn.n 
	// the value of x is the value of i (before adding)
	&& old inLoop.x == old inLoop.i
	// i starts at 0 and runs until n
	&& old inLoop.i >= 0 && old inLoop.i < old inLoop.n;
	

invariant (new stack @ beforeReturn) ==>
	if (new beforeReturn.n >= 0) then 
		new beforeReturn.x == new beforeReturn.n
	else
		new beforeReturn.x == 0;


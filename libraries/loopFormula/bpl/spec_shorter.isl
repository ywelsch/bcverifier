place new beforeReturn:
	before C.sum.return;
	
place old inLoop:
	before C.sum.forLoops[0].body[0]
	termination measure n-i;
	
place old beforeReturn:
	before C.sum.return;

stall new beforeReturn while old inLoop;

invariant (old stack @ beforeReturn || old stack @ inLoop ) <==> (new stack @ beforeReturn);

//value of both x variables is equal before return
invariant (old stack @ beforeReturn) ==> (x == new beforeReturn.x);


invariant (old stack @ inLoop) ==> 
	// n on both sides is the same
	n == new beforeReturn.n 
	// the value of x is the value of i (before adding)
	&& x == i
	// i starts at 0 and runs until n
	&& i >= 0 && i < n;
	

invariant (new stack @ beforeReturn) ==>
	if n >= 0 then x == n else x == 0;


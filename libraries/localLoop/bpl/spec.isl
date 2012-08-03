place old inLoop:
	 before C.m.forLoops[0].body[0]
	 when true;
	 
place new inLoop:
	 before C.m.whileLoops[0].body[0]
	 when true;

// both libraries are in the loop at the same time.	 
// a)
invariant (old stack.place == inLoop) <==> (new stack.place == inLoop);
// b)
invariant (old stack @ inLoop) <==> (new stack @ inLoop);
// which interaction frame? always the latest?


//the value of the variables are the same
invariant old stack.place == inLoop ==> 
	   old inLoop.n = new inLoop.n 
	&& old inLoop.x = new inLoop.x
	&& old inLoop.i = new inLoop.i;

	
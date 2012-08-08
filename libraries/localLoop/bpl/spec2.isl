sync place inLoop1: old C, line 7;
sync place inLoop2: new C, line 8;

// both libraries are in the loop at the same time.	 
	local invariant at(inLoop1) <==> at(inLoop2);

//the value of the variables are the same
	// a) for all instances of the places which are in relation
	local invariant forall inLoop1 p1, inLoop2 p2 :: p1 ~ p2 ==> 
		   p1.n == p2.n 
		&& p1.x == p2.x
		&& p1.i == p2.i;
		
	// b) for the current instance
	local invariant at(inLoop1) ==> 
		   latestInstance(inLoop1).n == latestInstance(inLoop2).n
		&& latestInstance(inLoop1).x == latestInstance(inLoop2).x
		&& latestInstance(inLoop1).i == latestInstance(inLoop2).i
	
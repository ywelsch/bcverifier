place inLoop1: old C, line 7;
place inLoop2: new C, line 8;

// both libraries are in the loop at the same time.	 
	local invariant at(inLoop1) <==> at(inLoop2);

/**
stack1[ip1][spmap1[ip1]][place] == inLoop1 <==> stack2[ip2][spmap2[ip2]][place] == inLoop2
*/


//the value of the variables are the same
	local invariant at(inLoop1) && at(inLoop2) ==> 
		   old n == new n
		&& old x == new x
		&& old i == new i;
		
/*
(stack1[ip1][spmap1[ip1]][place] == inLoop1 && stack2[ip2][spmap2[ip2]][place] == inLoop2) ==>
	   stack1[ip1][spmap1[ip1]][n] == stack2[ip2][spmap2[ip2]][n]
	&& stack1[ip1][spmap1[ip1]][x] == stack2[ip2][spmap2[ip2]][x]
	&& stack1[ip1][spmap1[ip1]][i] == stack2[ip2][spmap2[ip2]][i]
*/
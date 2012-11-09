local place newBeforeReturn = line 7 of new C
		stall when at(oldInLoop);

local place oldInLoop = line 5 of old C;
local place oldBeforeReturn = line 7 of old C;

local invariant at(oldBeforeReturn) || at(oldInLoop) <==> at(newBeforeReturn);

// loop invariant
	local invariant at(oldInLoop) && at(newBeforeReturn) 
		==> stack(oldInLoop, n) == stack(newBeforeReturn, n);
	
	local invariant at(oldInLoop) 
		==> stack(oldInLoop, 
				   x == (i-1)*i / 2
				&& 0 <= i 
				&& i <= n);  
		

// equal at return:
	local invariant at(oldBeforeReturn) 
		==> stack(oldBeforeReturn, x == if n >= 0 then (n+1)*n / 2 else 0);
		
	local invariant at(newBeforeReturn)
		==> stack(newBeforeReturn, x == if n >= 0 then (n+1)*n / 2 else 0);
	
	local invariant at(oldBeforeReturn) && at(newBeforeReturn) 
		==> stack(oldBeforeReturn, n) == stack(newBeforeReturn, n);

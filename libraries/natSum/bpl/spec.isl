local place newBeforeReturn = line 7 of new C
    stall when at(oldInLoop) splitvc;

local place oldInLoop = line 5 of old C splitvc;
local place oldBeforeReturn = line 7 of old C splitvc;

local invariant at(oldBeforeReturn) || at(oldInLoop) <==> at(newBeforeReturn);

// loop invariant
  local invariant at(oldInLoop) && at(newBeforeReturn)
    ==> eval(oldInLoop, n) == eval(newBeforeReturn, n);

  local invariant at(oldInLoop)
    ==> eval(oldInLoop,
           x == (i-1)*i / 2
        && 0 <= i
        && i <= n);


// equal at return:
  local invariant at(oldBeforeReturn)
    ==> eval(oldBeforeReturn, x == if n >= 0 then (n+1)*n / 2 else 0);

  local invariant at(newBeforeReturn)
    ==> eval(newBeforeReturn, x == if n >= 0 then (n+1)*n / 2 else 0);

  local invariant at(oldBeforeReturn) && at(newBeforeReturn)
    ==> eval(oldBeforeReturn, n) == eval(newBeforeReturn, n);

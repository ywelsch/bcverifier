/*local place beforeCall = line 8 of old A;
local place afterCall = line 9 of old A;
local place beforeLoop = line 5 of new A stall when at(beforeCall) && n > 1;
local place inLoop = line 7 of new A;

local invariant at(afterCall) <==> at(inLoop);
local invariant at(beforeCall) <==> at(beforeLoop);
local invariant at(afterCall) && at(inLoop) ==> eval(inLoop, i) == eval(afterCall, n);
local invariant at(afterCall) && at(inLoop) ==> eval(inLoop, result) == eval(afterCall, result);
local invariant at(inLoop) ==> eval(inLoop, i > 1 && i <= n);
local invariant at(afterCall) && at(inLoop) ==> eval(inLoop, n) == eval(afterCall, n) ==> stackIndex(old) == 0;

local invariant at(beforeLoop) && at(beforeCall) ==> eval(beforeLoop, n) <= eval(beforeCall, n);
*/
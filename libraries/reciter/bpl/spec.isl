/*local place beforeCall = line 8 of old A;
local place afterCall = line 9 of old A;
local place beforeLoop = line 5 of new A stall when at(beforeCall) && n > 1;
local place inLoop = line 7 of new A;

local invariant at(afterCall) <==> at(inLoop);
local invariant at(beforeCall) <==> at(beforeLoop);
local invariant at(afterCall) && at(inLoop) ==> stack(inLoop, i) == stack(afterCall, n);
local invariant at(afterCall) && at(inLoop) ==> stack(inLoop, result) == stack(afterCall, result);
local invariant at(inLoop) ==> stack(inLoop, i > 1 && i <= n);
local invariant at(afterCall) && at(inLoop) ==> stack(inLoop, n) == stack(afterCall, n) ==> sp1() == 0;

local invariant at(beforeLoop) && at(beforeCall) ==> stack(beforeLoop, n) <= stack(beforeCall, n);
*/
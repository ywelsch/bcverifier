local place pl1 = line 4 of old C
                  stall when at(pl2inLoop)
                  with measure if at(pl2inLoop)
                               then 1 + eval(pl2inLoop, n) - eval(pl2inLoop, i)
                               else 0 splitvc;
local place pl2inLoop = line 5 of new C splitvc;
local place pl2afterLoop = line 8 of new C splitvc;

local invariant at(pl2inLoop) ==> eval(pl2inLoop, i) < eval(pl2inLoop, n);
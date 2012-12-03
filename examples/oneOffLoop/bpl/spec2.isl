local place inLoop1 = line 5 of old C;
local place inLoop2 = line 6 of new C
  stall when at(inLoop1) && eval(inLoop1, i) == 0;

local invariant at(inLoop1) && at(inLoop2) ==>
  eval(inLoop1, i) >= 0 && eval(inLoop1, n) > 1
  && eval(inLoop2, i) == (if eval(inLoop1, i) == 0 then 1 else eval(inLoop1, i))
  && eval(inLoop1, n) == eval(inLoop2, n)
  && eval(inLoop1, x) == eval(inLoop2, x);

local place inLoop1 = line 5 of old C;
local place inLoop2 = line 6 of new C
  stall when at(inLoop1) && stack(inLoop1, i) == 0;

local invariant at(inLoop1) && at(inLoop2) ==>
  stack(inLoop1, i) >= 0 && stack(inLoop1, n) > 1
  && stack(inLoop2, i) == (if stack(inLoop1, i) == 0 then 1 else stack(inLoop1, i))
  && stack(inLoop1, n) == stack(inLoop2, n)
  && stack(inLoop1, x) == stack(inLoop2, x);

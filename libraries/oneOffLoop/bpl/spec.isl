local place inLoop1 = line 5 of old C when i > 0;
local place inLoop2 = line 6 of new C;

local invariant at(inLoop1) && at(inLoop2) ==>
     eval(inLoop1, n) == eval(inLoop2, n)
  && eval(inLoop1, x) == eval(inLoop2, x)
  && eval(inLoop1, i) == eval(inLoop2, i);
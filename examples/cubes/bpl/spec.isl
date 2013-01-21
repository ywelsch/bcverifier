local place inLoop1 = line 6 of old C when i < n;
local place inLoop2 = line 6 of new C when i < n && 2 * t == i * (i + 1);

local invariant at(inLoop1) && at(inLoop2) ==>
     eval(inLoop1, n) == eval(inLoop2, n)
  && eval(inLoop1, i) == eval(inLoop2, i)
  && eval(inLoop1, s) == eval(inLoop2, t * t);
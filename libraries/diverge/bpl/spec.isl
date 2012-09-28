local place inLoop1 = line 4 of old C;
local place inLoop2a = line 6 of new C;
local place inLoop2b = line 8 of new C;
 
local invariant at(inLoop1) <==> at(inLoop2a) || at(inLoop2b);
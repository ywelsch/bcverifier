local place inLoop1a = line 6 of old C;
local place inLoop1b = line 8 of old C;
local place inLoop2 = line 5 of new C;
 
local invariant at(inLoop2) <==> at(inLoop1a) || at(inLoop1b);
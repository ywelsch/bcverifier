local place inLoop1a = line 6 of old C;
local place inLoop1b = line 8 of old C;
local place inLoop2 = line 4 of new C stall when at(inLoop1a) || at(inLoop1b);
 
local invariant at(inLoop2) <==> at(inLoop1a) || at(inLoop1b);
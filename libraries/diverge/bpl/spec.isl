place inLoop1 = line 4 in old C where true;
place inLoop2a = line 6 in new C where true;
place inLoop2b = line 8 in new C where true;
 
local invariant at(inLoop1) <==> at(inLoop2a) || at(inLoop2b);
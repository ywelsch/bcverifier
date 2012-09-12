place inLoop1: old C, line 4 where true;
place inLoop2a: new C, line 6 where true;
place inLoop2b: new C, line 8 where true;
 
local invariant at(inLoop1, sp1()) <==> at(inLoop2a, sp2()) || at(inLoop2b, sp2());
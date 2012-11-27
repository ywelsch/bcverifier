local place oldN = line 3 of old C splitvc;
local place oldM = line 6 of old C splitvc;
local place newN = call p in line 3 of new C;
local place newM = call p in line 6 of new C;
local place newPN = line 9 of new C
    when stackIndex(new) == 1 && at(newN, 0) && i == 0 splitvc;
local place newPM = line 9 of new C
    when stackIndex(new) == 1 && at(newM, 0) && i == 1 splitvc;

local invariant at(oldN) <==> at(newPN);
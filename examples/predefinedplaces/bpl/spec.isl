local place oldN = line 3 of old C;
local place oldM = line 6 of old C;
local place newN = call p in line 3 of new C nosync;
local place newM = call p in line 6 of new C nosync;
local place newPN = line 9 of new C
    when topFrame(new) == 1 && at(newN, topSlice(), 0) && i == 0;
local place newPM = line 9 of new C
    when topFrame(new) == 1 && at(newM, topSlice(), 0) && i == 1;

local invariant at(oldN) <==> at(newPN);
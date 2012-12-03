local place ps1 = line 5 of old A;
local place ps2 = line 5 of new A;

local place pe1 = line 7 of old A when x == 0;
local place pe2 = line 7 of new A when x == 0;

local invariant at(ps1) <==> at(ps2);
local invariant at(ps1) && at(ps2) ==> eval(ps1, c) ~ eval(ps2, c);
local invariant at(pe1) <==> at(pe2);

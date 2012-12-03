local place afterLoop = line 12 of old C
  stall when at(endLoop) with measure stackIndex(new);
local place endLoop = line 18 of new C;
local place afterRec = line 10 of new C;

place callSet1 = call set in line 10 of old C;
place callSet2 = call set in line 16 of new C;
local place beforeLoop2 = call loop in line 9 of new C nosync;

//lists are related:
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;
//lists are related in local places:
local invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;

invariant at(callSet1) <==> at(callSet2);
invariant at(callSet1) && at(callSet2) ==> eval(callSet1, i) == eval(callSet2, i);
invariant at(callSet1) && at(callSet2) ==> eval(callSet1, i <= 5);
invariant at(callSet1) && at(callSet2) ==> stackIndex(old) == 0;
invariant at(callSet1) && at(callSet2) ==> stackIndex(new) == 1 + eval(callSet1, i);


// 'this' is the same as in the lowest stack frame
invariant at(callSet2) ==> at(beforeLoop2, 0) && eval(callSet2, this) == eval(beforeLoop2, 0, this);


local invariant at(afterLoop) <==> (at(endLoop) || at(afterRec));
local invariant at(endLoop) ==> stackIndex(new) >= 1;
local invariant at(endLoop) ==> stackIndex(new) <= 6;
local invariant at(endLoop) ==> at(beforeLoop2, 0);



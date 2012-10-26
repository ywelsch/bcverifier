local place afterLoop = line 12 of old C 
	stall when at(endLoop) with measure sp2();
local place endLoop = line 18 of new C;
local place afterRec = line 10 of new C;

predefined place(splitvc) callSet1 = call set in line 10 of old C;
predefined place(splitvc) callSet2 = call set in line 16 of new C;
predefined place beforeLoop2 = call loop in line 9 of new C;

//lists are related:
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;
//lists are related in local places:
local invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;

invariant at(callSet1) <==> at(callSet2);
invariant at(callSet1) && at(callSet2) ==> stack(callSet1, i) == stack(callSet2, i);
invariant at(callSet1) && at(callSet2) ==> stack(callSet1, i <= 5);
invariant at(callSet1) && at(callSet2) ==> sp1() == 0; 
invariant at(callSet1) && at(callSet2) ==> sp2() == 1 + stack(callSet1, i);
	

// 'this' is the same as in the lowest stack frame
invariant at(callSet2) ==> at(beforeLoop2, 0) && stack(callSet2, this) == stack(beforeLoop2, 0, this);	   
	   

local invariant at(afterLoop) <==> (at(endLoop) || at(afterRec));
local invariant at(endLoop) ==> sp2() >= 1;
local invariant at(endLoop) ==> sp2() <= 6;
local invariant at(endLoop) ==> at(beforeLoop2, 0);



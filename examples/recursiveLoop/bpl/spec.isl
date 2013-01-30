local place afterLoop = line 12 of old C
  stall when at(endLoop) with measure topFrame(new);
local place endLoop = line 18 of new C;
local place afterRec = line 10 of new C;

place callSet1 = call set in line 10 of old C;
place callSet2 = call set in line 16 of new C;
place beforeLoop2 = call loop in line 9 of new C;

//lists are related:
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;
//lists are related in local places:
local invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;

invariant forall int s :: librarySlice(s) ==> (at(callSet1, s) <==> at(callSet2, s));
invariant forall int s :: librarySlice(s) && at(callSet1, s) && at(callSet2, s) ==> eval(callSet1, s, i) == eval(callSet2, s, i);
invariant forall int s :: librarySlice(s) && at(callSet1, s) && at(callSet2, s) ==> eval(callSet1, s, i <= 5);
invariant forall int s :: librarySlice(s) && at(callSet1, s) && at(callSet2, s) ==> topFrame(old, s) == 0;
invariant forall int s :: librarySlice(s) && at(callSet1, s) && at(callSet2, s) ==> topFrame(new, s) == 1 + eval(callSet1, s, i);


// 'this' is the same as in the lowest stack frame
      invariant forall int s :: librarySlice(s) && at(callSet2, s) ==> at(beforeLoop2, s, 0) && eval(callSet2, s, this) == eval(beforeLoop2, s, 0, this);
local invariant forall int s :: librarySlice(s) && at(callSet2, s) ==> at(beforeLoop2, s, 0) && eval(callSet2, s, this) == eval(beforeLoop2, s, 0, this);




local invariant at(afterLoop) <==> (at(endLoop) || at(afterRec));
local invariant at(endLoop) ==> topFrame(new) >= 1;
local invariant at(endLoop) ==> topFrame(new) <= 6;
local invariant at(endLoop) ==> at(beforeLoop2, topSlice(), 0);



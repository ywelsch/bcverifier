place oldpl1 = line 6 in old C where true;
place oldpl2 = line 7 in old C where true;
place newpl1 = line 9 in new C where true;
place newpl2 = line 10 in new C where true;

//both libraries are in the loop at the same time.	 
local invariant at(oldpl1) <==> at(newpl1);
local invariant at(oldpl2) <==> at(newpl2);

// the values are ...
local invariant at(oldpl1) ==> stack(oldpl1, n) == stack(newpl1, n);
local invariant at(oldpl2) ==> stack(oldpl2, n) == stack(newpl2, n);
local invariant (at(oldpl1) || at(oldpl2)) ==> sp1() == sp2() - 1;
local invariant at(oldpl1) && sp1() > 0 ==> at(oldpl1, sp1() - 1);
local invariant at(oldpl1) && sp1() > 0 ==> stack(oldpl1, sp1() - 1, n) == stack(oldpl1, n);
//local invariant at(p1) ==> stack(p1, result) * stack(p2, a) == stack(p2, result);
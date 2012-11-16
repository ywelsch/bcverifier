place oldpl1 = line 6 in old C where true;
place oldpl2 = line 7 in old C where true;
place newpl1 = line 9 in new C where true;
place newpl2 = line 10 in new C where true;

//both libraries are in the loop at the same time.	 
local invariant at(oldpl1) <==> at(newpl1);
local invariant at(oldpl2) <==> at(newpl2);

// the values are ...
local invariant at(oldpl1) ==> eval(oldpl1, n) == eval(newpl1, n);
local invariant at(oldpl2) ==> eval(oldpl2, n) == eval(newpl2, n);
local invariant (at(oldpl1) || at(oldpl2)) ==> stackIndex(old) == stackIndex(new) - 1;
local invariant at(oldpl1) && stackIndex(old) > 0 ==> at(oldpl1, stackIndex(old) - 1);
local invariant at(oldpl1) && stackIndex(old) > 0 ==> eval(oldpl1, stackIndex(old) - 1, n) == eval(oldpl1, n);
//local invariant at(p1) ==> eval(p1, result) * eval(p2, a) == eval(p2, result);
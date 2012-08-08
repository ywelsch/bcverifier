place afterLoop: old C, line 8;
place endLoop: new C, line 14;
place afterRec: new C, line 6;

// lists are related:
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;
// lists are related in local places:
local invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.list ~ o2.list;

local invariant at(call1) <==> at(call2);

/*
(stack1[libip(ip1)][spmap1[libip(ip1)]][place] == call1) <==> (stack2[libip(ip2)][spmap2[libip(ip2)]][place] == call2)

function libip(ip: int) returns (int);
axiom (forall ip: int :: ip % 2 == 0 ==> libip(ip) == ip-1);
axiom (forall ip: int :: ip % 2 == 1 ==> libip(ip) == ip);
*/


local invariant at(call1) && at(call2) ==>  old i == new i;

/*
stack1[libip(ip1)][spmap1[libip(ip1)]][place] == call1 
 && stack2[libip(ip2)][spmap2[libip(ip2)]][place] == call2) ==>
 	stack1[libip(ip1)][spmap1[libip(ip1)]][i] == stack2[libip(ip2)][spmap2[libip(ip2)]][i]
*/

local invariant at(call1) ==>  i <= 5;

/*
 stack1[libip(ip1)][spmap1[libip(ip1)]][place] == call1 ==>
 	stack1[libip(ip1)][spmap1[libip(ip1)]][i] <= 5 
 */

local invariant at(call1, SP1) && at(call2, SP2) ==> 
	   SP1 == 1 
	&& SP2 == stack(call1, SP1, i) + 1;

/*
stack1[libip(ip1)][spmap1[libip(ip1)]][place] == call1 
 && stack2[libip(ip2)][spmap2[libip(ip2)]][place] == call2 ==>
   	   spmap1[libip(ip1)] == 1
   	&& spmap2[libip(ip2)] == stack1[libip(ip1)][spmap1[libip(ip1)]][i] + 1
*/
	   
	   
local invariant at(call2) ==> new this == stack(call2, 0, this);		

/*
stack2[libip(ip2)][spmap2[libip(ip2)]][place] == call2 ==>
   	   stack2[libip(ip2)][spmap2[libip(ip2)]][this] == stack2[libip(ip2)][0][this]
*/

local invariant at(afterLoop1) <==> (at(endLoop2) || at(afterRec2));
local invariant at(endLoop2) ==> (SP2 >= 1 && SP2 <= 6);



 
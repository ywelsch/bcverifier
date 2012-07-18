invariant forall old Cell o1, new Cell o2 ::
					o1 ~ o2 ==>	if o2.f then o1.c ~ o2.c1 else o1.c ~ o2.c2;
invariant forall new Cell o :: o.c1 == o.c2;
invariant forall old Cell o1, new Cell o2 ::
					o1 ~ o2 ==>	o1.c ~ o2.c1;
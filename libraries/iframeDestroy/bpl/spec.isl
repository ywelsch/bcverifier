predefined place callNotifyMe1 = call notifyMe in line 8 of old C;
predefined place callNotifyMe2 = call notifyMe in line 8 of new C;

invariant forall old C o1, new C o2 :: o1 ~ o2 ==> (o1.i == null <==> o2.i == null);
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.i ~ o2.i;
invariant at(callNotifyMe1) ==> stack(callNotifyMe1, this.f) == 5;
invariant at(callNotifyMe2) ==> stack(callNotifyMe2, this.f) == 5;
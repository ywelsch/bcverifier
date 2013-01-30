place callNotifyMe1 = call notifyMe in line 8 of old C;
place callNotifyMe2 = call notifyMe in line 8 of new C;

invariant forall old C o1, new C o2 :: o1 ~ o2 ==> (o1.i == null <==> o2.i == null);
invariant forall old C o1, new C o2 :: o1 ~ o2 ==> o1.i ~ o2.i;
invariant forall int s :: librarySlice(s) && at(callNotifyMe1, s) ==> eval(callNotifyMe1, s, this.f) == 5;
invariant forall int s :: librarySlice(s) && at(callNotifyMe2, s) ==> eval(callNotifyMe2, s, this.f) == 5;
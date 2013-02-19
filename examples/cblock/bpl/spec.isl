invariant forall old A o1, new A o2 :: o1 ~ o2 ==> o1.x == o2.x && o1.l == o2.l;
place p1 = call run in line 10 of old A nosplit;
place p2 = call run in line 11 of new A nosplit;
invariant forall int i :: librarySlice(old, i) && at(p1, i)
  ==> eval(p1, i, this.l);
invariant forall int i :: librarySlice(new, i) && at(p2, i)
  ==> eval(p2, i, this.l) && eval(p2, i, n == this.x);
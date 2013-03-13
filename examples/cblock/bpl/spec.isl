invariant forall old A o1, new A o2 :: o1 ~ o2 ==> o1.x == o2.x && o1.l == o2.l;
place p = call run in line 11 of new A nosplit;
invariant forall int i :: librarySlice(new, i) && at(p, i)
  ==> eval(p, i, this.l && n == this.x);
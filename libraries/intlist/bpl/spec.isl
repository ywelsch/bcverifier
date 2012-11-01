invariant forall new IntList o :: o.snt != null;
invariant forall old IntList o1, new IntList o2 :: o1 ~ o2 ==> (o1.fst == null <==> o2.snt.next == null);
invariant forall old IntList o1, new IntList o2 :: o1 ~ o2 && o1.fst != null && o2.snt.next != null ==> o1.fst.value == o2.snt.next.value;

//invariant forall old IntList o1, old IntList o2 :: o1.fst == null || o1.fst != o2.fst;
//invariant forall new IntList o1, new IntList o2 :: o1.snt.next == null || o1.snt.next != o2.snt.next;
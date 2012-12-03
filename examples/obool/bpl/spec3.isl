// nonnull:
invariant forall old OBool o :: o.g != null;
invariant forall new OBool o :: o.g != null;
// internal:
invariant forall old OBool o :: createdByLibrary(o.g) && !exposed(o.g);
invariant forall new OBool o :: createdByLibrary(o.g) && !exposed(o.g);
// unique (bijection between g fields):
invariant exists binrelation relbij :: bijective(relbij) &&
  (forall old OBool o1, new OBool o2 ::
          o1 ~ o2 ==> related(relbij, o1.g, o2.g));

invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;
invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;
// nonnull:
invariant forall old OBool o :: o.g != null;
invariant forall new OBool o :: o.g != null;
// internal:
invariant forall old OBool o :: !createdByCtxt(o.g) && !exposed(o.g);
invariant forall new OBool o :: !createdByCtxt(o.g) && !exposed(o.g);
// unique (aliasing on field g allowed but consistently):
invariant forall old OBool o1, old OBool o2, new OBool o3, new OBool o4 ::
          o1 ~ o3 && o2 ~ o4 ==> (o1.g == o2.g <==> o3.g == o4.g);

invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;
invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;
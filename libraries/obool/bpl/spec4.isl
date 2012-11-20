// nonnull:
invariant forall old OBool o :: o.g != null;
invariant forall new OBool o :: o.g != null;
// internal:
invariant forall old OBool o :: createdByLibrary(o.g) && !exposed(o.g);
invariant forall new OBool o :: createdByLibrary(o.g) && !exposed(o.g);

invariant forall old Bool o1, new Bool o2 :: o1 ~ o2 ==> o1.f == o2.f;
invariant forall old OBool o1, new OBool o2 :: o1 ~ o2 ==> o1.g.f != o2.g.f;

var bijection bij = empty();
var old Bool x1 = null;
var new Bool x2 = null;
place p1 = line 6 of old OBool assign x1 = this.g;
place p2 = line 6 of new OBool assign x2 = this.g;
assign bij = if x1 != null && x2 != null then add(bij, x1, x2) else bij;
assign x1 = null;
assign x2 = null;

invariant x1 == null && x2 == null;

// unique (bijection between g fields):
invariant forall old OBool o1, new OBool o2 ::
          o1 ~ o2 ==> related(bij, o1.g, o2.g);
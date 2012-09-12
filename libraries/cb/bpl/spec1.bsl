>>>invariant
( forall r: Ref :: Obj(heap1, r) && RefOfType(r, heap1, $cb.A) ==> heap1[r,$cb.A.g] % 2 == 0 )
<<<
>>>preconditions
useHavoc[lib1_cb.A.exec#boolean$cb.C_run_0] := false;
useHavoc[lib2_cb.A.exec#boolean$cb.C_run_0] := false;
<<<
>>>invariant
( forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1,o2] && RefOfType(o1, heap1, $obool.OBool) && RefOfType(o2, heap2, $obool.OBool) ==> (heap1[heap1[o1,$obool.OBool.g],$obool.Bool.f] != heap2[heap2[o2,$obool.OBool.g],$obool.Bool.f]) )
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1, o2] && RefOfType(o1, heap1, $obool.Bool) && RefOfType(o2, heap2, $obool.Bool) ==> heap1[o1, $obool.Bool.f] == heap2[o2, $obool.Bool.f])
//(forall r: Ref :: Obj(heap1, r) && RefOfType(r, heap1, $obool.OBool) ==> !heap1[heap1[r, $obool.OBool.g], exposed] && !heap1[heap1[r, $obool.OBool.g], createdByCtxt])
//(forall r: Ref :: Obj(heap2, r) && RefOfType(r, heap2, $obool.OBool) ==> !heap2[heap2[r, $obool.OBool.g], exposed] && !heap2[heap2[r, $obool.OBool.g], createdByCtxt])
Internal($obool.OBool,$obool.OBool.g,heap1) && Internal($obool.OBool,$obool.OBool.g,heap2)
NonNull($obool.OBool,$obool.OBool.g,heap1) && NonNull($obool.OBool,$obool.OBool.g,heap2)
Unique($obool.OBool,$obool.OBool.g,heap1) && Unique($obool.OBool,$obool.OBool.g,heap2)
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframemod2==0 && iframe>0 ==> stack1[iframe-1][spmap1[iframe-1]][place]!=lib1_obool.OBool.getg#boolean_get#boolean_0)
<<<
>>>preconditions
useHavoc[lib2_obool.OBool.setg$boolean_set$boolean_0] := false;
useHavoc[lib2_obool.OBool.getg#boolean_get#boolean_0] := false;
useHavoc[lib1_obool.OBool..init_set$boolean_0] := false;
useHavoc[lib1_obool.OBool..init_..init#obool.Bool_0] := false;
useHavoc[lib1_obool.OBool.setg$boolean_set$boolean_0] := false;
useHavoc[lib1_obool.OBool.getg#boolean_get#boolean_0] := false;
useHavoc[lib2_obool.OBool..init_..init#obool.Bool_0] := false;
useHavoc[lib2_obool.OBool..init_set$boolean_0] := false;
<<<
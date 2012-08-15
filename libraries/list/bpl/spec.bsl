>>>invariant
//( forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1,o2] && RefOfType(o1, heap1, $obool.OBool) && RefOfType(o2, heap2, $obool.OBool) ==> (heap1[heap1[o1,$obool.OBool.g],$obool.Bool.f] != heap2[heap2[o2,$obool.OBool.g],$obool.Bool.f]) )
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1, o2] && RefOfType(o1, heap1, $list.List) && RefOfType(o2, heap2, $list.List) ==> heap1[o1, $list.List.fst] == heap2[heap2[o2, $list.List.snt], $list.Node.next])
//(forall r: Ref :: Obj(heap1, r) && RefOfType(r, heap1, $obool.OBool) ==> !heap1[heap1[r, $obool.OBool.g], exposed] && !heap1[heap1[r, $obool.OBool.g], createdByCtxt])
//(forall r: Ref :: Obj(heap2, r) && RefOfType(r, heap2, $obool.OBool) ==> !heap2[heap2[r, $obool.OBool.g], exposed] && !heap2[heap2[r, $obool.OBool.g], createdByCtxt])
//NonNull($obool.OBool,$obool.OBool.g,heap1) && NonNull($obool.OBool,$obool.OBool.g,heap2)
(exists relint: Bij :: Bijective(relint) && ObjectCoupling(heap1, heap2, relint) && (forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1,o2] && RefOfType(o1, heap1, $list.List) && RefOfType(o2, heap2, $list.List) ==> relint[heap1[o1,$list.List.fst],heap2[heap2[o2, $list.List.snt], $list.Node.next]]) && (forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && relint[o1, o2] ==> related[heap1[o1, $list.Node.ob],heap2[o2, $list.Node.ob]] ))

NonNull($list.List,$list.List.snt,heap2)
<<<
>>>local_invariant
//(sp1 == 0)
<<<
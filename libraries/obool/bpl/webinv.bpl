( forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1,o2] && RefOfType(o1, heap1, $obool.OBool) && RefOfType(o2, heap2, $obool.OBool) ==> (heap1[heap1[o1,$obool.OBool.g],$obool.Bool.f] != heap2[heap2[o2,$obool.OBool.g],$obool.Bool.f]) ) &&
(forall o1, o2: Ref :: Obj(heap1, o1) && Obj(heap2, o2) && related[o1, o2] && RefOfType(o1, heap1, $obool.Bool) && RefOfType(o2, heap2, $obool.Bool) ==> heap1[o1, $obool.Bool.f] == heap2[o2, $obool.Bool.f]) &&
Internal($obool.OBool,$obool.OBool.g,heap1) && Internal($obool.OBool,$obool.OBool.g,heap2) &&
NonNull($obool.OBool,$obool.OBool.g,heap1) && NonNull($obool.OBool,$obool.OBool.g,heap2) &&
Unique($obool.OBool,$obool.OBool.g,heap1) && Unique($obool.OBool,$obool.OBool.g,heap2)
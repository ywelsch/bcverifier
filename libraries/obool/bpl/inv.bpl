//( forall o1, o2: Ref :: related[o1,o2] && RefOfType(o1, heap1, $obool.OBool) && RefOfType(o2, heap2, $obool.OBool) ==> (heap1[heap1[o1,$obool.OBool.g],$obool.Bool.f] != heap2[heap2[o2,$obool.OBool.g],$obool.Bool.f]) )
//(forall o1, o2: Ref :: related[o1, o2] && heap1[o1, createdByCtxt] && heap2[o2, createdByCtxt] && RefOfType(o1, heap1, $obool.Bool) && RefOfType(o2, heap2, $obool.Bool) ==> heap1[o1, $obool.Bool.f] == heap2[o2, $obool.Bool.f])
(sp1 > 0 && stack1[sp1-1][place] == lib1_obool.OBool..init_set$boolean0) ==> (!heap1[stack1[sp1][param0_r], createdByCtxt])
(sp1 > 0 && stack1[sp1-1][place] == lib1_obool.OBool.getg#boolean_get#boolean0) ==> (!heap1[stack1[sp1][param0_r], createdByCtxt])
//NonNull($obool.OBool,$obool.OBool.g,heap1) && NonNull($obool.OBool,$obool.OBool.g,heap2) && Unique($obool.OBool,$obool.OBool.g,heap1) && Unique($obool.OBool,$obool.OBool.g,heap2) && Internal($obool.OBool,$obool.OBool.g,heap1) && Internal($obool.OBool,$obool.OBool.g,heap2)

isOfType(stack1[0][param0_r], heap1, $obool.OBool) && isOfType(stack2[0][param0_r], heap2, $obool.OBool) ==> (heap1[heap1[stack1[0][param0_r],$obool.OBool.g],$obool.Bool.f] != heap2[heap2[stack2[0][param0_r],$obool.OBool.g],$obool.Bool.f])
heap1[stack1[0][param0_r], createdByCtxt] && heap2[stack2[0][param0_r], createdByCtxt] && isOfType(stack1[0][param0_r], heap1, $obool.Bool) && isOfType(stack2[0][param0_r], heap2, $obool.Bool) ==> heap1[stack1[0][param0_r], $obool.Bool.f] == heap2[stack2[0][param0_r], $obool.Bool.f]
isOfType(stack1[0][param0_r], heap1, $obool.OBool) ==> !heap1[heap1[stack1[0][param0_r],$obool.OBool.g], createdByCtxt]
isOfType(stack2[0][param0_r], heap2, $obool.OBool) ==> !heap2[heap2[stack2[0][param0_r],$obool.OBool.g], createdByCtxt]
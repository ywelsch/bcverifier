>>>invariant
( forall o1,o2:Ref :: Obj(heap1, o1) && Obj(heap2, o2) && RefOfType(o1, heap1, $cell.Cell) && RefOfType(o2, heap2, $cell.Cell) && related[o1,o2] ==> if int2bool(heap2[o2,$cell.Cell.f]) then related[heap1[o1,$cell.Cell.c], heap2[o2,$cell.Cell.c1]] else related[heap1[o1, $cell.Cell.c], heap2[o2,$cell.Cell.c2]] )
<<<
>>>invariant
( forall o1,o2:Ref :: o1 != null && heap1[o1,alloc] && o2 != null && heap2[o2,alloc] && RefOfType(o1, heap1, $cell.Cell) && RefOfType(o2, heap2, $cell.Cell) && RelNull(o1,o2, related) ==> if int2bool(heap2[o2,$cell.Cell.f]) then RelNull(heap1[o1,$cell.Cell.c], heap2[o2,$cell.Cell.c1], related) else RelNull(heap1[o1, $cell.Cell.c], heap2[o2,$cell.Cell.c2], related) )
<<<
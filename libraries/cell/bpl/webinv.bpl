( forall o1,o2:Ref :: ObjOfType(o1,$cell.Cell,heap1) &&
                      ObjOfType(o2,$cell.Cell,heap2) && 
                      related[o1,o2]
    ==> if   int2bool(heap2[o2,$cell.Cell.f]) 
	    then RelNull(heap1[o1,$cell.Cell.c], heap2[o2,$cell.Cell.c1], related) 
	    else RelNull(heap1[o1, $cell.Cell.c], heap2[o2,$cell.Cell.c2], related)
)
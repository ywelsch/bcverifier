place p = call run in line 9 of old A nosplit;
invariant forall int iframe :: librarySlice(iframe) && iframe == topSlice()-1 && at(p, iframe)
	==> old A.x == 1;


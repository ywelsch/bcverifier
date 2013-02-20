place p1 = call run in line 7 of old A nosplit;
place p2 = call run in line 9 of old A nosplit;

invariant forall int iframe :: librarySlice(iframe) ==> at(p1, iframe) || at(p2, iframe);	
invariant forall int iframe :: librarySlice(iframe) && at(p2, iframe)
	&& !(exists int iframe2 :: iframe2 > iframe && librarySlice(iframe2) && at(p1, iframe2) 
		     && eval(p2, iframe, this) == eval(p2, iframe2, this)
        )
	==> eval(p2, iframe, this.x == 1);
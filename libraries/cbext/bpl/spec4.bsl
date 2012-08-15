>>>preconditions
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run1] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run1] := false;
<<<
>>>invariant
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_cb.A.exec#cb.A$cb.C_run0 ==> stack2[iframe][spmap2[iframe]][place] == lib2_cb.A.exec#cb.A$cb.C_run0))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> ( related[stack1[iframe][spmap1[iframe]][param1_r], stack2[iframe][spmap2[iframe]][param1_r]] ))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_cb.A.exec#cb.A$cb.C_run1 ==> stack2[iframe][spmap2[iframe]][place] == lib2_cb.A.exec#cb.A$cb.C_run1))
<<<

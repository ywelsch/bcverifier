>>>preconditions
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run_0] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run_0] := false;
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run_1] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run_1] := false;
<<<
>>>invariant
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_cb.A.exec#cb.A$cb.C_run_0 ==> stack2[iframe][spmap2[iframe]][place] == lib2_cb.A.exec#cb.A$cb.C_run_0))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> ( RelNull(stack1[iframe][spmap1[iframe]][param1_r], stack2[iframe][spmap2[iframe]][param1_r], related) ))
(forall iframe: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 ==> (stack1[iframe][spmap1[iframe]][place] == lib1_cb.A.exec#cb.A$cb.C_run_1 ==> stack2[iframe][spmap2[iframe]][place] == lib2_cb.A.exec#cb.A$cb.C_run_1))
<<<

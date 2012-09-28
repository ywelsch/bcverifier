>>>preconditions
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run_0] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run_0] := false;
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run_1] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run_1] := false;
<<<
>>>invariant
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_cb.A.exec#cb.A$cb.C_run_0 ==> stack2[ip2-1][spmap2[ip2-1]][place] == lib2_cb.A.exec#cb.A$cb.C_run_0)
(ip1 % 2 == 0 && RelNull(stack1[ip1-1][spmap1[ip1-1]][reg1_r], stack2[ip2-1][spmap2[ip2-1]][reg1_r], related) )
(ip1 % 2 == 0 && stack1[ip1-1][spmap1[ip1-1]][place] == lib1_cb.A.exec#cb.A$cb.C_run_1 ==> stack2[ip2-1][spmap2[ip2-1]][place] == lib2_cb.A.exec#cb.A$cb.C_run_1)
<<<
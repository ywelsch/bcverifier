>>>preconditions
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run1] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run1] := false;
<<<
>>>local_invariant
stack1[ip1-1][spmap1[ip1-1]][place] == lib1_cb.A.exec#cb.A$cb.C_run0 <==> stack2[ip2-1][spmap2[ip2-1]][place] == lib2_cb.A.exec#cb.A$cb.C_run0
related[stack1[ip1-1][spmap1[ip1-1]][param1_r], stack2[ip2-1][spmap2[ip2-1]][param1_r]]
stack1[ip1-1][spmap1[ip1-1]][place] == lib1_cb.A.exec#cb.A$cb.C_run1 <==> stack2[ip2-1][spmap2[ip2-1]][place] == lib2_cb.A.exec#cb.A$cb.C_run1
<<<
>>>preconditions
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run0] := false;
useHavoc[lib1_cb.A.exec#cb.A$cb.C_run1] := false;
useHavoc[lib2_cb.A.exec#cb.A$cb.C_run1] := false;
<<<
>>>local_invariant
(forall iframe, sframe1, sframe2: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 && 0<=sframe1 && sframe1 <= spmap1[iframe] && 0<=sframe2 && sframe2<=spmap2[iframe] ==> (stack1[iframe][sframe1][place] == lib1_cb.A.exec#cb.A$cb.C_run0 ==> stack2[iframe][sframe2][place] == lib2_cb.A.exec#cb.A$cb.C_run0))
// (forall iframe, sframe1, sframe2: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 && 0<=sframe1 && sframe1 <= spmap1[iframe] && 0<=sframe2 && sframe2<=spmap2[iframe] ==> ((stack1[iframe][sframe1][param1_r] == null && stack2[iframe][sframe2][param1_r] == null) || (stack1[iframe][sframe1][param1_r] != null && stack2[iframe][sframe2][param1_r] != null && related[stack1[iframe][sframe1][param1_r], stack2[iframe][sframe2][param1_r]]) ))
(forall iframe, sframe1, sframe2: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 && 0<=sframe1 && sframe1 <= spmap1[iframe] && 0<=sframe2 && sframe2<=spmap2[iframe] ==> ( RelNull(stack1[iframe][sframe1][param1_r], stack2[iframe][sframe2][param1_r], related) ))
(forall iframe, sframe1, sframe2: int :: 0<=iframe && iframe<=ip1 && iframe % 2 == 1 && 0<=sframe1 && sframe1 <= spmap1[iframe] && 0<=sframe2 && sframe2<=spmap2[iframe] ==> (stack1[iframe][sframe1][place] == lib1_cb.A.exec#cb.A$cb.C_run1 ==> stack2[iframe][sframe2][place] == lib2_cb.A.exec#cb.A$cb.C_run1))
<<<
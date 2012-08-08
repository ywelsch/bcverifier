>>>local_invariant
( forall f: int :: 0<=f && f <= ip1 && f % 2 == 1 && stack1[f][spmap1[f]][place] == lib1_cb.A.exec#boolean$cb.C_begin ==> (heap1[stack1[f][spmap1[f]][param0_r],$cb.A.g] % 2 == 0) )
( forall f: int :: 0<=f && f <= ip1 && f % 2 == 1 && stack1[f][spmap1[f]][place] == lib1_cb.A.exec#boolean$cb.C_run0 ==> (heap1[stack1[f][spmap1[f]][param0_r],$cb.A.g] % 2 == 0) )
<<<
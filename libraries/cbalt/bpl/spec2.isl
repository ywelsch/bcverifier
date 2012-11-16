invariant forall old A a :: a.g % 2 == 0;

place p1 = call run in line 7 of old A splitvc;
place p2 = call run in line 7 of new A splitvc;

invariant at(p1) ==> eval(p1, i) % 2 == 0;
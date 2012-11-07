invariant forall old A a :: a.g % 2 == 0;

predefined place(splitvc) p1 = call run in line 7 of old A;
predefined place(splitvc) p2 = call run in line 7 of new A;

invariant at(p1) ==> stack(p1, i) % 2 == 0;
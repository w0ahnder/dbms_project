SELECT * FROM Sailors S, Reserves R,Boats B WHERE S.A = R.G AND S.A<500 AND S.B>=200 AND 5<7;
SELECT * FROM Sailors S, Boats B WHERE S.A = 1 AND B.E=1;
SELECT * FROM Sailors, Reserves, Boats  WHERE Sailors.A = Reserves.G AND Sailors.A<500 AND Sailors.B>=200 AND 5<7;
SELECT * FROM Sailors S, Reserves R,Boats B WHERE S.A = R.G AND S.A<500 AND S.B>=200 AND 5<7;
SELECT * FROM Sailors S,Reserves R,Boats B;
SELECT * FROM Sailors WHERE 3<7 AND 5>7 AND Sailors.C = Sailors.B;
SELECT * FROM Sailors WHERE Sailors.A < 3;
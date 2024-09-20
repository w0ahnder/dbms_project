SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
SELECT DISTINCT R.G FROM Reserves R;
SELECT * FROM Sailors ORDER BY Sailors.B;
SELECT S.A, B.E, R.G FROM Sailors S,Reserves R,Boats B ORDER BY R.G, S.A;
SELECT DISTINCT S.A, B.E, R.G FROM Sailors S,Reserves R,Boats B WHERE B.E > 2 ORDER BY R.G, S.A
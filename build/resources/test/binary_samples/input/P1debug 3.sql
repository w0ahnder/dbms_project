SELECT * FROM Reserves, Boats;
SELECT Sailors.C, Reserves.H FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Reserves.G <= 3;
SELECT S1.A, S2.A, S3.A FROM Sailors S1, Sailors S2, Sailors S3 WHERE S1.A < S2.A AND S2.A < S3.A AND S3.A < 5;
SELECT S.C, R.H FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D AND S.B != 100;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F, Boats.E;
SELECT DISTINCT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D ORDER BY Sailors.C, Boats.F;
SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;
SELECT * FROM TestTable2 T2A, TestTable2 T2B;
SELECT Sailors.C, Reserves.H FROM Sailors,Boats, Reserves  WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D AND Reserves.G <= 3;
SELECT * FROM Reserves;
SELECT Sailors.A, Sailors.B FROM Sailors;


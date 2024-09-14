SELECT * FROM Sailors WHERE 3<7 AND 5<7 AND Sailors.C>50;
SELECT * FROM Sailors, Reserves,Boats WHERE Sailors.A = Reserves.G AND SAILORS.A<500 AND Sailors.B>300 AND Sailors.B>200;
SELECT * FROM Sailors WHERE Sailors.A < 3;
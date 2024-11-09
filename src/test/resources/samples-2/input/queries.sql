SELECT * FROM Sailors WHERE Sailors.A < 4000;
SELECT * FROM Sailors WHERE Sailors.A <= 4000;
SELECT * FROM Sailors WHERE Sailors.A <= 1000 AND Sailors.B<3000;
SELECT Sailors.A, Boats.E FROM Sailors, Boats WHERE Sailors.A < 4000 AND Boats.E <300;
SELECT Sailors.A, Boats.E FROM Sailors, Boats WHERE Sailors.A <= 4000 AND Boats.E <=35;
SELECT Sailors.A, Sailors.B, Boats.E FROM Sailors, Boats WHERE Sailors.A <= 4000 AND Boats.E <=35 AND Sailors.A!=Sailors.B;
SELECT Sailors.A, Boats.E FROM Sailors, Boats WHERE Sailors.A <= 4000 AND Boats.E <=35 ORDER BY Sailors.A, Boats.E;
SELECT DISTINCT Sailors.A, Boats.E FROM Sailors, Boats WHERE Sailors.A <= 4000 AND Boats.E <=35 ORDER BY Sailors.A, Boats.E;
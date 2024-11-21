SELECT * FROM Sailors, Boats WHERE Sailors.A = 100 AND Sailors.A > Boats.E;
SELECT Sailors.A, Sailors.B, Boats.E, Boats.F FROM Sailors, Boats WHERE Sailors.A < 100 AND Sailors.A = Sailors.B AND Sailors.B = Boats.E AND Boats.E > 50 AND Boats.E < 80 AND Boats.F = 42 AND Sailors.C <> Boats.G AND Sailors.D <> 55;
SELECT * FROM Sailors, Boats WHERE Sailors.A = 100 AND Sailors.A = Boats.E;
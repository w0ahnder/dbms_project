SELECT * FROM Sailors;
SELECT * FROM Sailors WHERE Sailors.A < 4;
SELECT * FROM Sailors, Reserves;
SELECT * FROM Sailors, Reserves WHERE Sailors.A < 0; 
SELECT * FROM Reserves, Sailors WHERE Sailors.A < 0; 
SELECT * FROM Reserves, Sailors WHERE Sailors.A = Reserves.H; 
SELECT * FROM Sailors WHERE Sailors.A>=9969;
SELECT * FROM Sailors WHERE Sailors.A>=9969 AND Sailors.B=Sailors.A;
SELECT * FROM Sailors WHERE Sailors.A>=Sailors.B AND Sailors.B=Sailors.A;
SELECT * FROM Sailors WHERE Sailors.A<>0 AND Sailors.B<= 5000;
SELECT * FROM Sailors, Boats WHERE Sailors.A>=9998 AND Boats.E>=9999;
SELECT * FROM Sailors, Boats WHERE Sailors.A>=9969 AND Boats.E>=9973 AND Boats.E>= 9963;
SELECT * FROM Sailors, Boats WHERE Sailors.A>=9969 AND Sailors.A > Sailors.B AND Boats.E>=9973 AND Boats.E>= 9963;
SELECT * FROM Sailors, Boats WHERE Sailors.A>=9969 AND Sailors.A >Boats.E AND Boats.E>=9973 AND Boats.E>= 9963 AND Boats.E> Boats.D;

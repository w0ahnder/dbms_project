SELECT * FROM Table1, Table2 WHERE Table1.A = Table2.D AND Table1.B = Table2.E;
SELECT Table1.A, Table2.D FROM Table1, Table2 WHERE Table1.C = Table2.F AND Table1.C = Table2.E;
SELECT DISTINCT Table1.A FROM Table1, Table2 WHERE Table1.A = Table2.D AND Table1.B = Table2.E AND Table1.C = Table2.F;
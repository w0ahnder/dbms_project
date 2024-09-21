
## Instructions

Our top level harness class is named Compiler.java

**PATH:**  src/main/java/compiler/Compiler.java

**Logic for JoinOperator:**

After Parsing the command, we change the expression into a list of expressions. 
If it is an AndExpression, for instance, we make it into a list of all its left and right expressions by recursing.

From the tables to be joined, we identify the right most table, the last table. 

We classify the list of expressions into:

**RightExpressions:** expressions that only contain the last table
**LeftExpressions:** expressions that do not contain the last table
**InExpressions:** expressions that involve the last table and any table from the remaining

We then create a tree, with its right being a leaf (Scan/Select Operator on the last table with rightExpressions being the condition)

And a JoinOperator on the remaining tables with the leftExpressions being the expressions

**Base case:**

  When we only have two tables remaining, then we will have two leaves, Scan / Select Operator on each side

**Evaluation:**
```
when getNextTuple is called:
  if calls getNextTuple on the leftOperator:
    if that is null:
      it returns null
    else:
      it calls getNextTuple on the rightOperator:
        if that is null:
          it returns null
        else:
          it evaluates the inExpressions
            if that is false:
              it repeats back from line 6
      if it is null:
        it repeats back from line 2
```
### Additional Information

When adding unit tests for each file we ran into an issue where tests do not run 
in sequence when using @BeforeAll declaration as in P1UnitTests.java but they were
successful when running them individually or using @BeforeEach because of shared 
resources, DBCatalog. 
[![Java CI with Gradle](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml/badge.svg)](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml)

# Cornell Database Systems Practicum - CS 4321/5321
### Phase 4

The top level class is the Compiler class.
Path: src/main/java/compiler/Compiler.java


The optimal select plan is chosen by first iterating through each of the select expressions for a table. If the table has an index and
also has an index on the column mentioned in the expression, we keep track of the column, all of the indexed expressions it's
involved in, and update its range. Once we have all the unindexed expressions (which are those that involve unindexed columns, or operations other than >=, =, <=, <, >), 
we compute the indexed scan costs and compare it to the cost of a full scan. We find the column that gives us the minimum cost scan and create an index scan operator, and
and a select operator if we have expressions not covered by the column we chose
1. high key is set to be the inclusive upper bound and lowkey is set to be the inclusive lower bound. In cases when they are not specified, we use max and min integer value, which we go through the select expressions to update by comparison
2. In src/main/tree/BulkLoad.java , sortRelation method is only called when it is clustered. It sorts the table in memory and overrides the original table
3. When first setting up the logical operator, we separate the expressions in the Select condition using an implementation of ExpressionVisitor to see which queries can be processed by index and which cannot. 
Then we pass two separate conditions, one indexed and one unindexed. If indexed is null, we just run a normal scan. If unindexed is null, we just use the IndexScan operator. Or else, we create a SelectOperator that takes  IndexScan operator as a child operator. 
So getNextTuple in this case will call IndexScan operator nextTuple(). For returned tuple, if it passes unindexed conditions, it returns the tuple. Else, it calls nextTuple() on IndexScan until it finds a tuple that passes both conditions. Same as how we did for joinOperator.

4. For the choice of join Order, we followed the instruction in implementing the DP algorithm in selecting the optimal order based on the cost. The cost was calculated based on the left cost and size of left child. After finding the optimal order we combined the tables left to right and reOrdered the column order later in getnextTuple() method. 
5. We used BNLJ as a choice of join implementation because we could not get to implement SMJ for certain conditions before the deadline. Our thought idea, however, was to only use SMJ in cases of equality join and BNLJ in all other cases. 


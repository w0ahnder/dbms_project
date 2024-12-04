[![Java CI with Gradle](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml/badge.svg)](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml)

# Cornell Database Systems Practicum - CS 4321/5321
### Phase 4

The top level class is the Compiler class.
Path: src/main/java/compiler/Compiler.java

### Select Plan
The optimal select plan is chosen by first iterating through each of the select expressions for a table. If the table has an index and
also has an index on the column mentioned in the expression, we keep track of the column, all of the indexed expressions it's
involved in, and update its range. Once we have all the unindexed expressions (which are those that involve unindexed columns, or operations other than >=, =, <=, <, >), 
we compute the indexed scan costs and compare it to the cost of a full scan. We find the column that gives us the minimum cost scan and create an index scan operator, and
and a select operator if we have expressions not covered by the column we chose. The functionality for choosing the optimal selectplan is in common/SelectPlan

### Join
When first setting up the logical operator, we separate the expressions in the Select condition using an implementation of ExpressionVisitor to see which queries can be processed by index and which cannot. 
Then we pass two separate conditions, one indexed and one unindexed. If indexed is null, we just run a normal scan. If unindexed is null, we just use the IndexScan operator. Or else, we create a SelectOperator that takes  IndexScan operator as a child operator. 
So getNextTuple in this case will call IndexScan operator nextTuple(). For returned tuple, if it passes unindexed conditions, it returns the tuple. Else, it calls nextTuple() on IndexScan until it finds a tuple that passes both conditions. Same as how we did for joinOperator.

For the choice of join Order, we followed the instruction in implementing the DP algorithm in selecting the optimal order based on the cost. The cost was calculated based on the left cost and size of left child. After finding the optimal order we combined the tables left to right and reOrdered the column order later in getnextTuple() method. 
We used BNLJ as a choice of join implementation because we could not get to implement SMJ for certain conditions before the deadline. Our thought idea, however, was to only use SMJ in cases of equality join and BNLJ in all other cases. 

###SelectPush
For select push we implemented it by creating an element class that stored sets of columns that were bound together by equality signs and then a union find that is a collection of the elements. After modifying the elements based on selection condions to set their upper, lower or equality constraint, we converted it back to expressions to be used for processing scans and selects.

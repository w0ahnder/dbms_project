[![Java CI with Gradle](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml/badge.svg)](https://github.com/CornellDB/db_practicum/actions/workflows/gradle.yml)

# Cornell Database Systems Practicum - CS 4321/5321
### Phase 3

The top level class is the Compiler class.
Path: src/main/java/compiler/Compiler.java


1. high key is set to be the inclusive upper bound and lowkey is set to be the inclusive lower bound. In cases when they are not specified, we use max and min integer value, which we go through the select expressions to update by comparison
2. In src/main/tree/BulkLoad.java , sortRelation method is only called when it is clustered. It sorts the table in memory and overrides the original table
3. When first setting up the logical operator, we separate the expressions in the Select condition using an implementation of ExpressionVisitor to see which queries can be processed by index and which cannot. 
Then we pass two separate conditions, one indexed and one unindexed. If indexed is null, we just run a normal scan. If unindexed is null, we just use the IndexScan operator. Or else, we create a SelectOperator that takes  IndexScan operator as a child operator. 
So getNextTuple in this case will call IndexScan operator nextTuple(). For returned tuple, if it passes unindexed conditions, it returns the tuple. Else, it calls nextTuple() on IndexScan until it finds a tuple that passes both conditions. Same as how we did for joinOperator.

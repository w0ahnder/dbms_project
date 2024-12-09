package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import operator.LogicalOperators.LogicalOperator;
import operator.PhysicalOperators.IndexScanOperator;
import operator.PhysicalOperators.Operator;
import operator.PhysicalOperators.ScanOperator;
import operator.PhysicalOperators.SelectOperator;

public class SelectPlan {
  ArrayList<Expression> expressions;
  String tableName;
  HashMap<String, Integer[]> colMinMax = new HashMap<>();
  int numTuples;
  int numCols;
  TableStats stats;
  HashMap<String, Double> colIndexCost = new HashMap<>();
  HashMap<String, Expression> indexedExpressions = new HashMap<>();
  ArrayList<Expression> unindexedExpressions = new ArrayList<>();
  LogicalOperator scan;
  String tablePath;
  ArrayList<Column> schema;

  // if we have indexed expressions, then we can check if indexes are better than full scan
  // have to check if we have an index for the column tho
  // if expressions can't be indexed, then full scan?
  public SelectPlan(
      String tableName, ArrayList<Column> schema, String tablePath, LogicalOperator scan) {
    // this.expressions = expressions;
    this.tableName = tableName;
    stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(tableName));
    numTuples =
        DBCatalog.getInstance()
            .getTableStats(DBCatalog.getInstance().getTableName(tableName))
            .getNumTuples();
    numCols =
        DBCatalog.getInstance()
            .getTableStats(DBCatalog.getInstance().getTableName(tableName))
            .numCols();
    this.scan = scan;
    this.tablePath = tablePath;
    this.schema = schema;
  }

  /**
   * Takes in an expression that can be a conjunction of multiple expressions. It loops through each
   * of them and checks if one side is a column and the other is a value. If so, it gets the column
   * name and checks if the table has an index, and if there is an index on that column. If there's
   * an index, using ScanVisitor, we check if the op is one we can use an index for, and if so, get
   * the low and high values, then add the expression and column to our HashMap of indexed
   * expressions. If there's no index or the op is not one we can use an index for, the expression
   * is added to a list of unindexed expressions.
   *
   * @param expression
   */
  public void plan(Expression expression) {
    // each expression in here will have the table in it?
    ArrayList<Expression> expressions = getAndExpressions(expression);
    for (Expression exp : expressions) {
      Expression left = ((BinaryExpression) exp).getLeftExpression();
      Expression right = ((BinaryExpression) exp).getRightExpression();
      boolean isLeftCol = left instanceof Column;
      boolean isRightCol = right instanceof Column;
      boolean checkIndex = false;
      String colToCheck = null;
      if (isLeftCol && !isRightCol) { // table.col = long
        String[] left_col = ((Column) left).toString().split("\\.");
        colToCheck = left_col[1];
      } else if (!isLeftCol && isRightCol) { // long =  table.col
        String[] right_col = ((Column) right).toString().split("\\.");
        colToCheck = right_col[1];
      } else {
        unindexedExpressions.add(exp);
        continue;
      }
      boolean tableHasIndex = DBCatalog.getInstance().getAvailableIndexColumn(DBCatalog.getInstance().getTableName(tableName)) != null;
      boolean colHasIndex =
          DBCatalog.getInstance().getAvailableIndex(tableName, colToCheck) != null;
      if (tableHasIndex && colHasIndex) {
        // means we have an available index, now check if expression can be indexed on it
        ScanVisitor visitor = new ScanVisitor(exp, tableName + "." + colToCheck);
        if (visitor.evaluate_expr()) {
          Integer low = visitor.getLowKey();
          Integer high = visitor.getHighKey();
          colRange(colToCheck, low, high);

          if (indexedExpressions.containsKey(colToCheck)) {
            ArrayList<Expression> colExp = new ArrayList<>();
            colExp.add(exp);
            Expression currExpression = indexedExpressions.get(colToCheck);
            colExp.add(currExpression);
            indexedExpressions.put(colToCheck, createAndExpression(colExp));
          } else {
            indexedExpressions.put(colToCheck, exp);
          }
        } else {
          unindexedExpressions.add(exp);
        }
      } else {
        unindexedExpressions.add(exp);
      }
    }
    // optimalPlan();
  }

  public HashMap<String, Integer[]> getColMinMax() {
    return this.colMinMax;
  }

  /**
   * Computes the optimal Select plan for the given expressions.
   *
   * @return an Operator that provides the lowest cost plan
   * @throws FileNotFoundException
   */
  public Operator optimalPlan() throws FileNotFoundException {
    int pages = ((numTuples * numCols * 4) / 4096) + 1; //
    double indexScan = pages;
    System.out.println("regular scan cost for table " + tableName + ": " + indexScan);
    for (String col : colMinMax.keySet()) {
      /***computing reduction factor*/
      Integer[] colRange = stats.getColumnInfo(col);
      int rangeSize = colRange[1] - colRange[0];

      Integer[] selectRange = colMinMax.get(col);
      double redFactor = (double) (selectRange[1] - selectRange[0]) / rangeSize;
      //System.out.println("reduction factor before for  " + col + " is: " + redFactor);
      // calc cost for clustered and unclustered indexes
      Tuple index_info = DBCatalog.getInstance().getIndexInfo(DBCatalog.getInstance().getTableName(tableName), col);
      boolean clustered = index_info.getElementAtIndex(0) == 1;
      // tree_height + pages*reduction factos
      int treeHeight = stats.getHeightforCol(col);
      //System.out.println("Tree height for column " + col + " is: " + treeHeight);
      if (clustered) {
        //System.out.println("clustered");
        indexScan = treeHeight + pages * redFactor;
      } else {
        //System.out.println("not clustered on column " + col);
        int numLeaves = stats.getNumLeaves(col);
        // if(redFactor==0){
        //  redFactor = 1;
        // }
        indexScan = treeHeight + (numLeaves * redFactor) + (numTuples * redFactor);
      }
      // now keep track of cost for each index on available column
      //System.out.println("index scan cost for " + col + ": " + indexScan);
      if (indexScan <= pages) { // means better than doing a full scan
        colIndexCost.put(col, indexScan);
      }
    }

    if (colIndexCost.size() == 0 || indexedExpressions == null) {
      // means we just do a full scan
      ArrayList<Expression> allExp = new ArrayList<>();
      allExp.addAll(unindexedExpressions);
      for (String col : indexedExpressions.keySet()) {
        allExp.add((indexedExpressions.get(col)));
      }
      Expression finalExp = createAndExpression(allExp);
      //System.out.println("Just doing regular full scan because more efficient");
      // case when it is more efficient to do a full scan, or no available index
      ScanOperator scanOp = new ScanOperator(schema, tablePath);
      return new SelectOperator(schema, scanOp, finalExp);
    }
    String minCol = (String) colIndexCost.keySet().toArray()[0];
    double minCost = colIndexCost.get(minCol);
    for (String c : colIndexCost.keySet()) {
      double curr = colIndexCost.get(c);
      if (curr < minCost) {
        minCost = curr;
        minCol = c;
      }
    }

    // now we have to separate all of the expressions into the one we can use
    // an index for (indexed expressions)
    // and the ones we cant unindexed exp
    Expression unindexed = combineUnindexed(minCol);
    // if uindexed expressions non existent, then can just use an index scan op
    if (unindexed == null && indexedExpressions != null) {
      //System.out.println(
        //  "Just using a regular index scan operator because only have indexed expressions");
      return createOP(unindexed, minCol);
    }
    // now cover case when there are unindexed and indexed expressions
    //System.out.println(
       // "Uisng IndexScan as a child because we have unindexed and indexed expressions");
    ScanOperator childOperator = createOP(unindexed, minCol);
    return new SelectOperator(schema, childOperator, unindexed);
    // actually want to return the column we want to index on and
  }

  /**
   * Creates an IndexScanOperator using the column that we found to have the smallest index scan
   * cost.
   *
   * @param unindexed All of the unindexed expressions that cannot be evaluated with an index on col
   * @param col The col whose index provides the lowest cost plan
   * @return A ScanOperator that is an IndexScan with high and low set for col
   * @throws FileNotFoundException
   */
  public ScanOperator createOP(Expression unindexed, String col) throws FileNotFoundException {
    Expression indexedExpr = indexedExpressions.get(col);
    Expression unIndexedExpr = unindexed;
    int low = colMinMax.get(col)[0];
    int high = colMinMax.get(col)[1];
    Integer ind = DBCatalog.getInstance().colIndex(DBCatalog.getInstance().getTableName(tableName), col);
    boolean clustered =
        DBCatalog.getInstance().getClustOrd(DBCatalog.getInstance().getTableName(tableName), col).getElementAtIndex(0) == 1;
    File indexFile = DBCatalog.getInstance().getAvailableIndex(tableName, col);
    //System.out.println("IndexedExpr: " + indexedExpr);
    //System.out.println("Unindexed Expr: " + unIndexedExpr);
    //System.out.println("Indexing column: " + col);
    return new IndexScanOperator(
        schema, tablePath, tableName, indexFile, ind, low, high, clustered);
  }

  /**
   * Combines all Expressions that do not included indexedCol or cannot be evaluated with an index
   * on indexedCol into one Expression
   *
   * @param indexedCol the column the index is built on
   * @return a single expression that is the conjunction of all unindexed expressions
   */
  public Expression combineUnindexed(String indexedCol) {
    ArrayList<Expression> unindexed = new ArrayList<>();
    for (String col : indexedExpressions.keySet()) {
      if (!col.equalsIgnoreCase(indexedCol)) {
        unindexed.add(indexedExpressions.get(col));
      }
    }
    unindexed.addAll(unindexedExpressions);
    return createAndExpression(unindexed);
  }

  /**
   * Sets the column range for col to [low,high]
   *
   * @param col The column we want to update the range on
   * @param low The lower bound for the column range
   * @param high The upper bound for the column range
   */
  public void colRange(String col, int low, int high) {
    if (!colMinMax.containsKey(col)) {
      Integer[] range = new Integer[2];
      Integer[] actual_range = stats.getColumnInfo(col);
      if (low < actual_range[0]) {
        low = actual_range[0];
      }
      if (high > actual_range[1]) {
        high = actual_range[1];
      }
      range[0] = low;
      range[1] = high;
      colMinMax.put(col, range);
    } else {
      Integer[] range = colMinMax.get(col);
      range[0] = Math.max(range[0], low);
      range[1] = Math.min(range[1], high);
      colMinMax.put(col, range);
    }
  }

  /**
   * Combines all the individual expressions into a single expression that is a conjunction
   *
   * @param expressions A list of expressions we want to 'AND' together
   * @return An expression with all individual expressions 'AND'-ed together
   */
  private Expression createAndExpression(List<Expression> expressions) {
    if (expressions.size() < 1) {
      return null;
    } else if (expressions.size() == 1) {
      return expressions.get(0);
    }
    AndExpression andExpression = new AndExpression(expressions.get(0), expressions.get(1));
    expressions.remove(0);
    expressions.remove(0);
    while (!expressions.isEmpty()) {
      andExpression = new AndExpression(andExpression, expressions.get(0));
      expressions.remove(0);
    }
    return andExpression;
  }

  /**
   * Get the individual expressions that are being 'AND'-ed toegther
   *
   * @param where the expression we want to decompose
   * @return A list containing the individual expressions in the where
   */
  public ArrayList<Expression> getAndExpressions(Expression where) {
    ArrayList<Expression> ands = new ArrayList<>();
    while (where instanceof AndExpression) {
      ands.add(((AndExpression) where).getRightExpression());
      where = ((AndExpression) where).getLeftExpression();
    }
    ands.add(where);
    return ands;
  }
}

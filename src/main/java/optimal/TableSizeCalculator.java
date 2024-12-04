package optimal;

import common.DBCatalog;
import common.TableStats;
import java.util.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import operator.LogicalOperators.*;

public class TableSizeCalculator {
  public List<String> tables;
  public Map<String, ArrayList<Expression>> selectExpressions;
  public Map<String, ArrayList<Expression>> joinExpressions;
  public Map<List<String>, Integer> joinTableCost;
  public Map<String, LogicalOperator> tableToOp;
  public VValueCalculator VValueCalculator;

  public TableSizeCalculator(
      Map<String, LogicalOperator> tableToOp,
      List<String> tables,
      Map<String, ArrayList<Expression>> selectExpressions,
      Map<String, ArrayList<Expression>> joinExpressions,
      HashMap<String, HashMap<String, Integer[]>> colMinMax) {
    this.tables = tables;
    this.selectExpressions = selectExpressions;
    this.joinExpressions = joinExpressions;
    this.joinTableCost = new HashMap<>();
    this.VValueCalculator =
        new VValueCalculator(selectExpressions, joinExpressions, tableToOp, colMinMax);
    this.VValueCalculator.buildVValues(this.tables);
  }

  public Integer getTableSize(List<String> rightTable, String leftTable) {
    Integer den = 1;
    if (rightTable == null) {
      TableStats stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(leftTable));
      List<String> myList = new ArrayList<>();
      myList.add(leftTable);
      Integer result = stats.getNumTuples();
      this.joinTableCost.put(myList, stats.getNumTuples());
      return result;
    } else if (rightTable.size() == 1) {
      String right = rightTable.get(0);
      String left = leftTable;
      TableStats stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(left));
      Integer leftSize = stats.getNumTuples();
      stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(right));
      Integer rightSize = stats.getNumTuples();
      Integer denom = processJoin(joinExpressions.get(right));
      System.out.println("denom " + ((leftSize * rightSize) / denom));
      return (leftSize * rightSize) / denom;
    } else {
      List<Expression> joins = joinExpressions.get(leftTable);
      Integer rightCost = this.joinTableCost.get(rightTable);
      TableStats stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(leftTable));
      List<String> myList = new ArrayList<>();
      myList.addAll(rightTable);
      myList.add(leftTable);
      Integer result = rightCost * stats.getNumTuples();
      this.joinTableCost.put(myList, result);
      return result;
    }
  }

  public Integer getTableSize(List<String> table) {
    return this.joinTableCost.get(table);
  }

  public Integer processJoin(List<Expression> joinExpressions) {
    Integer denom = 1;
    for (Expression expr : joinExpressions) {
      if (expr instanceof EqualsTo) {
        Expression left = ((EqualsTo) expr).getLeftExpression();
        Expression right = ((EqualsTo) expr).getRightExpression();
        String[] data = (left.toString()).split("\\.");
        String table = data[0];
        String col = data[1];
        Integer leftV = this.VValueCalculator.getVValue(table, col);
        data = (right.toString()).split("\\.");
        table = data[0];
        col = data[1];
        Integer rightV = this.VValueCalculator.getVValue(table, col);
        denom = denom * Integer.max(leftV, rightV);
      }
    }
    return denom;
  }
}

// for each table:
// have a cost
// have a vvalue for all its attributes
// have a size estimate

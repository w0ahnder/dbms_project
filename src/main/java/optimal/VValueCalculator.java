package optimal;

import common.*;
import java.util.*;
import net.sf.jsqlparser.expression.Expression;
import operator.LogicalOperators.*;

public class VValueCalculator {
  public Map<String, ArrayList<Expression>> selectExpressions;
  public Map<String, ArrayList<Expression>> joinExpressions;
  public Map<String, LogicalOperator> tableToOp;
  public HashMap<String, HashMap<String, Integer>> vvalues;
  public HashMap<String, HashMap<String, Integer[]>> colMinMax;

  public VValueCalculator(
      Map<String, ArrayList<Expression>> selectExpressions,
      Map<String, ArrayList<Expression>> joinExpressions,
      Map<String, LogicalOperator> tableToOp,
      HashMap<String, HashMap<String, Integer[]>> colMinMax) {
    this.tableToOp = tableToOp;
    this.selectExpressions = selectExpressions;
    this.joinExpressions = joinExpressions;
    this.vvalues = new HashMap<>();
    this.colMinMax = colMinMax;
  }

  // TODO: pass in the whole column in order to know which table it belongs to
  public Integer getVValue(List<String> tables, String col) {
    if ((tables.size()) == 1) {
      if ((selectExpressions.get(tables.get(0)).size()) == 0) {
        Integer[] range = DBCatalog.getInstance().getTableStats(tables.get(0)).getColumnInfo(col);
        return range[1] - range[0] + 1;
      } else {
        SelectLogOperator op = (SelectLogOperator) tableToOp.get(tables.get(0));
        return op.highKey - op.lowKey + 1;
      }
    } else {
    }
    return 0;
  }

  public Integer getVValue(String table, String col) {
    return this.vvalues.get(table).get(col);
  }

  public void buildVValues(List<String> tables) {
    HashMap<String, HashMap<String, Integer>> vvalues = new HashMap<>();
    for (String table : tables) {
      // DBCatalog.getInstance().getTableStats(table).getColumnInfos();
      HashMap<String, Integer> tval = new HashMap();
      TableStats stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(table));
      HashMap<String, Integer[]> columnInfo = stats.getColumnInfos();
      for (String col : columnInfo.keySet()) {
        Integer[] range = stats.getColumnInfo(col);
        tval.put(col, Integer.min(stats.getNumTuples(), range[1] - range[0] + 1));
      }
      vvalues.put(table, tval);
    }
    this.vvalues = vvalues;
    processSelection();
  }

  // For table R: calculate
  public void processSelection() {
    for (String table : this.selectExpressions.keySet()) {
      List<Expression> exprs = this.selectExpressions.get(table);
      TableStats stats = DBCatalog.getInstance().getTableStats(DBCatalog.getInstance().getTableName(table));
      HashMap<String, Integer[]> columnInfo = stats.getColumnInfos();
      for (Expression expr : exprs) {
        for (String col : columnInfo.keySet()) {

          ScanVisitor visitor = new ScanVisitor(expr, table + "." + col);
          visitor.evaluate_expr();
          Integer low = visitor.getLowKey();
          Integer high = visitor.getHighKey();
          Integer[] range = stats.getColumnInfo(col);
          Integer ranges = Integer.min(high, range[1]) - Integer.max(low, range[0]) + 1;
          this.vvalues.get(table).put(col, Integer.min(stats.getNumTuples(), ranges));
        }
      }
    }
  }
}

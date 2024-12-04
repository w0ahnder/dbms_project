package operator.LogicalOperators;

import UnionFind.UnionFind;
import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;

public class NewJoinLogOperator implements LogicalOperator {

  public ArrayList<Column> outputSchema;
  public List<LogicalOperator> childOperators;
  public Map<String, LogicalOperator> tableToOp;
  public List<String> tables;
  public Map<String, ArrayList<Expression>> selectExpressions;
  public Map<String, ArrayList<Expression>> joinExpressions;
  public HashMap<String, HashMap<String, Integer[]>> colMinMax;

  public UnionFind unionFind;

  public NewJoinLogOperator(
      ArrayList<Column> outputSchema,
      List<LogicalOperator> childOperators,
      List<String> tables,
      Map<String, ArrayList<Expression>> selectExpressions,
      Map<String, ArrayList<Expression>> joinExpressions,
      Map<String, LogicalOperator> tableToOp,
      HashMap<String, HashMap<String, Integer[]>> colMinMax,
      UnionFind union) {
    this.outputSchema = outputSchema;
    this.childOperators = childOperators;
    this.tables = tables;
    this.selectExpressions = selectExpressions;
    this.joinExpressions = joinExpressions;
    this.tableToOp = tableToOp;
    this.colMinMax = colMinMax;
    this.unionFind = union;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }

  public void printLog(PrintStream ps, int level) {
    ps.println("-".repeat(level) + "Join");
    for (Expression expr : unionFind.joins) {
      String left_attr = expr.toString().split("\\s")[0];
      if (expr instanceof EqualsTo) {
        ps.println(unionFind.printLogHelper(left_attr));
      } else {
        ps.println("[" + expr + "]");
        String line = unionFind.printLogHelper(left_attr);
        if (!Objects.equals(line, "")) {
          ps.println(line);
        }
        String right_attr = expr.toString().split("\\s")[2];
        String line_right = unionFind.printLogHelper(right_attr);
        if (!Objects.equals(line_right, "")) {
          ps.println(line_right);
        }
      }
    }
    for (LogicalOperator op : childOperators) {
      op.printLog(ps, level + 1);
    }
  }
}

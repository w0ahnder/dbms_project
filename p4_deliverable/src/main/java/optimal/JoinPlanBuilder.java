package optimal;

import common.*;
import java.util.*;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import operator.LogicalOperators.*;
import operator.PhysicalOperators.*;

public class JoinPlanBuilder {
  Map<String, LogicalOperator> tableToOp;
  List<String> optimalOrder;
  NewJoinLogOperator newJoinLogOperator;
  PhysicalPlanBuilder plan;
  ArrayList<Column> realSchema;

  public JoinPlanBuilder(
      Map<String, LogicalOperator> tableToOp,
      List<String> optimalOrder,
      NewJoinLogOperator newJoinLogOperator,
      ArrayList<Column> realSchema) {
    this.tableToOp = tableToOp;
    this.optimalOrder = optimalOrder;
    this.newJoinLogOperator = newJoinLogOperator;
    this.plan = new PhysicalPlanBuilder();
    this.realSchema = realSchema;
  }

  public Operator buildPlan() {
    Operator op = null;
    ArrayList<Column> outputSchema = new ArrayList<Column>();
    try {

      for (String table : optimalOrder) {
        tableToOp.get(table).accept(plan);
        outputSchema.addAll(tableToOp.get(table).getOutputSchema());
        if (op == null) {
          op = plan.returnResultTuple();
        } else {
          // System.out.println(table + " joinExp " +
          // this.newJoinLogOperator.joinExpressions.get(table));
          ArrayList<Expression> joinExp = this.newJoinLogOperator.joinExpressions.get(table);

          HashSet<String> cols = new HashSet<>();
          op.getOutputSchema().forEach(column -> cols.add(getColname(column)));
          plan.returnResultTuple()
              .getOutputSchema()
              .forEach(column -> cols.add(getColname(column)));
          // System.out.println("all cols from left and right " + cols);
          ArrayList<Expression> bnlJoinExpr = new ArrayList<>();
          for (Expression expr : joinExp) {
            String[] exprStr = expr.toString().split("\\s");
            //            System.out.println(Arrays.toString(exprStr));
            //            System.out.println(exprStr[0]);
            //            System.out.println(exprStr[2]);
            //            System.out.println(cols.contains(exprStr[0]) &&
            // cols.contains(exprStr[2]));
            if (cols.contains(exprStr[0]) && cols.contains(exprStr[2])) {
              bnlJoinExpr.add(expr);
            }
          }
          op =
              new BNLOperator(
                  outputSchema, op, plan.returnResultTuple(), createAndExpression(bnlJoinExpr));
        }
      }
      return new NewJoinOperator(op, outputSchema, this.realSchema);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return op;
  }

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

  public String getColname(Column col) {
    String cols = col.toString();
    String[] colStr = cols.split("\\.");
    if (colStr.length == 3) {
      StringBuilder build = new StringBuilder();
      build.append(colStr[0]).append(".").append(colStr[2]);
      return build.toString();
    }
    return cols;
  }
}

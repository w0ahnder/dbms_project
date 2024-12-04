package optimal;

import common.*;
import java.util.*;
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

  public JoinPlanBuilder(
      Map<String, LogicalOperator> tableToOp,
      List<String> optimalOrder,
      NewJoinLogOperator newJoinLogOperator) {
    this.tableToOp = tableToOp;
    this.optimalOrder = optimalOrder;
    this.newJoinLogOperator = newJoinLogOperator;
    this.plan = new PhysicalPlanBuilder();
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
          op =
              new BNLOperator(
                  outputSchema,
                  op,
                  plan.returnResultTuple(),
                  createAndExpression(this.newJoinLogOperator.joinExpressions.get(table)));
        }
      }
      return op;
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
}

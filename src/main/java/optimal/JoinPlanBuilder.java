package optimal;

import common.*;
import java.util.*;
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
          op = new BNLOperator(outputSchema, op, plan.returnResultTuple(), null);
        }
      }
      return op;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return op;
  }
}

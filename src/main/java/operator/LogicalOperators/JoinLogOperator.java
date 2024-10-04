package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class JoinLogOperator implements LogicalOperator {
  public LogicalOperator leftOperator;
  public LogicalOperator rightOperator;
  public Expression condition;
  public ArrayList<Column> outputSchema;

  public JoinLogOperator(
      ArrayList<Column> outputSchema,
      LogicalOperator leftOperator,
      LogicalOperator rightOperator,
      Expression condition) {
    this.outputSchema = outputSchema;
    this.leftOperator = leftOperator;
    this.rightOperator = rightOperator;
    this.condition = condition;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }
}

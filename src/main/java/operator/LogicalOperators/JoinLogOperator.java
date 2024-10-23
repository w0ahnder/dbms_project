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
  public String tempDir;

  public JoinLogOperator(
      ArrayList<Column> outputSchema,
      LogicalOperator leftOperator,
      LogicalOperator rightOperator,
      Expression condition, String tempDir) {
    this.outputSchema = outputSchema;
    this.leftOperator = leftOperator;
    this.rightOperator = rightOperator;
    this.condition = condition;
    this.tempDir = tempDir;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }
}

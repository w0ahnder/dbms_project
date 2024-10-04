package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class SelectLogOperator implements LogicalOperator {

  public Expression where;
  public LogicalOperator scan;
  public ArrayList<Column> outputSchema;

  public SelectLogOperator(Expression where, LogicalOperator scan) {
    this.where = where;
    this.scan = scan;
    this.outputSchema = scan.getOutputSchema();
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }
}

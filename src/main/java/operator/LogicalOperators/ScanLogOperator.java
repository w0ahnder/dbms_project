package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public class ScanLogOperator implements LogicalOperator {
  public ArrayList<Column> outputSchema;

  public String path;

  public ScanLogOperator(ArrayList<Column> outputSchema, String path) {
    this.outputSchema = outputSchema;
    this.path = path;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }
}

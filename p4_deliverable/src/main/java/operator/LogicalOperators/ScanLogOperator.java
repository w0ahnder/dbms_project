package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public class ScanLogOperator implements LogicalOperator {
  public ArrayList<Column> outputSchema;

  public String path;
  public String table;

  public ScanLogOperator(ArrayList<Column> outputSchema, String path, String table) {
    this.outputSchema = outputSchema;
    this.path = path;
    this.table = table;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }

  public void printLog(PrintStream ps, int level) {
    // TDOO: have to get table name for the scan operator
    StringBuilder builder = new StringBuilder();
    builder.append("-".repeat(Math.max(0, level)));
    builder.append("Leaf[ ").append(table).append("]");
    ps.println(builder);
  }
}

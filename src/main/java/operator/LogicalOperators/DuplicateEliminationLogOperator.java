package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public class DuplicateEliminationLogOperator implements LogicalOperator {

  public ArrayList<Column> outputSchema;
  public LogicalOperator sort;

  public DuplicateEliminationLogOperator(ArrayList<Column> outputSchema, LogicalOperator sort) {
    this.outputSchema = outputSchema;
    this.sort = sort;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }

  public void printLog(PrintStream ps, int level) {
    ps.println("DupElim");
    sort.printLog(ps, level + 1);
  }
}

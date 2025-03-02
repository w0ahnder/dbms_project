package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortLogOperator implements LogicalOperator {
  public List<OrderByElement> orderByElements;
  public LogicalOperator child;
  public ArrayList<Column> outputSchema;
  public Integer bufferPages;
  public String tempDir;
  List<Integer> sortConfig;

  public SortLogOperator(List<OrderByElement> orderByElements, LogicalOperator child) {
    this.orderByElements = orderByElements;
    this.child = child;
    this.outputSchema = child.getOutputSchema();
  }

  public SortLogOperator(
      List<OrderByElement> orderByElements,
      LogicalOperator child,
      Integer bufferPages,
      String tempDir) {
    this.orderByElements = orderByElements;
    this.child = child;
    this.outputSchema = child.getOutputSchema();
    this.bufferPages = bufferPages;
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

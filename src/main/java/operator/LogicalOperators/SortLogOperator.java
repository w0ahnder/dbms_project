package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.apache.logging.log4j.core.config.Order;

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

  public void printLog(PrintStream ps, int level){
    String res = "";
    StringBuilder builder = new StringBuilder();
    builder.append("-".repeat(Math.max(0, level)));
    builder.append("Sort[");
    for(int i=0; i<orderByElements.size();i++){
      String ob = orderByElements.get(i).toString();
      builder.append(ob);
      if(i<=orderByElements.size()-1);
      builder.append(", ");
    }
    builder.append("]");
    ps.println(builder);
    child.printLog(ps, level+1);
  }
}

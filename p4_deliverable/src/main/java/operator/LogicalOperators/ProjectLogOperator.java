package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectLogOperator implements LogicalOperator {

  public LogicalOperator child;
  public List<SelectItem> selectItems;
  public ArrayList<Column> outputSchema;

  public ProjectLogOperator(
      LogicalOperator child, List<SelectItem> selectItems, ArrayList<Column> newSchema) {
    this.child = child;
    this.selectItems = selectItems;
    this.outputSchema = newSchema;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }

  public void printLog(PrintStream ps, int level) {
    String res = "";
    StringBuilder builder = new StringBuilder();
    builder.append("-".repeat(Math.max(0, level)));
    builder.append("Project[");
    for (int i = 0; i < selectItems.size(); i++) {
      String ob = selectItems.get(i).toString();
      builder.append(ob);
      if (i <= selectItems.size() - 1)
        ;
      builder.append(", ");
    }
    builder.append("]");
    ps.println(builder);
    child.printLog(ps, level + 1);
  }
}

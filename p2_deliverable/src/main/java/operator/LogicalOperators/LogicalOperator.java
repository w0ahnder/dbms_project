package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public interface LogicalOperator {

  public abstract void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException;

  public ArrayList<Column> getOutputSchema();
}

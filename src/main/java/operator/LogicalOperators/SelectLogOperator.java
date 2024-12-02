package operator.LogicalOperators;
import common.PhysicalPlanBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import operator.PhysicalOperators.*;

public class SelectLogOperator implements LogicalOperator {

  public String table_name;
  public int ind;
  public boolean clustered;
  public File index_file;
  public int lowKey;
  public int highKey;
  public String table_path;
  public ArrayList<Column> outputSchema;
  public Expression indexedExpr;
  public Expression unIndexedExpr;
  public LogicalOperator scan;
  public Expression where;

  public SelectLogOperator(
      Expression indexedExpr,
      Expression unIndexedExpr,
      ArrayList<Column> outputSchema,
      String table_path,
      String table_name,
      int ind,
      boolean clustered,
      int low,
      int high,
      File index_file,
      LogicalOperator scan) {
    this.indexedExpr = indexedExpr;
    this.unIndexedExpr = unIndexedExpr;
    this.outputSchema = outputSchema;
    this.table_path = table_path;
    this.table_name = table_name;
    this.ind = ind;
    this.clustered = clustered;
    this.lowKey = low;
    this.highKey = high;
    this.index_file = index_file;
    this.scan = scan;
  }

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

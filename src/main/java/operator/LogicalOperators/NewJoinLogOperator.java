package operator.LogicalOperators;

import common.PhysicalPlanBuilder;
import java.io.FileNotFoundException;
import java.util.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class NewJoinLogOperator implements LogicalOperator {

  public ArrayList<Column> outputSchema;
  public List<LogicalOperator> childOperators;
  public Map<String, LogicalOperator> tableToOp;
  public List<String> tables;
  public Map<String, ArrayList<Expression>> selectExpressions;
  public Map<String, ArrayList<Expression>> joinExpressions;
  public HashMap<String, HashMap<String, Integer[]>> colMinMax;

  public NewJoinLogOperator(
      ArrayList<Column> outputSchema,
      List<LogicalOperator> childOperators,
      List<String> tables,
      Map<String, ArrayList<Expression>> selectExpressions,
      Map<String, ArrayList<Expression>> joinExpressions,
      Map<String, LogicalOperator> tableToOp,
      HashMap<String, HashMap<String, Integer[]>> colMinMax) {
    this.outputSchema = outputSchema;
    this.childOperators = childOperators;
    this.tables = tables;
    this.selectExpressions = selectExpressions;
    this.joinExpressions = joinExpressions;
    this.tableToOp = tableToOp;
    this.colMinMax = colMinMax;
  }

  @Override
  public void accept(PhysicalPlanBuilder physicalPlanBuilder) throws FileNotFoundException {
    physicalPlanBuilder.visit(this);
  }

  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }
}

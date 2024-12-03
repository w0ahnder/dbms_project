package operator.LogicalOperators;
import common.PhysicalPlanBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
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

  public void printLog(PrintStream ps, int level){
    String res = "";
    StringBuilder builder = new StringBuilder();
    builder.append("-".repeat(Math.max(0, level)));
    builder.append("Select[");
    if(where!=null) {
      builder.append(where.toString());
    }
    else{
      ArrayList<Expression> and = new ArrayList<>();
      if(unIndexedExpr!=null){
        and.add(unIndexedExpr);
      }
      if(indexedExpr!=null){
        and.add(indexedExpr);
      }

      builder.append( createAndExpression(and).toString());
    }
    builder.append("]");
    ps.println(builder.toString());
    scan.printLog(ps, level+1);
  }

  private Expression createAndExpression(List<Expression> expressions) {
    if (expressions.size() < 1) {
      return null;
    } else if (expressions.size() == 1) {
      return expressions.get(0);
    }
    AndExpression andExpression = new AndExpression(expressions.get(0), expressions.get(1));
    expressions.remove(0);
    expressions.remove(0);
    while (!expressions.isEmpty()) {
      andExpression = new AndExpression(andExpression, expressions.get(0));
      expressions.remove(0);
    }
    return andExpression;
  }
}

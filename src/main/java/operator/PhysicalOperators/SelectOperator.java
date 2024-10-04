package operator.PhysicalOperators;

import common.SelectVisitor;
import common.Tuple;
import java.io.*;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class SelectOperator extends Operator {
  ScanOperator scanOp;
  Expression expression;

  public SelectOperator(ArrayList<Column> outputSchema, ScanOperator sc, Expression expr) {
    super(outputSchema);
    expression = expr;
    scanOp = sc;
  }

  /** resets the child operator, which is ScanOperator */
  public void reset() {

    scanOp.reset();
  }

  /**
   * Keeps calling the child's getNextTuple() and evaluates the expression. If the expression is
   * true, we return the tuple. Otherwise, keep calling the child's getNextTuple() until there are
   * none left
   *
   * @return Tuple statisfying Expression expression or null
   */
  public Tuple getNextTuple() {
    Tuple curr = scanOp.getNextTuple();
    while (curr != null) {
      SelectVisitor sv = new SelectVisitor(curr, this.outputSchema, expression);
      if (sv.evaluate_expr()) {
        return curr;
      }
      curr = scanOp.getNextTuple();
    }
    scanOp.reset();
    return null;
  }
}

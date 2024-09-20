package operator;

import common.SelectVisitor;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/** Operator class that enables users to join outputs from more than one table */
public class JoinOperator extends Operator {
  Operator leftOperator;
  Operator rightOperator;
  Expression condition;
  Tuple leftTuple;

  public JoinOperator(
      ArrayList<Column> outputSchema,
      Operator leftOperator,
      Operator rightOperator,
      Expression condition) {
    super(outputSchema);
    this.leftOperator = leftOperator;
    this.rightOperator = rightOperator;
    this.condition = condition;
    this.leftTuple = leftOperator.getNextTuple();
  }

  /**
   * @return the leftOperator object
   */
  public Operator getLeftOperator() {
    return this.leftOperator;
  }

  /**
   * @return the rightOperator object
   */
  public Operator getRightOperator() {
    return this.rightOperator;
  }

  /**
   * @return the condition that joins left and right child
   */
  public Expression getCondition() {
    return this.condition;
  }

  /** Resets cursor on the operator to the beginning */
  public void reset() {
    this.leftOperator.reset();
    this.rightOperator.reset();
  }

  /**
   * @return joint nextTuple or null if we are at the end
   */
  public Tuple getNextTuple() {
    while (leftTuple != null) {
      Tuple rightTuple = rightOperator.getNextTuple();
      while (rightTuple != null) {
        ArrayList<Integer> elements = new ArrayList<>();
        elements.addAll(leftTuple.getAllElements());
        elements.addAll(rightTuple.getAllElements());
        Tuple curr = new Tuple(elements);

        if (this.condition == null) {
          return curr;
        }
        SelectVisitor sv = new SelectVisitor(curr, this.outputSchema, this.condition);
        if (sv.evaluate_expr()) {
          return curr;
        }
        rightTuple = rightOperator.getNextTuple();
      }
      rightOperator.reset();
      leftTuple = leftOperator.getNextTuple();
    }
    leftOperator.reset();
    return leftTuple;
  }
}

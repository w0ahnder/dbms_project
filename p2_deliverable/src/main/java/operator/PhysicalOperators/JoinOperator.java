package operator.PhysicalOperators;

import common.SelectVisitor;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * Operator class that enables users to join outputs from more than one table. Operator has left and
 * right child where right child could only be a Scan/ Select Operator. Left Child could be a join
 * operator too. Condition is the expressions that involves operands that contain Any of the tables
 * in the left child and the right child
 */
public class JoinOperator extends Operator {
  Operator leftOperator;
  Operator rightOperator;
  Expression condition;
  Tuple leftTuple;

  /**
   * Creates a JoinOperator Object
   *
   * @param outputSchema the Schema of the output which becomes its child.
   * @param leftOperator the left child to be joined, should be an Operator
   * @param rightOperator the right child to be joined, should be an Operator
   */
  public JoinOperator(
      ArrayList<Column> outputSchema,
      Operator leftOperator,
      Operator rightOperator,
      Expression condition) {
    // have to make sure all the tables are represented
    // when creating curr we concatenate the left and right tuples,
    // but not the schemas so
    super(outputSchema);
    this.leftOperator = leftOperator;
    this.rightOperator = rightOperator;
    this.condition = condition;
    this.leftTuple = leftOperator.getNextTuple();
  }

  public ArrayList<Column> concatSchema() {
    ArrayList<Column> conc = new ArrayList<Column>();
    conc.addAll(leftOperator.getOutputSchema());
    conc.addAll(rightOperator.getOutputSchema());
    return conc;
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
   * Calls getNextTuple on left Operator if that is null, it returns null Calls getNextTuple on
   * right Operator if that is null, it returns null If there is a valid response from left and
   * right tuples, it evaluates condition and returns the concatenated tuple. if not, it goes back
   * to check for any other valid tuple.
   *
   * @return joint nextTuple or null if we are at the end
   */
  public Tuple getNextTuple() { // left tuple is null here, enter from
    // external sort next = op.getNextTuple()
    if (leftTuple == null) {
      this.leftTuple = leftOperator.getNextTuple();
    }
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
        // concatSchema has to join the two schema because sometimes a table in the right tuple
        // is not mentioned in the left schema so the long value is not updated
        // in visit equals.
        SelectVisitor sv = new SelectVisitor(curr, concatSchema(), this.condition);
        if (sv.evaluate_expr()) {
          return curr;
        }
        rightTuple = rightOperator.getNextTuple();
      }
      rightOperator.reset();
      leftTuple = leftOperator.getNextTuple();
    }
    // leftOperator.reset();
    return leftTuple; // return null
  }
}

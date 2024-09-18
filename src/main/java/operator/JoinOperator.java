package operator;

import common.SelectVisitor;
import common.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;

public class JoinOperator extends Operator {
  Operator leftOperator;
  Operator rightOperator;
  Expression condition;

  public JoinOperator(ArrayList<Column> outputSchema, Operator leftOperator, Operator rightOperator,
      Expression condition) {
    super(outputSchema);
    this.leftOperator = leftOperator;
    this.rightOperator = rightOperator;
    this.condition = condition;
  }

  public void reset() {
    this.leftOperator.reset();
    this.rightOperator.reset();
  }

  public Tuple getNextTuple() {
    Tuple leftTuple = leftOperator.getNextTuple();
    while (leftTuple != null) {
      Tuple rightTuple = rightOperator.getNextTuple();
      while (rightTuple != null) {
        ArrayList<Integer> elements = new ArrayList<>();
        elements.addAll(leftTuple.getAllElements());
        elements.addAll(rightTuple.getAllElements());
        Tuple curr = new Tuple(elements);
        // TODO: Replace SelectVisitor with JoinVisitor
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

// TODO 1: implement JoinVisitor
// TODO 2: Expressions, Left, Right
package operator;

import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public class DuplicateEliminationOperator extends Operator {

  SortOperator so;

  Tuple curr;

  public DuplicateEliminationOperator(ArrayList<Column> outputSchema, SortOperator sortOp) {
    super(outputSchema);
    so = sortOp;
    curr = null;
  }

  @Override
  public void reset() {
    curr = new Tuple(new ArrayList<>());
    so.reset();
  }

  @Override
  public Tuple getNextTuple() {
    Tuple next = so.getNextTuple();
    if (curr == null) {
      curr = next;
      return curr;
    }
    while (curr.equals(next)) {
      next = so.getNextTuple();
    }
    curr = next;
    return next;
  }
}

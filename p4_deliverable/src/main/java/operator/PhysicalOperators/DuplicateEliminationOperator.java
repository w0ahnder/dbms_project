package operator.PhysicalOperators;

import common.Tuple;
import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/** Class that extends Operator Class to handle SQL queries with the DISTINCT keyword. */
public class DuplicateEliminationOperator extends Operator {

  Operator so;

  Tuple curr;

  /**
   * Creates a DuplicateElimination Object
   *
   * @param outputSchema the Schema of the output which becomes its child.
   * @param sortOp the child operator which must be a SortOperator object
   */
  public DuplicateEliminationOperator(ArrayList<Column> outputSchema, Operator sortOp) {
    super(outputSchema);
    so = sortOp;
    curr = null;
  }

  public void printPhys(PrintStream ps, int level) {
    ps.println("DupElim");
    so.printPhys(ps, level + 1);
  }

  /**
   * Resets pointer on the operator object to the beginning. Achieves this by resetting its child
   * operator and resetting "curr" to null
   */
  @Override
  public void reset() {
    curr = null;
    so.reset();
  }

  /**
   * Get next tuple from operator
   *
   * @return Tuple, or null if we are at the end. Retrieves next tuple by calling getNextTuple() on
   *     its child operator and checking that the tuple is not a duplicate of the previous tuple
   *     gotten
   */
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

package operator;

import common.Tuple;
import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;


/**
 * Class that extends Operator Class to handle SQL queries with the ORDER BY keyword.
 */
public class SortOperator extends Operator {
  List<OrderByElement> orderByElements;
  Operator op;

  ArrayList<Tuple> result = new ArrayList<>();

  Integer curr;

  /**
   * Creates a SortOperator Object
   *
   * @param  outputSchema the Schema of the output which becomes its child.
   * @param orderElements a list of the columns which should be used to sort the tuples
   * @param sc the child operator
   */
  public SortOperator(
      ArrayList<Column> outputSchema, List<OrderByElement> orderElements, Operator sc) {
    super(outputSchema);
    orderByElements = orderElements;
    op = sc;
    curr = 0;
    Tuple tuple = sc.getNextTuple();
    while (tuple != null) {
      result.add(tuple);
      tuple = sc.getNextTuple();
    }

    result.sort(new TupleComparator());

  }
  /** Resets pointer on the operator object to the beginning. Achieves this by resetting its child operator
   * and resetting its "curr" field */
  public void reset() {
    curr = 0;
    op.reset();
  }

  /**
   * Get next tuple from operator
   * @return Tuple, or null if we are at the end. Retrieves next tuple by indexing with "curr" into "result"
   * which is a sorted list of all the tuples from its child operator.
   */
  public Tuple getNextTuple() {

    if (curr == result.size()) {
      this.reset();
      return null;
    }
    curr += 1;
    return result.get(curr - 1);
  }

  /**
   * Custom Comparator class to compare two tuples based on columns
   *
   */
  private class TupleComparator implements Comparator<Tuple> {
    public TupleComparator() {
    }

    /**
     * Custom Comparator class to compare to tuples when sorting them.
     * If there is a list of OrderByElements, it compares first with the columns in the list and then
     * with the remaining columns not in the list but in the output schema in the order of its appearance
     *<p>
     * @param  t1 the first tuple to compare
     * @param t2 the second tuple being compared to t1
     * @return a number which is either 1, 0 or -1
     */
    @Override
    public int compare(Tuple t1, Tuple t2) {
      if (!orderByElements.isEmpty()) {
        Map<String, Integer> columnToIndexMap = new HashMap<>();
        for (int i = 0; i < outputSchema.size(); i += 1) {
          columnToIndexMap.put(outputSchema.get(i).getFullyQualifiedName(), i);
        }
        for (OrderByElement orderByElement : orderByElements) {
          Column orderToCol = (Column) orderByElement.getExpression();
          String col = orderToCol.getFullyQualifiedName();
          int t1_val = t1.getElementAtIndex(columnToIndexMap.get(col));
          int t2_val = t2.getElementAtIndex(columnToIndexMap.get(col));

          if (t1_val > t2_val) {
            return 1;
          } else if (t1_val < t2_val) {
            return -1;
          }
          columnToIndexMap.remove(col);
        }
        for (Column col : outputSchema) {
          String col_str = col.getFullyQualifiedName();
          if (columnToIndexMap.containsKey(col_str)) {
            int t1_val = t1.getElementAtIndex(columnToIndexMap.get(col_str));
            int t2_val = t2.getElementAtIndex(columnToIndexMap.get(col_str));
            if (t1_val > t2_val) {
              return 1;
            } else if (t1_val < t2_val) {
              return -1;
            }
          }
        }
        return 0;
      } else {
        return t1.toString().compareTo(t2.toString());
      }
    }
  }
}

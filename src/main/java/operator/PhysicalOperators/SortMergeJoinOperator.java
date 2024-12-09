package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import java.io.IOException;
import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** Class that extends Operator Class to handle join SQL queries using Sort Merge. */
public class SortMergeJoinOperator extends Operator {

  SortOperator left;

  SortOperator right;

  List<OrderByElement> orderElements_left;

  List<OrderByElement> orderElements_right;

  int tuple_count_right = 0;

  Tuple left_curr;

  Tuple right_curr;

  TupleComparator comparator = new TupleComparator();

  public int partition_indx;

  /**
   * Creates a SortMergeJoinOperator Object
   *
   * @param schema the Schema of the output which becomes its child.
   * @param table_1 the child operator which is the outer table and is of type Operator
   * @param table_2 the child operator which is the inner table and is of type Operator
   * @param orderElements_left the list of columns for which we perform the join on table 1
   * @param orderElements_right the list of columns for which we perform the join on table 2
   */
  public SortMergeJoinOperator(
      ArrayList<Column> schema,
      SortOperator table_1,
      SortOperator table_2,
      List<OrderByElement> orderElements_left,
      List<OrderByElement> orderElements_right) {
    super(schema);
    this.left = table_1;
    this.right = table_2;
    this.orderElements_left = orderElements_left;
    this.orderElements_right = orderElements_right;
    partition_indx = -1;
    tuple_count_right = 0;
    left_curr = left.getNextTuple();
    right_curr = right.getNextTuple();
  }

  /**
   * Resets pointer on the operator object to the beginning. Achieves this by resetting its left and
   * right children
   */
  @Override
  public void reset() {
    right.reset();
    left.reset();

  }

  public void reset(int index) throws IOException {}

  /**
   * Get next tuple from operator
   *
   * @return Tuple, or null if we are at the end. Retrieves next tuple by calling getNextTuple() on
   *     its left and right child operator and checking that the values at the required column match
   *     each other
   */
  public Tuple getNextTuple() {
    if(left_curr==null){
      left_curr = left.getNextTuple();
    }
    try {
      while (left_curr != null) {
        if (right_curr == null) {
          if (partition_indx != -1) {
            right.reset(partition_indx);
            tuple_count_right = partition_indx;
            right_curr = right.getNextTuple();
            tuple_count_right++;
          } else {
            return null;
          }
          left_curr = left.getNextTuple();
        } else if (comparator.compare(left_curr, right_curr) < 0) {
          if (partition_indx != -1) { //means we created a partition, since right>left, reset the right partition and get next left tuple
            right.reset(partition_indx);
            tuple_count_right = partition_indx;
            right_curr = right.getNextTuple();
            tuple_count_right++;
          }
          left_curr = left.getNextTuple();
        } else if (comparator.compare(left_curr, right_curr) > 0) { //means left is larger, so incrememnt right?
          right_curr = right.getNextTuple();
          tuple_count_right++;
          // partition_indx = tuple_count_right;
        } else {
          // means that they are = ?
          ArrayList<Integer> elements = new ArrayList<>();
          elements.addAll(left_curr.getAllElements());
          elements.addAll(right_curr.getAllElements());
          Tuple result = new Tuple(elements);

          if (partition_indx == -1) {
            partition_indx = tuple_count_right;
          }
          right_curr = right.getNextTuple(); // increment position now
          tuple_count_right++;

          // now compare this next tuple with left; if they are not equal
          // then we get the next left tuple, and reset the partition for right relation
          if (result != null) return result;
        }
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  /**
   * Custom Comparator class to compare to tuples two tuples to determine if to move to the next
   * tuple on the left(outer) table or on the right(inner) table.
   *
   * <p>
   */
  private class TupleComparator implements Comparator<Tuple> {
    public TupleComparator() {}

    /**
     * @param t1 the first tuple to compare
     * @param t2 the second tuple being compared to t1
     * @return a number which is either 1, 0 or -1
     */
    @Override
    public int compare(Tuple t1, Tuple t2) {
      if (!orderElements_left.isEmpty() && !orderElements_right.isEmpty()) {
        Map<String, Integer> columnToIndexMap_left = createMap(left);
        Map<String, Integer> columnToIndexMap_right = createMap(right);
        ;

        for (int i = 0; i < orderElements_left.size(); i += 1) {
          Column orderToCol_left = (Column) orderElements_left.get(i).getExpression();
          String col_left = orderToCol_left.getFullyQualifiedName();

          Column orderToCol_right = (Column) orderElements_right.get(i).getExpression();
          String col_right = orderToCol_right.getFullyQualifiedName();

          int t1_val = t1.getElementAtIndex(columnToIndexMap_left.get(col_left));

          int t2_val = t2.getElementAtIndex(columnToIndexMap_right.get(col_right));

          if (t1_val > t2_val) {
            return 1;
          } else if (t1_val < t2_val) {
            return -1;
          }
        }
        return 0;
      } else {
        return t1.toString().compareTo(t2.toString());
      }
    }

    private Map<String, Integer> createMap(SortOperator right) {
      Map<String, Integer> columnToIndexMap_right = new HashMap<>();
      ArrayList<Column> outputSchema_2 = right.getOutputSchema();
      for (int i = 0; i < outputSchema_2.size(); i += 1) {
        String name = outputSchema_2.get(i).getTable().getName();
        String ali = outputSchema_2.get(i).getTable().getSchemaName();
        String col_name = outputSchema_2.get(i).getColumnName();
        // gives Sailors, and when alias=true, it tries to get Sailors from alias map

        String full = name + "." + col_name;
        if (DBCatalog.getInstance().getUseAlias()) {
          full = ali + "." + col_name;
        }
        columnToIndexMap_right.put(full, i);
      }
      return columnToIndexMap_right;
    }
  }
}

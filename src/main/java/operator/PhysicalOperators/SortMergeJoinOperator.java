package operator.PhysicalOperators;

import common.Convert;
import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import common.TupleWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortMergeJoinOperator extends Operator {

  SortOperator left;

  SortOperator right;

  List<OrderByElement> orderElements_left;

  List<OrderByElement> orderElements_right;

  int tuple_count_right = 0;

  Tuple left_curr;

  Tuple right_curr;

  Tuple right_partition;

  TupleComparator comparator = new TupleComparator();

  String tempDir;

  TupleWriter tw;

  TupleReader reader;
  public int partition_indx;

  public SortMergeJoinOperator(
      ArrayList<Column> schema,
      SortOperator table_1,
      SortOperator table_2,
      String tempDir,
      List<OrderByElement> orderElements_left,
      List<OrderByElement> orderElements_right) {
    super(schema);
    System.out.println(orderElements_left);
    this.left = table_1;
    this.right = table_2;
    this.orderElements_left = orderElements_left;
    this.orderElements_right = orderElements_right;
    this.tempDir = tempDir + "/join" + UUID.randomUUID();
    File tmp = new File(this.tempDir);
    partition_indx = 0;
    tuple_count_right = 0;
    left_curr = left.getNextTuple();
    right_curr = right.getNextTuple();
    tmp.mkdir();
  }

  @Override
  public void reset() {
    right.reset();
    left.reset();
    /*if (reader == null) return;
    try {
      reader.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }
    */

  }

  @Override
  public void reset(int index) throws IOException {}

  public Tuple getNextTuple2() {
    if (reader == null) {
      return null;
    }
    try {
      Tuple tp = reader.read();
      return tp;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  public Tuple getNextTuple(){
    try {
      while (left_curr != null && right_curr != null) {
        if (comparator.compare(left_curr, right_curr) < 0) {
          left_curr = left.getNextTuple();
        } else if (comparator.compare(left_curr, right_curr) > 0) {
          right_curr = right.getNextTuple();
          tuple_count_right++;
          partition_indx = tuple_count_right;
        } else {
          //means that they are = ?
          ArrayList<Integer> elements = new ArrayList<>();
          elements.addAll(left_curr.getAllElements());
          elements.addAll(right_curr.getAllElements());
          Tuple result = new Tuple(elements);

          right_curr = right.getNextTuple();//increment position now
          tuple_count_right++;
          //now compare this next tuple with left; if they are not equal
          //then we get the next left tuple, and reset the partition for right relation
          left_curr = left.getNextTuple();
          right.reset(partition_indx);
          //now we change our current location to be same as partition
          tuple_count_right = partition_indx;
          //read tuple at this index
          right_curr = right.getNextTuple();

          if(result!=null) return result;
        }
        return null;
      }
    }
      catch(IOException e){
      return null;
      }
    return null;
  }
  public void join() {
    try {
      tw = new TupleWriter(tempDir + "/joins");
      left_curr = left.getNextTuple();
      System.out.println(left_curr);
      right_curr = right.getNextTuple();
      System.out.println(right_curr);
      right_partition = right_curr;
      tuple_count_right += 1;
      while (left_curr != null && right_partition != null) {
        while (comparator.compare(left_curr, right_partition) < 0) {
          left_curr = left.getNextTuple();
          if (left_curr == null) tw.close();
        }
        while (comparator.compare(left_curr, right_partition) > 0) {
          right_partition = right.getNextTuple();
          if (right_partition == null) tw.close();
          tuple_count_right += 1;
        }
        right.reset(tuple_count_right);
        right_curr = right.getNextTuple();
        while (left_curr != null && comparator.compare(left_curr, right_partition) == 0) {
          right.reset(tuple_count_right);
          right_curr = right.getNextTuple();
          while (right_curr != null && comparator.compare(left_curr, right_curr) == 0) {
            ArrayList<Integer> elements = new ArrayList<>();
            elements.addAll(left_curr.getAllElements());
            elements.addAll(right_curr.getAllElements());
            Tuple result = new Tuple(elements);
            tw.write(result);
            right_curr = right.getNextTuple();
          }
          right.reset();
          left_curr = left.getNextTuple();
        }
        right_partition = right_curr;
      }
      tw.close();
      reader = new TupleReader(new File(tempDir + "/joins"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // remove
    try {
      Convert conv = new Convert(tempDir + "/joins", new PrintStream(tempDir + "/joinshuman"));
      conv.bin_to_human();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<Column> concatSchema() {
    ArrayList<Column> conc = new ArrayList<Column>();
    conc.addAll(left.getOutputSchema());
    conc.addAll(right.getOutputSchema());
    return conc;
  }

  private class TupleComparator implements Comparator<Tuple> {
    public TupleComparator() {}

    /**
     * Custom Comparator class to compare to tuples when sorting them. If there is a list of
     * OrderByElements, it compares first with the columns in the list and then with the remaining
     * columns not in the list but in the output schema in the order of its appearance
     *
     * <p>
     *
     * @param t1 the first tuple to compare
     * @param t2 the second tuple being compared to t1
     * @return a number which is either 1, 0 or -1
     */
    @Override
    public int compare(Tuple t1, Tuple t2) {
      System.out.println(t2);
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

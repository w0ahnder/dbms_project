package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** Class that extends Operator Class to handle SQL queries with the ORDER BY keyword. */
public class InMemorySortOperator extends SortOperator {
  List<OrderByElement> orderByElements;
  Operator op;

  ArrayList<Tuple> result = new ArrayList<>();

  Integer curr;

  /**
   * Creates a SortOperator Object
   *
   * @param outputSchema the Schema of the output which becomes its child.
   * @param orderElements a list of the columns which should be used to sort the tuples
   * @param sc the child operator
   */
  public InMemorySortOperator(
      ArrayList<Column> outputSchema, List<OrderByElement> orderElements, Operator sc) {
    super(outputSchema, orderElements, sc);

    if (orderElements.size() == 0) {
      this.orderByElements = new ArrayList<>();
      for (Column c : outputSchema) {

        OrderByElement ob = new OrderByElement();
        Column newc = new Column();
        String table_name =
            DBCatalog.getInstance().getUseAlias()
                ? c.getTable().getSchemaName()
                : c.getTable().getName();
        Table t = new Table(table_name);
        newc.setTable(t);
        newc.setColumnName(c.getColumnName());
        ob.setExpression(c);
        this.orderByElements.add(ob);
      }
    } else {
      this.orderByElements = orderElements;
    }

    curr = 0;
    this.op = sc;
    Tuple tuple = sc.getNextTuple();
    while (tuple != null) {
      result.add(tuple);
      tuple = sc.getNextTuple();
    }
    sort(result);
  }

  public ArrayList<Tuple> sort(ArrayList<Tuple> result) {
    try {
      result.sort(new TupleComparator());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public ArrayList<Tuple> getResult() {
    return result;
  }

  public ArrayList<Tuple> getResult() {
    return result;
  }

  /**
   * Get next tuple from operator
   *
   * @return Tuple, or null if we are at the end. Retrieves next tuple by indexing with "curr" into
   *     "result" which is a sorted list of all the tuples from its child operator.
   */
  public Tuple getNextTuple() {
    if (curr == result.size()) {
      return null;
    }
    curr += 1;
    return result.get(curr - 1);
  }


  /** Custom Comparator class to compare two tuples based on columns */
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
      Map<String, Integer> columnToIndexMap = new HashMap<>();
      for (int i = 0; i < outputSchema.size(); i += 1) {
        String name = outputSchema.get(i).getTable().getName();
        String ali = outputSchema.get(i).getTable().getSchemaName();
        String col_name = outputSchema.get(i).getColumnName();
        // gives Sailors, and when alias=true, it tries to get Sailors from alias map

        String full = name + "." + col_name;
        if (DBCatalog.getInstance().getUseAlias()) {
          full = ali + "." + col_name;
        }
        columnToIndexMap.put(full, i);
      }

      for (OrderByElement orderByElement : orderByElements) {
        Column orderToCol = (Column) orderByElement.getExpression();
        String col = orderToCol.getFullyQualifiedName();

        // Sailors.C
        /*if(DBCatalog.getInstance().getUseAlias()){
                    String[] names = col.split("\\.");
                    col = DBCatalog.getInstance().getTableName(names[0]) + "." + names[names.length - 1];
                  }
        */

        int t1_val = t1.getElementAtIndex(columnToIndexMap.get(col));

        int t2_val = t2.getElementAtIndex(columnToIndexMap.get(col));

        if (t1_val > t2_val) {
          return 1;
        } else if (t1_val < t2_val) {
          return -1;
        }
        //        columnToIndexMap.remove(col);
      }

      for (Column col : outputSchema) {

        String name = col.getTable().getName();
        String ali = col.getTable().getSchemaName();
        String col_name = col.getColumnName();
        String col_str = name + "." + col_name;
        if (DBCatalog.getInstance().getUseAlias()) {
          col_str = ali + "." + col_name;
        }

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
    }
  }
}

package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/** Class that extends Operator Class to handle SQL queries that selects specific columns. */
public class ProjectOperator extends Operator {
  Operator childOperator;
  List<SelectItem> selectItems;
  ArrayList<Column> oldSchema;

  public ProjectOperator(
      ArrayList<Column> newSchema,
      ArrayList<Column> oldSchema,
      Operator childOperator,
      List<SelectItem> selectItems)
      throws FileNotFoundException {
    super(newSchema);
    this.childOperator = childOperator;
    this.selectItems = selectItems;
    this.oldSchema = oldSchema;
  }

  /** resets the child operator */
  public void reset() {
    childOperator.reset();
  }

  /** returns the child operator of this operator */
  public Operator getChildOperator() {
    return this.childOperator;
  }

  /**
   * Calls the childOperator's getNextTuple() and selects the appropriate entries corresponding to
   * the columns and tables in the SELECT body
   *
   * @return Tuple that is the selected columns from the tuple returned by childOperator's
   *     getNextTuple()
   */
  public Tuple getNextTuple() {
    Tuple nextTuple = childOperator.getNextTuple();
    if (nextTuple == null) {
      reset();
      return nextTuple;
    }
    if (selectItems.get(0).toString().equals("*")) {
      return nextTuple;
    }
    // get all tables and columns reference in SELECT body
    ArrayList<String> tables = new ArrayList<>();
    ArrayList<String> columns = new ArrayList<>();

    for (SelectItem s : selectItems) {
      Column c = (Column) ((SelectExpressionItem) s).getExpression();
      // if we use aliases, then this adds the alias instead like S.C adds S and Sailors.C adds
      // Sailors
      String t = c.getTable().getName();
      if (DBCatalog.getInstance().getUseAlias()) {
        t = c.getTable().getSchemaName();
      }
      tables.add(t);
      columns.add(c.getColumnName());
    }
    ArrayList<Integer> resTuple = new ArrayList<>();
    for (int i = 0; i < tables.size(); i++) {
      // get index of column in schema
      int index = getIndex(columns.get(i), tables.get(i));
      resTuple.add(nextTuple.getElementAtIndex(index));
    }
    return new Tuple(resTuple);
  }

  /**
   * Given a column name and table/alias, looks for the index in oldSchema whose name.column matches
   * the name of the table and column in the select body
   *
   * @return index of element to be retrieved for projection
   */
  public int getIndex(String column, String name) {
    for (int i = 0; i < oldSchema.size(); i++) {
      Column curr = oldSchema.get(i);
      String c = curr.getColumnName();
      // String t = curr.getTable().getName();
      String al = curr.getTable().getSchemaName();
      String table_name = DBCatalog.getInstance().getTableName(name);
      if (DBCatalog.getInstance().getUseAlias()) {
        table_name = al;
      }

      // if (DBCatalog.getInstance().getUseAlias()) t = curr.getTable().getName();
      if (c.equalsIgnoreCase(column) && table_name.equals(name)) {
        return i;
      }
    }
    return -1;
  }
}

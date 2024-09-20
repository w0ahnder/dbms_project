package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

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

  public void reset() {
    childOperator.reset();
  }

  public Tuple getNextTuple() {
    Tuple nextTuple = childOperator.getNextTuple();
    if (nextTuple == null) {
      reset();
      return nextTuple;
    }
    /*Set<String> selectedColumnNames =
       selectItems.stream()
           .filter(item -> item instanceof SelectExpressionItem)
           .map(item -> (Column) ((SelectExpressionItem) item).getExpression())
           .map(Column::getColumnName)
           .collect(Collectors.toSet());

    */
    // keep track of schema name and column name in two separate
    ArrayList<String> tables = new ArrayList<>();
    ArrayList<String> columns = new ArrayList<>();
    for (SelectItem s : selectItems) {
      Column c = (Column) ((SelectExpressionItem) s).getExpression();
      tables.add(c.getTable().getName());
      columns.add(c.getColumnName());
    }
    ArrayList<Integer> resTuple = new ArrayList<>();
    for (int i = 0; i < tables.size(); i++) {
      // get index of column in schema
      int index = getIndex(columns.get(i), tables.get(i));
      resTuple.add(nextTuple.getElementAtIndex(index));
    }

    /*Map<String, Integer> schemaIndexMap = new HashMap<>();

    //column name keeps duplicates for self join
    for (int i = 0; i < this.outputSchema.size(); i++) {
      schemaIndexMap.put(this.outputSchema.get(i).getColumnName(), i);
    }
    */

    // Create the result tuple based on selected columns
    /*ArrayList<Integer> returnTup =
    selectedColumnNames.stream()
        .map(columnName -> schemaIndexMap.getOrDefault(columnName, -1))
        .filter(index -> index != -1)
        .map(nextTuple::getElementAtIndex)
        .collect(Collectors.toCollection(ArrayList::new));
        */
    return new Tuple(resTuple);
  }

  public int getIndex(String column, String name) {
    for (int i = 0; i < oldSchema.size(); i++) {
      Column curr = oldSchema.get(i);
      String c = curr.getColumnName();
      String t = curr.getTable().getName();

      if (DBCatalog.getInstance().getUseAlias()) t = curr.getTable().getSchemaName();
      if (c.equalsIgnoreCase(column) && t.equals(name)) {
        return i;
      }
    }
    return -1;
  }
}

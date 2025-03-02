package common;

import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * Class that is responsible for evaluating conditions in WHERE expressions that contains columns.
 * Extends ExpressVisit
 */
public class SelectVisitor extends ExpressVisit {
  Expression expression;
  ArrayList<Column> schema;
  Tuple tuple;

  public SelectVisitor(Tuple t, ArrayList<Column> tableSchema, Expression expr) {
    expression = expr;
    schema = tableSchema;
    tuple = t;
  }

  /**
   * evaluates expression and returns result
   *
   * @return boolean value of the expression evaluated
   */
  public boolean evaluate_expr() {
    expression.accept(this);
    return return_cond();
  }

  /**
   * gets the value of a column
   *
   * @param column Column to be visited
   */
  public void visit(Column column) {
    String[] data = (column.toString()).split("\\.");
    String table = data[0];
    String col = data[1];

    int count = 0;
    for (Column c : schema) {
      String colName = c.getColumnName();

      String alias = c.getTable().getSchemaName();
      // if an alias, this will be null
      String column_table = DBCatalog.getInstance().getTableName(c.getTable().getName());

      if (DBCatalog.getInstance().getUseAlias()) {
        column_table = alias;
      }
      if (colName.equalsIgnoreCase(col) && table.equalsIgnoreCase(column_table)) {
        longValue = tuple.getElementAtIndex(count);
        return;
      }
      count++;
    }
  }
}

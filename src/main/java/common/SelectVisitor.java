package common;

import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

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
    // S.A
    String[] data = (column.toString()).split("\\.");
    // could be an alias
    String table = data[0];
    String col = data[1];
    String tablename = DBCatalog.getInstance().getTableName(table);
    int count = 0;
    // get alias and use as table name
    for (Column c : schema) {
      String colName = c.getColumnName();
      String alias = c.getTable().getSchemaName();
      if (alias != null) {
        tablename = alias;
      }
      if (colName.equalsIgnoreCase(col) && tablename.equalsIgnoreCase(table)) {
        longValue = tuple.getElementAtIndex(count);
        return;
      }
      count++;
    }
  }
}

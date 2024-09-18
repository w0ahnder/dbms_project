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

  public boolean evaluate_expr() {
    expression.accept(this);
    return return_cond();
  }

  public void visit(Column column) {
    String[] data = (column.toString()).split("\\.");
    String table = data[0];
    String col = data[1];
    int count = 0;
    for (Column c : schema) {
      String colName = c.getColumnName();
      String tableName = c.getTable().getName();
      if (colName.equalsIgnoreCase(col) && tableName.equalsIgnoreCase(table)) {
        longValue = tuple.getElementAtIndex(count);
        return;
      }
      count++;
    }
  }
}

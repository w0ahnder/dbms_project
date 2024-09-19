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
   * @return boolean value of the expression evaluated
   */
  public boolean evaluate_expr() {
    expression.accept(this);
    return return_cond();
  }

  /**
   * gets the value of a column
   * @param column Column to be visited
   */
  public void visit(Column column) {
    String[] data = (column.toString()).split("\\.");
    String table = data[0];
    System.out.println("table:"+table);
    System.out.println("Column column:"+column.toString());
    String col = data[1];
    int count = 0;
    for (Column c : schema) {
      String colName = c.getColumnName();
      System.out.println("Column name:"+colName);
      String tablename;
      String alias = c.getTable().getSchemaName();
      System.out.println("SCHEMA NAME:" + alias);
      if(alias!=null){
        tablename = alias;
      }
      else{
        tablename = c.getTable().getName();
      }

      if (colName.equalsIgnoreCase(col) && tablename.equalsIgnoreCase(table)) {
        longValue = tuple.getElementAtIndex(count);
        return;
      }
      count++;
    }
  }
}

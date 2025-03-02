package utilities;

import common.DBCatalog;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.PhysicalOperators.Operator;

public class ColumnProcessor {

  /***
   * Get the columns from where expressions
   * @param op operator to get schema from
   * @param expr where expression for joining
   * @return
   */
  public List<OrderByElement> getOrderByElements(Operator op, Expression expr) {
    ArrayList<String> tableSchema = getColumns(op.getOutputSchema());
    List<OrderByElement> result = new ArrayList<>();
    List<Expression> andExpressions = getAndExpressions(expr);
    List<Expression> allExpressions = getColumnExpressions(andExpressions);
    for (Expression e : allExpressions) {
      if (tableSchema.contains(e.toString())) {
        OrderByElement oE = new OrderByElement();
        oE.setExpression(e);
        result.add(oE);
      }
    }
    return result;
  }

  /***
   *
   * @param expr AndExpression to parse
   * @return list of all the expressions in expr
   */
  private static List<Expression> getAndExpressions(Expression expr) {
    List<Expression> temp = new ArrayList<>();
    while (expr instanceof AndExpression) {
      AndExpression and = (AndExpression) expr;
      temp.add(and.getRightExpression());
      expr = and.getLeftExpression();
    }
    temp.add(expr);
    return temp;
  }

  /***
   *
   * @param outputSchema schema of table
   * @return the names of all the columns in form of Alias.Col or TableName .col
   */
  private static ArrayList<String> getColumns(ArrayList<Column> outputSchema) {
    ArrayList<String> result = new ArrayList<>();
    for (Column col : outputSchema) {
      String name = col.getTable().getName();
      String ali = col.getTable().getSchemaName();
      String col_name = col.getColumnName();
      String col_str = name + "." + col_name;
      if (DBCatalog.getInstance().getUseAlias()) {
        col_str = ali + "." + col_name;
      }
      result.add(col_str);
    }
    return result;
  }

  /***
   *
   * @param exprs List of EqualsTo expressions
   * @return the List of Column Expressions in exprs
   */
  private static List<Expression> getColumnExpressions(List<Expression> exprs) {
    List<Expression> temp = new ArrayList<>();
    for (Expression e : exprs) {
      if (!(e instanceof EqualsTo)) {
        temp.add(e);
        continue;
      } else {
        EqualsTo et = (EqualsTo) e;
        temp.add(et.getLeftExpression());
        temp.add(et.getRightExpression());
      }
    }
    return temp;
  }
}

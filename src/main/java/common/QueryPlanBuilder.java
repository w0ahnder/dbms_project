package common;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import operator.*;

/**
 * Class to translate a JSQLParser statement into a relational algebra query
 * plan. For now only
 * works for Statements that are Selects, and specifically PlainSelects. Could
 * implement the visitor
 * pattern on the statement, but doesn't for simplicity as we do not handle
 * nesting or other complex
 * query features.
 *
 * <p>
 * Query plan fixes join order to the order found in the from clause and uses a
 * left deep tree
 * join. Maximally pushes selections on individual relations and evaluates join
 * conditions as early
 * as possible in the join tree. Projections (if any) are not pushed and
 * evaluated in a single
 * projection operator after the last join. Finally, sorting and duplicate
 * elimination are added if
 * needed.
 *
 * <p>
 * For the subset of SQL which is supported as well as assumptions on semantics,
 * see the Project
 * 2 student instructions, Section 2.1
 */
public class QueryPlanBuilder {
  ArrayList<Expression> andExpressions;
  ArrayList<String> table_names;

  public QueryPlanBuilder() {
  }

  /**
   * Top level method to translate statement to query plan
   *
   * @param stmt statement to be translated
   * @return the root of the query plan
   * @precondition stmt is a Select having a body that is a PlainSelect
   */
  @SuppressWarnings("unchecked")
  public Operator buildPlan(Statement stmt) throws ExecutionControl.NotImplementedException {
    Select select = (Select) stmt;
    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    Table fromItemT = (Table) plainSelect.getFromItem();
    String tableName = fromItemT.getName();
    ArrayList<Column> schema = DBCatalog.getInstance().get_Table(tableName);

    String table_path = DBCatalog.getInstance().getFileForTable(tableName).getPath();
    Expression where = plainSelect.getWhere();

    FromItem fromItem = plainSelect.getFromItem();
    table_names = new ArrayList<>();
    List<SelectItem> selectItems = plainSelect.getSelectItems();
    // FROM Sailors, Reserves,Boats
    // Sailors is given in .getFromItem
    // [Reserves, Boats] in .getJoins() plainSelect.getJoins().get(0).toString is
    // table name
    // now evaluate
    ScanOperator sc = null;
    SelectOperator so = null;
    ProjectOperator po = null;

    try {
      sc = new ScanOperator(schema, table_path);
      so = new SelectOperator(sc.getOutputSchema(), sc, where);
      po = new ProjectOperator(schema, sc, so, selectItems);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    if (selectItems.size() > 1 || !(selectItems.get(0) instanceof AllColumns)) {
      return po;
    }
    if (where == null) {
      return sc;
    }

    return so;
  }

  public ArrayList<Expression> getAnds(Expression where) {
    ArrayList<Expression> ands = new ArrayList<>();
    if (where != null) {
      while (where instanceof AndExpression) {
        ands.add(((AndExpression) where).getRightExpression());
        where = ((AndExpression) where).getLeftExpression();
      }
      ands.add(where);
    }
    return ands;
  }

  public void getColumn(Expression ex) {
    for (int i = 0; i < andExpressions.size(); i++) {
      Expression curr = andExpressions.get(i);
      Expression right = (((BinaryExpression) ex).getRightExpression());
      Expression left = (((BinaryExpression) ex).getLeftExpression());
      String right_and_expr = ((BinaryExpression) right).getRightExpression().toString();
      String left_and_expr = ((BinaryExpression) left).getLeftExpression().toString();
      String[] right_col = right_and_expr.split("\\.");
      String[] left_col = left_and_expr.split("\\.");
      String table;
      String col;
      if (right_col.length > 1) {
        table = right_col[0];
        col = right_col[1];
      }
    }
  }
}

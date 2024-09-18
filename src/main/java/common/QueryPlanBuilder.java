package common;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import operator.*;

/**
 * Class to translate a JSQLParser statement into a relational algebra query plan. For now only
 * works for Statements that are Selects, and specifically PlainSelects. Could implement the visitor
 * pattern on the statement, but doesn't for simplicity as we do not handle nesting or other complex
 * query features.
 *
 * <p>Query plan fixes join order to the order found in the from clause and uses a left deep tree
 * join. Maximally pushes selections on individual relations and evaluates join conditions as early
 * as possible in the join tree. Projections (if any) are not pushed and evaluated in a single
 * projection operator after the last join. Finally, sorting and duplicate elimination are added if
 * needed.
 *
 * <p>For the subset of SQL which is supported as well as assumptions on semantics, see the Project
 * 2 student instructions, Section 2.1
 */
public class QueryPlanBuilder {
  ArrayList<Expression> andExpressions = new ArrayList<>();
  ArrayList<String> tableNames;

  public QueryPlanBuilder() {}

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
    String tableName = fromItemT.getName().trim();
    ArrayList<Column> schema = DBCatalog.getInstance().get_Table(tableName);
    List<Join> joinTables =
        Optional.ofNullable(plainSelect.getJoins()).orElse(Collections.emptyList());

    ArrayList<String> tables = new ArrayList<>();
    tables.add(tableName);
    joinTables.stream()
        .forEach(
            join -> {
              Table fromTable = (Table) join.getRightItem();
              String fromName = fromTable.getName();
              tables.add(fromName);
            });
    String table_path = DBCatalog.getInstance().getFileForTable(tableName).getPath();
    Expression where = plainSelect.getWhere();

    // List of all and expressions
    if (where != null) {
      andExpressions = getAndExpressions(where);
    }

    List<SelectItem> selectItems = plainSelect.getSelectItems();
    Operator result = null;
    try {
      result = new ScanOperator(schema, table_path);
      if (where != null) {
        result = new SelectOperator(schema, result, where);
      }
      if (tables.size() > 1) {
        result = createJoinOperator(andExpressions, tables);
      }
      if (selectItems.size() > 1 || !(selectItems.get(0) instanceof AllColumns)) {
        result = new ProjectOperator(result.getOutputSchema(), result, selectItems);
      }
      return result;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private JoinOperator createJoinOperator(
      ArrayList<Expression> andExpressions, ArrayList<String> tableNames)
      throws FileNotFoundException {
    JoinOperator joinOperator = null;
    if (tableNames.size() == 1) {
      return joinOperator;
    }
    String lastTable = tableNames.get(tableNames.size() - 1);
    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    ArrayList<Expression> leftExpressions = new ArrayList<>();
    ArrayList<Expression> inExpressions = new ArrayList<>();
    ArrayList<Expression> rightExpressions = new ArrayList<>();
    List<String> tablesInExpression;

    for (Expression expr : andExpressions) {
      tablesInExpression = tablesNamesFinder.getTableList(expr);
      if (tablesInExpression.size() == 1 && tablesInExpression.get(0).equalsIgnoreCase(lastTable)) {
        rightExpressions.add(expr);
      } else if (tablesInExpression.contains(lastTable)) {
        inExpressions.add(expr);
      } else {
        leftExpressions.add(expr);
      }
    }

    // expressions that do not mention the last table
    Expression leftExpression;
    if (leftExpressions.size() == 0) {
      leftExpression = null;
    } else if (leftExpressions.size() == 1) {
      leftExpression = leftExpressions.get(0);
    } else {
      leftExpression = createAndExpression(leftExpressions);
    }

    // expressions that only mention the last table
    Expression rightExpression;
    if (rightExpressions.size() == 0) {
      rightExpression = null;
    } else if (rightExpressions.size() == 1) {
      rightExpression = rightExpressions.get(0);
    } else {
      rightExpression = createAndExpression(rightExpressions);
    }

    // expressions that mention the last table and another table
    Expression inExpression;
    if (inExpressions.size() == 0) {
      inExpression = null;
    } else if (inExpressions.size() == 1) {
      inExpression = inExpressions.get(0);
    } else {
      inExpression = createAndExpression(inExpressions);
    }

    ArrayList<Column> joinSchema;
    if (tableNames.size() == 2) {
      // If two tables remaining process each and combine
      ArrayList<Column> rightSchema = DBCatalog.getInstance().get_Table(lastTable);
      ArrayList<Column> leftSchema = DBCatalog.getInstance().get_Table(tableNames.get(0));
      String rightTablePath = DBCatalog.getInstance().getFileForTable(lastTable).getPath();
      String leftTablePath = DBCatalog.getInstance().getFileForTable(tableNames.get(0)).getPath();
      Operator rightOperator = new ScanOperator(rightSchema, rightTablePath);
      Operator leftOperator = new ScanOperator(leftSchema, leftTablePath);
      if (rightExpression != null) {
        rightOperator = new SelectOperator(rightSchema, rightOperator, rightExpression);
      }
      if (leftExpression != null) {
        leftOperator = new SelectOperator(leftSchema, leftOperator, leftExpression);
      }
      joinSchema = new ArrayList<>();
      joinSchema.addAll(leftSchema);
      joinSchema.addAll(rightSchema);
      joinOperator = new JoinOperator(joinSchema, leftOperator, rightOperator, inExpression);

    } else {
      tableNames.remove(tableNames.get(tableNames.size() - 1));
      Operator leftOperator = createJoinOperator(getAndExpressions(leftExpression), tableNames);
      ArrayList<Column> rightSchema = DBCatalog.getInstance().get_Table(lastTable);
      String rightTablePath = DBCatalog.getInstance().getFileForTable(lastTable).getPath();
      Operator rightOperator = new ScanOperator(rightSchema, rightTablePath);
      ;
      if (rightExpression != null) {
        rightOperator = new SelectOperator(rightSchema, rightOperator, rightExpression);
      }

      ArrayList<Column> leftSchema = leftOperator.getOutputSchema();
      joinSchema = new ArrayList<>();
      joinSchema.addAll(leftSchema);
      leftSchema.addAll(rightSchema);
      joinOperator =
          new JoinOperator(joinSchema, leftOperator, rightOperator, inExpression);
    }
    return joinOperator;
  }

  private AndExpression createAndExpression(List<Expression> expressions) {
    if (expressions.size() < 2) {
      return null;
    }
    AndExpression andExpression = new AndExpression(expressions.get(0), expressions.get(1));
    expressions.remove(0);
    expressions.remove(0);
    while (expressions.size() > 0) {
      andExpression = new AndExpression(andExpression, expressions.get(0));
      expressions.remove(0);
    }
    return andExpression;
  }

  public ArrayList<Expression> getAndExpressions(Expression where) {
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
}

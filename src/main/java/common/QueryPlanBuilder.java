package common;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
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
  ArrayList<String> aliases;
  Boolean if_alias = false;

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
    if_alias = false;
    Select select = (Select) stmt;
    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    Table fromItemT = (Table) plainSelect.getFromItem();
    String tableName = fromItemT.getName().trim();

    Alias alias1 = plainSelect.getFromItem().getAlias();
    aliases = new ArrayList<>();
    if (alias1 != null) {
      if_alias = true;
      // set alias boolean in DBCatalog
      DBCatalog.getInstance().setUseAlias(if_alias);
      DBCatalog.getInstance().setTableAlias(tableName, alias1.toString().trim());
      aliases.add(alias1.toString().trim());
    } else {
      if_alias = false;
      DBCatalog.getInstance().setUseAlias(if_alias);
      DBCatalog.getInstance().resetDB();
    }

    List<Join> joinTables =
        Optional.ofNullable(plainSelect.getJoins()).orElse(Collections.emptyList());
    // get rest of the tables in the join
    ArrayList<String> tables = new ArrayList<>();

    tables.add(tableName);
    joinTables.forEach(
        join -> {
          Table fromTable = (Table) join.getRightItem();
          String fromName = fromTable.getName();
          tables.add(fromName);
          if (if_alias) {
            // set alias for the all the tables used in the join
            String alias = join.getRightItem().getAlias().toString().trim();
            aliases.add(alias);

            // add alias, tablename to hashmap
            DBCatalog.getInstance().setTableAlias(fromName, alias);
          }
        });

    if (if_alias) {
      assert alias1 != null;
      tableName = alias1.toString().trim();
    }
    String table_path = DBCatalog.getInstance().getFileForTable(tableName).getPath();
    ArrayList<Column> schema = DBCatalog.getInstance().get_Table(tableName);
    Expression where = plainSelect.getWhere();

    // List of all AND expressions
    if (where != null) {
      andExpressions = getAndExpressions(where);
    }
    List<SelectItem> selectItems = plainSelect.getSelectItems();
    List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
    boolean isDistinct = plainSelect.getDistinct() != null;
    Operator result = null;
    try {
      result = new ScanOperator(schema, table_path);
      if (where != null) {
        result = new SelectOperator(schema, (ScanOperator) result, where);
      }

      if (tables.size() > 1) {
        if (if_alias) result = createJoinOperator(andExpressions, copyList(aliases));
        else result = createJoinOperator(andExpressions, copyList(tables));

      }

      if (selectItems.size() >= 1 ){

        ArrayList<Column> newSchema = new ArrayList<>();
        if(!(selectItems.get(0) instanceof AllColumns)) {
          for (SelectItem selectItem : selectItems) {
            Column c = (Column) ((SelectExpressionItem) selectItem).getExpression();
            newSchema.add(c);
          }
        }
        ArrayList<Column> schem = new ArrayList<>();
        ArrayList<String> schemaTables;
        if (if_alias) schemaTables = aliases;
        else schemaTables = tables;
        for (String t : schemaTables) {
          ArrayList<Column> p = DBCatalog.getInstance().get_Table(t);
          schem.addAll(p);
        }
        if(selectItems.get(0) instanceof AllColumns){
          result = new ProjectOperator(schem, schem, result, selectItems);
        }
        else {
          result = new ProjectOperator(newSchema, schem, result, selectItems);
        }
      }
      if (orderByElements != null) {
        result = new SortOperator(result.getOutputSchema(), orderByElements, result);
      }
      if (isDistinct) {
        if (orderByElements != null) {
          result =
              new DuplicateEliminationOperator(result.getOutputSchema(), (SortOperator) result);
        } else {
          SortOperator child =
              new SortOperator(result.getOutputSchema(), new ArrayList<>(), result);
          result = new DuplicateEliminationOperator(result.getOutputSchema(), child);
        }
      }
      return result;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a tree of all necessary operators and returns using recursion
   *
   * @param andExpressions List of all expressions in a statement
   * @param tableNames names of all tables to be joined
   * @return JoinOperator object that is the root of all other operators
   * @throws FileNotFoundException
   */
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
    List<String> tablesInExpression = new ArrayList<>();

    for (Expression expr : andExpressions) {
      // GETS ALL TABLE NAMES IN THE EXPRESSION
      tablesInExpression = tablesNamesFinder.getTableList(expr);
      if (tablesInExpression.size() == 1
          && tablesInExpression.get(0).trim().equalsIgnoreCase(lastTable)) {
        rightExpressions.add(expr);
      } else if (tablesInExpression.contains(lastTable)) {
        inExpressions.add(expr);
      } else {
        leftExpressions.add(expr);
      }
    }

    // expressions that do not mention the last table
    Expression leftExpression;
    if (leftExpressions.isEmpty()) {
      leftExpression = null;
    } else if (leftExpressions.size() == 1) {
      leftExpression = leftExpressions.get(0);
    } else {
      leftExpression = createAndExpression(leftExpressions);
    }
    // expressions that only mention the last table
    Expression rightExpression;
    if (rightExpressions.isEmpty()) {
      rightExpression = null;
    } else if (rightExpressions.size() == 1) {
      rightExpression = rightExpressions.get(0);
    } else {
      rightExpression = createAndExpression(rightExpressions);
    }
    // expressions that mention the last table and another table
    Expression inExpression;
    if (inExpressions.isEmpty()) {
      inExpression = null;
    } else if (inExpressions.size() == 1) {
      inExpression = inExpressions.get(0);
    } else {
      inExpression = createAndExpression(inExpressions);
    }
    ArrayList<Column> joinSchema;
    if (tableNames.size() == 2) {
      joinOperator = joinTwoTables(tableNames, rightExpression, leftExpression, inExpression);
    } else {
      tableNames.remove(tableNames.get(tableNames.size() - 1));
      Operator leftOperator = createJoinOperator(getAndExpressions(leftExpression), tableNames);

      ArrayList<Column> rightSchema = DBCatalog.getInstance().get_Table(lastTable);
      String rightTablePath = DBCatalog.getInstance().getFileForTable(lastTable).getPath();
      Operator rightOperator = new ScanOperator(rightSchema, rightTablePath);
      ;
      if (rightExpression != null) {
        rightOperator =
            new SelectOperator(rightSchema, (ScanOperator) rightOperator, rightExpression);
      }

      ArrayList<Column> leftSchema = leftOperator.getOutputSchema();
      joinSchema = new ArrayList<>();
      joinSchema.addAll(leftSchema);
      leftSchema.addAll(rightSchema);
      joinOperator = new JoinOperator(joinSchema, leftOperator, rightOperator, inExpression);
    }
    return joinOperator;
  }

  /**
   * Base case for JoinOperator: a case where there are only two tables to join
   *
   * @param tableNames : the list of names of tables to be joined
   * @param rightExpression : Expressions that only mention the right table
   * @param leftExpression : Expressions that only mention left table
   * @param inExpression : Expressions that contain both right and left table Example: SELECT A, B
   *     WHERE A.C = 1 AND A.D = B.C AND B.D = 2 AND A.D = 4 tableNames = [A, B] rightExpression:
   *     B.D = 2 leftExpression: A.C = 1 AND A.D = 4 inExpression: A.D = B.C
   * @return JoinOperator object
   */
  private JoinOperator joinTwoTables(
      ArrayList<String> tableNames,
      Expression rightExpression,
      Expression leftExpression,
      Expression inExpression)
      throws FileNotFoundException {
    String leftTable = tableNames.get(0);
    String rightTable = tableNames.get(1);

    ArrayList<Column> rightSchema = DBCatalog.getInstance().get_Table(rightTable);
    ArrayList<Column> leftSchema = DBCatalog.getInstance().get_Table(leftTable);

    String rightTablePath = DBCatalog.getInstance().getFileForTable(rightTable).getPath();
    String leftTablePath = DBCatalog.getInstance().getFileForTable(leftTable).getPath();
    Operator rightOperator = new ScanOperator(rightSchema, rightTablePath);
    Operator leftOperator = new ScanOperator(leftSchema, leftTablePath);
    if (rightExpression != null) {
      rightOperator =
          new SelectOperator(rightSchema, (ScanOperator) rightOperator, rightExpression);
    }
    if (leftExpression != null) {
      leftOperator = new SelectOperator(leftSchema, (ScanOperator) leftOperator, leftExpression);
    }
    ArrayList<Column> joinSchema = new ArrayList<>();
    joinSchema.addAll(leftSchema);
    joinSchema.addAll(rightSchema);
    return new JoinOperator(joinSchema, leftOperator, rightOperator, inExpression);
  }

  /**
   * Changes an andExpression into a list of Expressions which are not andExpression
   *
   * @param where an Expression
   * @return a list of Expressions that are not andExpressions
   */
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

  /**
   * Takes in a list of expressions and connects them with an and clause
   *
   * @param expressions: List of expressions
   * @return an AndExpression that connects all other expressions
   */
  private AndExpression createAndExpression(List<Expression> expressions) {
    if (expressions.size() < 2) {
      return null;
    }
    AndExpression andExpression = new AndExpression(expressions.get(0), expressions.get(1));
    expressions.remove(0);
    expressions.remove(0);
    while (!expressions.isEmpty()) {
      andExpression = new AndExpression(andExpression, expressions.get(0));
      expressions.remove(0);
    }
    return andExpression;
  }

  private ArrayList<String> copyList(ArrayList<String> l) {
    ArrayList<String> res = new ArrayList<>();
    res.addAll(l);
    return res;
  }
}

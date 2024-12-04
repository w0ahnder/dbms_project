package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import operator.LogicalOperators.DuplicateEliminationLogOperator;
import operator.LogicalOperators.LogicalOperator;
import operator.LogicalOperators.NewJoinLogOperator;
import operator.LogicalOperators.ProjectLogOperator;
import operator.LogicalOperators.ScanLogOperator;
import operator.LogicalOperators.SelectLogOperator;
import operator.LogicalOperators.SortLogOperator;
import operator.PhysicalOperators.*;
import operator.PhysicalOperators.Operator;

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
  ArrayList<String> tables = new ArrayList<>();
  Integer indexFlag;
  Integer queryFlag;
  Boolean is_sorted = false;
  LogicalOperator logicalOP;
  Operator rootOperator;

  public QueryPlanBuilder() {}

  public void indexEval() {
    if (DBCatalog.getInstance().ifBuild()) {
    } else {
      // means indexes are provided
    }
  }

  /**
   * Top level method to translate statement to query plan
   *
   * @param stmt statement to be translated
   * @return the root of the query plan
   * @precondition stmt is a Select having a body that is a PlainSelect
   */
  @SuppressWarnings("unchecked")
  public Operator buildPlan(Statement stmt, String tempDir, Integer indexFlag, Integer queryFlag)
      throws ExecutionControl.NotImplementedException {

    if (!DBCatalog.getInstance().isEvalQuery()) {
      return null;
    }

    this.indexFlag = indexFlag;
    this.queryFlag = queryFlag;
    this.is_sorted = false;
    // List<Integer> joinConfig = planConfList.get(0);

    // I think that the temp directory is within the interpreter_config_file.txt
    // means we have to process this first ^^ to get the tempDir, sort/join types, inputDir, etc

    tables = new ArrayList<>();
    andExpressions = new ArrayList<>();
    if_alias = false;
    DBCatalog.getInstance().resetDB();
    DBCatalog.getInstance().setBNLbuff(4);
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

    // Join tables is the list of all tables to join left to right
    List<Join> joinTables =
        Optional.ofNullable(plainSelect.getJoins()).orElse(Collections.emptyList());

    tables.add(tableName);
    joinTables.forEach(
        join -> {
          Table fromTable = (Table) join.getRightItem();
          String fromName = fromTable.getName();
          tables.add(fromName);
          if (if_alias) {
            String alias = join.getRightItem().getAlias().toString().trim();
            aliases.add(alias);
            DBCatalog.getInstance().setTableAlias(fromName, alias);
          }
        });

    if (if_alias) {
      tableName = alias1.toString().trim();
    }

    String table_path = DBCatalog.getInstance().getFileForTable(tableName).getPath();
    ArrayList<Column> schema = new ArrayList<>();
    schema = copyColumn(DBCatalog.getInstance().get_Table(tableName), tableName);

    Expression where = plainSelect.getWhere();

    // List of all AND expressions
    if (where != null) {
      SelectPushVisitor pushSelect = new SelectPushVisitor(where);
      pushSelect.evaluate_expr();
      System.out.println(
          "Self table select: "
              + pushSelect.sameTableSelect); // for select have same columns on either side
      System.out.println("Joins: " + pushSelect.joins);
      System.out.println("Generated select expr: " + pushSelect.usable_expr.generateExpr());
      andExpressions.addAll(pushSelect.sameTableSelect);
      andExpressions.addAll(pushSelect.joins);
      andExpressions.addAll(pushSelect.usable_expr.generateExpr());
    }

    /*if (where != null) {
      andExpressions = getAndExpressions(where);
    }
    */

    // For Project
    List<SelectItem> selectItems = plainSelect.getSelectItems();

    // For ORDER BY
    List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

    // For DISTINCT
    boolean isDistinct = plainSelect.getDistinct() != null;

    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    List<String> tablesInExpression = new ArrayList<>();

    Map<String, ArrayList<Expression>> selectExpressions = new HashMap<>();
    Map<String, ArrayList<Expression>> joinExpressions = new HashMap<>();

    if (if_alias) tables = copyList(aliases);
    for (String table : tables) {
      selectExpressions.put(table, new ArrayList<>());
      joinExpressions.put(table, new ArrayList<>());
    }

    for (Expression expr : andExpressions) {
      // GETS ALL TABLE NAMES IN THE EXPRESSION

      tablesInExpression = tablesNamesFinder.getTableList(expr);
      if (tablesInExpression.size() == 0) {
        // ADDING EXPRESSIONS WITH NO TABLE TO THE FIRST TABLE
        selectExpressions
            .get(tables.get(0))
            .add(expr); // means select expressions for a table, only have one table in them
      } else if (tablesInExpression.size()
          == 1) { // get the tablein the expression and add the expression
        selectExpressions.get(tablesInExpression.get(0).trim()).add(expr);
      } else { // if expression has more than one table, get the last table and put the expression
        // for it
        Integer firstTableIndex = tables.indexOf(tablesInExpression.get(0).trim());
        Integer secondTableIndex = tables.indexOf(tablesInExpression.get(1).trim());
        String lastTable = tables.get(Integer.max(firstTableIndex, secondTableIndex));
        joinExpressions.get(lastTable).add(expr);
      }
    }

    LogicalOperator result = null;
    // TODO: 1. Make a list of all expressions for each table
    // TODO: 2. Make upper - lower limit + 1 for each table
    ArrayList<LogicalOperator> tableExprs = new ArrayList<>();
    Map<String, LogicalOperator> tableToOp = new HashMap<>();
    ArrayList<Column> outputSchema = new ArrayList<>();
    // outputSchema.addAll(result.getOutputSchema());
    // SCAN, SELECT, AND JOIN
    HashMap<String, HashMap<String, Integer[]>> vvalues = new HashMap<>();
    for (String table : tables) {
      table_path = DBCatalog.getInstance().getFileForTable(table).getPath();
      schema = copyColumn(DBCatalog.getInstance().get_Table(table), table);
      LogicalOperator op = new ScanLogOperator(schema, table_path, table);
      ArrayList<Expression> selectExpr = selectExpressions.get(table);

      // SELECT
      if (selectExpr.size() > 0) {
        op =
            new SelectLogOperator(
                createAndExpression(selectExpressions.get(table)), op, table, table_path);
      } // for each table, check if can use index scan or need regular select

      if (tables.get(0) == table) {
        result = op;
      } else {
        // ArrayList<Column> outputSchema = new ArrayList<>();
        outputSchema.addAll(result.getOutputSchema());
        outputSchema.addAll(op.getOutputSchema());
      } // for each table, check if can use index scan or need regular select

      tableExprs.add(op);
      tableToOp.put(table, op);
    }
    if (tables.size() > 1) {
      result =
          new NewJoinLogOperator(
              outputSchema,
              tableExprs,
              tables,
              selectExpressions,
              joinExpressions,
              tableToOp,
              vvalues);
      // TODO: REMOVE!! these lines
      // PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder();
      // try {
      //   result.accept(physicalPlanBuilder);
      // } catch (FileNotFoundException e) {
      //   throw new RuntimeException(e);
      // }
      // return physicalPlanBuilder.returnResultTuple();
      // return result;
    }
    // WE NEED:
    // UPPER AND LOWER LIMITS FOR EACH TABLE COLUMN
    // LIST OF TABLES TO BE JOINED
    // MAP OF TABLE -> UPPER AND LOWER LIMIT

    // ORDER BY
    if (orderByElements != null) {
      is_sorted = true;
      result = new SortLogOperator(orderByElements, result, 4, tempDir);
    }

    // PROJECT
    if (selectItems.size() >= 1) {
      ArrayList<Column> newSchema = new ArrayList<>();
      if (!(selectItems.get(0) instanceof AllColumns)) {
        for (SelectItem selectItem : selectItems) {
          Column c = (Column) ((SelectExpressionItem) selectItem).getExpression();
          if (if_alias) {
            String al = c.getTable().getName();
            c.getTable().setName(DBCatalog.getInstance().getTableName(al));
            c.getTable().setSchemaName(al);
          }
          newSchema.add(c);
        }
        result = new ProjectLogOperator(result, selectItems, newSchema);
      }
    }

    // DISTINCT
    if (isDistinct) {
      if (is_sorted) {
        result = new DuplicateEliminationLogOperator(result.getOutputSchema(), result);
        // result = new DuplicateEliminationLogOperator(schema, result);
      } else {
        SortLogOperator child;
        child = new SortLogOperator(new ArrayList<>(), result, 4, tempDir);
        result = new DuplicateEliminationLogOperator(result.getOutputSchema(), child);
      }
    }

    logicalOP = result;
    PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder();
    try {
      result.accept(physicalPlanBuilder);
      rootOperator = physicalPlanBuilder.getRootOp();

    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    return physicalPlanBuilder.returnResultTuple();
  }

  public void printPhysicalPlan(String path) {
    File queryi = new File(path + "physicalplan");
    try {
      PrintStream ps = new PrintStream(queryi);
      logicalOP.printLog(ps, 0);
      ps.close();
      printPhysicalPlan(path);

    } catch (Exception e) {
      System.out.println("Failed to print physical plan");
    }
  }

  public void printLogicalPlan(String path) {
    File queryi = new File(path + "logicalplan");
    try {
      PrintStream ps = new PrintStream(queryi);
      logicalOP.printLog(ps, 0);
      ps.close();
      printPhysicalPlan(path);

    } catch (Exception e) {
      System.out.println("Failed to print logical plan");
    }
  }

  /**
   * Changes an andExpression into a list of Expressions which are not andExpression
   *
   * @param where an Expression
   * @return a list of Expressions that are not andExpressions
   */
  public ArrayList<Expression> getAndExpressions(Expression where) {
    ArrayList<Expression> ands = new ArrayList<>();
    while (where instanceof AndExpression) {
      ands.add(((AndExpression) where).getRightExpression());
      where = ((AndExpression) where).getLeftExpression();
    }
    ands.add(where);
    return ands;
  }


  /**
   * Takes in a list of expressions and connects them with an and clause
   *
   * @param expressions: List of expressions
   * @return an AndExpression that connects all other expressions
   */
  private Expression createAndExpression(List<Expression> expressions) {
    if (expressions.size() < 1) {
      return null;
    } else if (expressions.size() == 1) {
      return expressions.get(0);
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

  private ArrayList<Column> copyColumn(ArrayList<Column> columns, String table) {
    ArrayList<Column> res = new ArrayList<>();
    String al = null;
    for (Column c : columns) {
      if (if_alias) {
        al = table;
      }
      res.add(new Column(new Table(al, c.getTable().getName().toString()), c.getColumnName()));
    }
    return res;
  }
}

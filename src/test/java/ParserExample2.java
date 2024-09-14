import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import operator.ScanOperator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

/**
 * Example class for getting started with JSQLParser. Reads SQL statements from a file and prints
 * them to screen; then extracts SelectBody from each query and also prints it to screen.
 */
public class ParserExample2 {
  private final Logger logger = LogManager.getLogger();

  @Test
  public void parserExampleTest() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();

    URI resourceUri = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();

    Path resourcePath = Paths.get(resourceUri);
    // loads all tables and column names
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesUri =
        Objects.requireNonNull(classLoader.getResource("samples/input/queries2.sql")).toURI();
    Path queriesFilePath = Paths.get(queriesUri);

    Statements statements = CCJSqlParserUtil.parseStatements(Files.readString(queriesFilePath));

    QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();

    for (Statement statement : statements.getStatements()) {
      logger.info("Read statement: " + statement);

      Select select = (Select) statement;
      PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
      List<SelectItem> selectCols = plainSelect.getSelectItems();
      TablesNamesFinder tf = new TablesNamesFinder();
      List<String> fromTables = tf.getTableList(statement);

      Table fromItem = (Table) plainSelect.getFromItem();
      String tableName = fromItem.getName();
      //////gets the entire WHERE condition.
      FromItem from = plainSelect.getFromItem();
      logger.info("From item is " + from);
      if(plainSelect.getJoins()!=null) {
        logger.info("From item joins is " + (plainSelect.getJoins()).get(0).toString());

      }
      else {
        logger.info("From item joins is " + (plainSelect.getJoins()));
      }

      Expression where = plainSelect.getWhere();
      if(where!=null){
        logger.info("left where:" + ((BinaryExpression)where).getLeftExpression());
      }
      if(where instanceof AndExpression) {
        AndExpression andexp = (AndExpression) where;
        logger.info("And left Body:" + andexp.getLeftExpression());
        logger.info("And RIGHT Body:" + andexp.getRightExpression());
        Expression right_and = (((BinaryExpression)andexp).getRightExpression());
        String right_and_expr = ((BinaryExpression)right_and).getLeftExpression().toString();
        String[] table_column = right_and_expr.split("\\.");
        logger.info("THIS IS A TABLE: " + table_column[0] + " THIS IS A COLUMN: "+ table_column[1]);

        logger.info("right where:" +((BinaryExpression)(andexp.getLeftExpression())).getRightExpression());

      }
      logger.info("Select body is " + select.getSelectBody());
      logger.info("From item is " + fromItem);
      // get file for from table
      File table_file = DBCatalog.getInstance().getFileForTable(fromItem.getName());
      String table_path = table_file.getPath();
      ScanOperator sc = new ScanOperator(DBCatalog.getInstance().get_Table(tableName), table_path);
      List<Tuple> tuples = sc.getAllTuples();
      for (Tuple t : tuples) {
        logger.info("From item is " + t.toString());
      }
      logger.info("Where is :" + where);


      /*for(SelectItem s: selectCols){
          logger.info("Select item is " + s);
        }
        if(fromTables !=null) {
          for (String t : fromTables) {
            logger.info("Table name is " + t);
          }
        }
      */
      /*
        Alias alias = fromItem.getAlias();
        String name = fromItem.getName();

        logger.info("Alias: " + alias);
        logger.info("Name: " + name);
      */
      List<Join> joins = plainSelect.getJoins();

      if (joins != null) {
        for (Join join : plainSelect.getJoins()) {
          // Process joins..
        }
      }
    }
  }
}

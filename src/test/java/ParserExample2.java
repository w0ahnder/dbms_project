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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
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
        Objects.requireNonNull(classLoader.getResource("samples/input/queries.sql")).toURI();
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
      Expression where = plainSelect.getWhere();
      //AndExpression andexp = where.visit(this);

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

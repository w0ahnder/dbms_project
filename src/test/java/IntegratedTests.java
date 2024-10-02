import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.Operator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IntegratedTests {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(path);

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db2").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples/input/testQueries.sql")).toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void testQuery1()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(0);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expected_size = 0;
    Assertions.assertEquals(expected_size, tuples.size());
  }

  @Test
  public void testQuery2()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(1);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQuery3()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(2);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 15;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQuery4()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(3);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    Assertions.assertEquals(12, tuples.size());
  }

  @Test
  public void testQuery5()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(4);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 1;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQuery6()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(5);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQueryNo7()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(6);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQueryNo8()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(7);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 180;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQueryNo9()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(8);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 96;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQueryNo10()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(9);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 216;
    Assertions.assertEquals(expectedSize, tuples.size());
  }
}

import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

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
    Statement stmt = statements.getStatements().get(0);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expected_size = 0;
    Assertions.assertEquals(expected_size, tuples.size());
  }

  @Test
  public void testQuery2() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(1);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
    // 1,200,50
    // 2,200,200
    // 6,300,400
    // pair the below tuples with each Boat tupe=> 20 tuples in result
    // 1, 200, 50, 1, 101
    // 1, 200, 50, 1, 102
    // 1, 200, 50, 1, 103
    // 2, 200, 200, 2, 101
  }

  @Test
  public void testQuery3() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(2);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 15;
    Assertions.assertEquals(expectedSize, tuples.size());
    /*
    // 1, 200, 50, 2, 200, 200
    // 1,200, 50, 3, 100, 105...5
    // 2,200,200, 3, 100, 105
    // 2,200,200, 4, 100, 50 ..4
    // 3, 100, 105, 4, 100, 50
    // 3, 100, 105, 5, 100, 500 ...3
    // 4, 100, 50, 5, 100, 500, ... 2
    // 5, 100, 500, 6, 300, 400..1
     */
  }

  @Test
  public void testQuery4() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(3);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    Assertions.assertEquals(12, tuples.size());
    // 1, 200, 50, 2, 200, 200, 1, 200, 50 ..6
    // 2,200,200, 2, 200, 200, 1, 200, 50 ...6 12 total
  }

  @Test
  public void testQuery5() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(4);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 1;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQuery6() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(5);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
  }

  @Test
  public void testQuery7() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(6);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 20;
    Assertions.assertEquals(expectedSize, tuples.size());
    // 1,200, 50, X [(1, 101), (1, 102), (1, 103)]
    // 2, 200, 200 X [(2, 101)]
    // 6, 300, 400
    // 5 tuples crossed with entries in Boats =>20
    // 1, 101
    // 1, 102
    // 1, 103
    // 2, 101
  }
  @Test
  public void testQuery8() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(7);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 180;
    Assertions.assertEquals(expectedSize, tuples.size());

  }
  @Test
  public void testQuery9() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(8);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 96;
    Assertions.assertEquals(expectedSize, tuples.size());

  }

  @Test
  public void testQuery10() throws ExecutionControl.NotImplementedException {
    Statement stmt = statements.getStatements().get(9);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 216;
    Assertions.assertEquals(expectedSize, tuples.size());

    /*Tuple[] expectedTuples =
            new Tuple[]{
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 50))),
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 200))),
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 105))),
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 50))),
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 500))),
                    new Tuple(new ArrayList<>(Arrays.asList(1, 100, 400)))
            };
    for(int i =0; i<7; i++){
      Tuple expectedTuple = expectedTuples[i];
      Tuple actualTuple = tuples.get(i);
      Assertions.assertEquals(expectedTuple, actualTuple, "Unexpected tuple at index " + i);
    }*/

  }
}


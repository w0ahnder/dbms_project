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

public class SelectOperatorTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = ScanOperatorTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(path);

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples/input/custom_queries.sql")).toURI();

    Statements statements =
        CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void getNextTupleTest() throws ExecutionControl.NotImplementedException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(1));

    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 3;

    Assertions.assertEquals(expectedSize, tuples.size(), "Unexpected number of rows.");

    Tuple[] expectedTuples =
        new Tuple[] {
          new Tuple(new ArrayList<>(List.of(1, 200, 50))),
          new Tuple(new ArrayList<>(List.of(2, 200, 200))),
          new Tuple(new ArrayList<>(List.of(3, 100, 105))),
        };

    for (int i = 0; i < expectedSize; i++) {
      Tuple expectedTuple = expectedTuples[i];
      Tuple actualTuple = plan.getNextTuple();
      Assertions.assertEquals(expectedTuple, actualTuple, "Unexpected tuple at index " + i);
    }
  }

  @Test
  public void assertNullGetNextTuple() throws ExecutionControl.NotImplementedException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(9));

    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 0;
    Assertions.assertEquals(expectedSize, tuples.size(), "Unexpected number of rows.");

    Assertions.assertNull(plan.getNextTuple());
  }

  @Test
  public void resetTest() throws ExecutionControl.NotImplementedException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(1));

    int resetIndex = 3;

    Tuple[] expectedTuples =
        new Tuple[] {
          new Tuple(new ArrayList<>(List.of(1, 200, 50))),
          new Tuple(new ArrayList<>(List.of(2, 200, 200))),
          new Tuple(new ArrayList<>(List.of(3, 100, 105))),
        };

    for (int i = 0; i < resetIndex; i++) {
      plan.getNextTuple();
    }
    plan.reset();
    for (int i = 0; i < expectedTuples.length; i++) {
      Tuple expectedTuple = expectedTuples[i];
      Tuple actualTuple = plan.getNextTuple();
      Assertions.assertEquals(expectedTuple, actualTuple, "Unexpected tuple at index " + i);
    }
  }
}

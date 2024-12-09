package common.operator;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JoinOperatorTest {

  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;

  @BeforeEach
  void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = JoinOperatorTest.class.getClassLoader();
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
  public void TestHappyCase() throws ExecutionControl.NotImplementedException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(2));
    List<Tuple> tuples = new ArrayList<>();
    Tuple t;
    while ((t = plan.getNextTuple()) != null) {
      tuples.add(t);
    }
    plan.reset();
    assert (plan.getNextTuple().equals(tuples.get(0)));
    ;
  }

  /*
  @Test
  public void AssertNullIfLeftIsNull() throws ExecutionControl.NotImplementedException {
    JoinOperator plan = (JoinOperator) queryPlanBuilder.buildPlan(statementList.get(3));
    assertNotNull(plan.getRightOperator().getNextTuple());
    plan.getRightOperator().reset();

    assertNull(plan.getLeftOperator().getNextTuple());
    plan.getLeftOperator().reset();

    assertNull(plan.getNextTuple());
  }
  */

  /*
    @Test
    public void AssertNullIfRightIsNull() throws ExecutionControl.NotImplementedException {
      JoinOperator plan = (JoinOperator) queryPlanBuilder.buildPlan(statementList.get(3));
      assertNull(plan.getLeftOperator().getNextTuple());
      plan.getLeftOperator().reset();

      assertNull(plan.getNextTuple());
    }
  */
  /*@Test
    public void AssertNullIfConditionIsFalse() throws ExecutionControl.NotImplementedException {
      JoinOperator plan = (JoinOperator) queryPlanBuilder.buildPlan(statementList.get(5));
      Operator leftOperator = plan.getLeftOperator();
      assertNotNull(leftOperator.getNextTuple());
      leftOperator.reset();

      Operator rightOperator = plan.getRightOperator();
      assertNotNull(rightOperator.getNextTuple());
      rightOperator.reset();

      assertNull(plan.getNextTuple());
    }
  */
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

  public static List<Tuple> collectAllTuples(Operator operator) {
    Tuple tuple;
    List<Tuple> tuples = new ArrayList<>();
    while ((tuple = operator.getNextTuple()) != null) {
      tuples.add(tuple);
    }
    return tuples;
  }
}
